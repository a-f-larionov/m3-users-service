package m3.users.services.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import m3.lib.commons.HttpExceptionError;
import m3.lib.dto.rs.UpdateUserInfoRsDto;
import m3.lib.entities.UserEntity;
import m3.lib.enums.ClientLogLevels;
import m3.lib.enums.StatisticEnum;
import m3.lib.helpers.TelegramSender;
import m3.lib.kafka.sender.CommonSender;
import m3.lib.repositories.UserRepository;
import m3.lib.settings.CommonSettings;
import m3.lib.settings.MapSettings;
import m3.users.dto.rq.AuthRqDto;
import m3.users.dto.rs.*;
import m3.lib.enums.SocNetType;
import m3.users.mappers.UsersMapper;
import m3.users.services.HealthService;
import m3.users.services.SocNetService;
import m3.users.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static m3.lib.commons.ErrorCodes.AUTH_FAILED;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Long DEFAULT_NEXT_POINT_ID = 1L;

    private final UserRepository userRepository;
    private final UsersMapper mapper;
    private final SocNetService socNet;
    private final HealthService healthService;
    private final CommonSender commonSender;
    @Value("${alerter.telegram.token}")
    private String teleToken;
    @Value("${alerter.telegram.chatId}")
    private String chatId;

    public AuthSuccessRsDto auth(AuthRqDto authRqDto) {

        var authSucceeded = socNet.checkAuth(authRqDto);
        if (!authSucceeded) {
            //@todo class ERRORS
            throw new HttpExceptionError(AUTH_FAILED);
        }
        UserEntity outUser;

        Optional<UserEntity> existentUser = userRepository
                .findBySocNetTypeIdAndSocNetUserId(authRqDto.getSocNetType().getId(), authRqDto.getSocNetUserId());

        if (existentUser.isEmpty()) {
            var currentMills = System.currentTimeMillis();
            var entity = mapper.forAuthNewUser(
                    authRqDto,
                    currentMills / 1000,
                    DEFAULT_NEXT_POINT_ID
            );
            outUser = userRepository.save(entity);
        } else {
            outUser = existentUser.orElseThrow();
            var newLoginTime = System.currentTimeMillis() / 1000;
            updateLogin(outUser.getId(), newLoginTime);
            outUser.setLoginTm(newLoginTime);
        }

        TelegramSender.getInstance().sendToTelegram(
                "Игрок вошел в игру 🥰 " + outUser.getId() +
                        " http://vk.com/id" + outUser.getSocNetUserId(), teleToken, chatId);

        return mapper.entityToAuthSuccessRsDto(outUser, authRqDto.getConnectionId());
    }

    private void updateLogin(Long id, Long newLoginTime) {
        userRepository.updateLogin(id, newLoginTime);
    }

    public UpdateUserListInfoRsDto getUsers(Long userId, List<Long> ids) {

        List<UserEntity> usersList = userRepository.findAllByIdIn(ids);

        var list = usersList.stream()
                .map(mapper::entityToDto)
                .toList();

        return UpdateUserListInfoRsDto.builder()
                .userId(userId)
                .list(list)
                .build();
    }

    public void updateLastLogout(Long userId) {
        //@todo moeve mills/1000 to one method
        commonSender.log(userId, "User logout", ClientLogLevels.INFO, true);
        userRepository.updateLastLogout(userId, System.currentTimeMillis() / 1000);
    }

    @Override
    public GotMapFriendIdsRsDto getMapFriends(Long userId, Long mapId, List<Long> fids) {

        var ids = userRepository.gotMapFriends(
                MapSettings.getFirstPointId(mapId),
                MapSettings.getLastPointId(mapId),
                fids
        );

        return GotMapFriendIdsRsDto.builder()
                .userId(userId)
                .mapId(mapId)
                .ids(ids)
                .build();
    }

    @Override
    public GotFriendsIdsRsDto getUserIdsFromSocNetIds(@NonNull Long userId, @NonNull List<Long> friendSocNetIds) {

        List<Long> friendIds = null;

        Optional<UserEntity> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            if (user.getSocNetTypeId().equals(SocNetType.Standalone.getId())) {
                friendIds = userRepository.findAll().stream().map(UserEntity::getId).toList();
            } else {
                friendIds = userRepository.findIdBySocNetTypeIdAndSocNetUserIdIn(user.getSocNetTypeId(), friendSocNetIds);
            }
        }

        return GotFriendsIdsRsDto.builder()
                .userId(userId)
                .fids(friendIds)
                .build();
    }

    public GotTopUsersRsDto getTopUsersRsDto(Long userId, List<Long> ids) {

        var users = userRepository.findAllByIdInOrderByNextPointIdDesc(ids, Pageable.ofSize(CommonSettings.TOP_USERS_LIMIT))
                .stream().map(mapper::entityToDto)
                .toList();

        return GotTopUsersRsDto.builder()
                .userId(userId)
                .users(users)
                .build();
    }

    @Override
    public synchronized SetOneHealthHideRsDto healthUp(Long userId) {

        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            var user = optionalUser.get();
            if (!healthService.isMaxHealths(user)) {
                healthService.setHealths(user,
                        healthService.getHealths(user) + 1);
                userRepository.updateHealth(user.getId(), user.getFullRecoveryTime());
            }
            return SetOneHealthHideRsDto.builder()
                    .userId(userId)
                    .oneHealthHide(false)
                    .fullRecoveryTime(user.getFullRecoveryTime())
                    .build();
        }
        return null;
    }

    @Override
    public SetOneHealthHideRsDto healthDown(Long userId, Long pointId) {

        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            var user = optionalUser.get();

            if (!healthService.getHealths(user).equals(0L)) {
                healthService.setHealths(user,
                        healthService.getHealths(user) - 1);
                userRepository.updateHealth(user.getId(), user.getFullRecoveryTime());
            }
            commonSender.statistic(userId, StatisticEnum.ID_START_PLAY, pointId.toString());

            return SetOneHealthHideRsDto.builder()
                    .userId(userId)
                    .oneHealthHide(true)
                    .fullRecoveryTime(user.getFullRecoveryTime())
                    .build();
        }
        return null;
    }

    @Override
    public UpdateUserInfoRsDto zeroLife(Long userId) {

        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            var user = optionalUser.get();

            if (!user.getSocNetTypeId().equals(SocNetType.Standalone.getId())) {
                return null;
            }
            healthService.setHealths(user, 0L);
            userRepository.updateHealth(user.getId(), user.getFullRecoveryTime());

            return mapper.entityToDto(user);
        }
        return null;
    }
}