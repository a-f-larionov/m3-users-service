package m3.users.services.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import m3.users.commons.HttpExceptionError;
import m3.users.dto.rq.AuthRqDto;
import m3.users.dto.rs.*;
import m3.users.entities.UserEntity;
import m3.users.enums.SocNetType;
import m3.users.mappers.UsersMapper;
import m3.users.repositories.UsersRepository;
import m3.users.services.HealthService;
import m3.users.services.SocNetService;
import m3.users.services.UserService;
import m3.users.settings.CommonSettings;
import m3.users.settings.MapSettings;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static m3.users.commons.ErrorCodes.AUTH_FAILED;

@AllArgsConstructor
@Slf4j
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Long DEFAULT_NEXT_POINT_ID = 1L;

    private final UsersRepository usersRepository;
    private final UsersMapper mapper;
    private final SocNetService socNet;
    private final HealthService healthService;
    //private final LogService logg;

    public AuthSuccessRsDto auth(AuthRqDto authRqDto) {

        var authSucceeded = socNet.checkAuth(authRqDto);
        if (!authSucceeded) {
            //@todo class ERRORS
            throw new HttpExceptionError(AUTH_FAILED);
        }
        UserEntity outUser;

        Optional<UserEntity> existentUser = usersRepository
                .findBySocNetTypeIdAndSocNetUserId(authRqDto.getSocNetType().getId(), authRqDto.getSocNetUserId());

        if (existentUser.isEmpty()) {
            var currentMills = System.currentTimeMillis();
            var entity = mapper.forAuthNewUser(
                    authRqDto,
                    currentMills / 1000,
                    DEFAULT_NEXT_POINT_ID
            );
            outUser = usersRepository.save(entity);
        } else {
            outUser = existentUser.orElseThrow();
            var newLoginTime = System.currentTimeMillis() / 1000;
            updateLogin(outUser.getId(), newLoginTime);
            outUser.setLoginTm(newLoginTime);
        }

        //log.warn("df)");
        //telega.send("🥰" +url);
        //socNet.getUrl();
        //// var url = SocNet(user.socNetTypeId).getUserProfileUrl(user.socNetUserId);
        //    // Logs.log("🥰 ", Logs.LEVEL_NOTIFY, url, Logs.CHANNEL_TELEGRAM);


        return mapper.entityToAuthSuccessRsDto(outUser, authRqDto.getConnectionId());
    }

    private void updateLogin(Long id, Long newLoginTime) {
        usersRepository.updateLogin(id, newLoginTime);
    }

    public UpdateUserListInfoRsDto getUsers(Long userId, List<Long> ids) {

        List<UserEntity> usersList = usersRepository.findAllByIdIn(ids);

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
        usersRepository.updateLastLogout(userId, System.currentTimeMillis() / 1000);
    }

    @Override
    public GotMapFriendIdsRsDto getMapFriends(Long userId, Long mapId, List<Long> fids) {

        var ids = usersRepository.gotMapFriends(
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
    public GotFriendsIdsRsDto getUserIdsFromSocNetIds(Long userId, List<Long> friendSocNetIds) {

        List<Long> friendIds = null;

        Optional<UserEntity> optionalUser = usersRepository.findById(userId);
        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            if (user.getSocNetTypeId().equals(SocNetType.Standalone.getId())) {
                friendIds = usersRepository.findAll().stream().map(UserEntity::getId).toList();
            } else {
                friendIds = usersRepository.findIdBySocNetTypeIdAndSocNetUserIdIn(user.getSocNetTypeId(), friendSocNetIds);
            }
        }

        return GotFriendsIdsRsDto.builder()
                .userId(userId)
                .fids(friendIds)
                .build();
    }

    public GotTopUsersRsDto getTopUsersRsDto(Long userId, List<Long> ids) {

        var users = usersRepository.findAllByIdInOrderByNextPointIdDesc(ids, Pageable.ofSize(CommonSettings.TOP_USERS_LIMIT))
                .stream().map(mapper::entityToDto)
                .toList();

        return GotTopUsersRsDto.builder()
                .userId(userId)
                .users(users)
                .build();
    }

    @Override
    public synchronized SetOneHealthHideRsDto healthUp(Long userId) {

        Optional<UserEntity> optionalUser = usersRepository.findById(userId);

        if (optionalUser.isPresent()) {
            var user = optionalUser.get();
            if (!healthService.isMaxHealths(user)) {
                healthService.setHealths(user,
                        healthService.getHealths(user) + 1);
                usersRepository.updateHealth(user.getId(), user.getFullRecoveryTime());
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
    public SetOneHealthHideRsDto healthDown(Long userId) {

        Optional<UserEntity> optionalUser = usersRepository.findById(userId);

         if (optionalUser.isPresent()) {
            var user = optionalUser.get();


            if (!healthService.getHealths(user).equals(0L)) {
                healthService.setHealths(user,
                        healthService.getHealths(user) - 1);
                usersRepository.updateHealth(user.getId(), user.getFullRecoveryTime());
            }
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

        Optional<UserEntity> optionalUser = usersRepository.findById(userId);

        if (optionalUser.isPresent()) {
            var user = optionalUser.get();

            if (!user.getSocNetTypeId().equals(SocNetType.Standalone.getId())) {
                return null;
            }
            healthService.setHealths(user, 0L);
            usersRepository.updateHealth(user.getId(), user.getFullRecoveryTime());

            return mapper.entityToDto(user);
        }
        return null;
    }
}