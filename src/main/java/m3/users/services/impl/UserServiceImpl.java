package m3.users.services.impl;

import lombok.AllArgsConstructor;
import m3.users.commons.HttpExceptionError;
import m3.users.dto.rq.AuthRqDto;
import m3.users.dto.rq.SendMeUserInfoRqDto;
import m3.users.dto.rq.UpdateLastLogoutRqDto;
import m3.users.dto.rs.AuthSuccessRsDto;
import m3.users.dto.rs.UpdateUserListInfoRsDto;
import m3.users.entities.UserEntity;
import m3.users.mappers.UsersMapper;
import m3.users.repositories.UsersRepository;
import m3.users.services.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static m3.users.commons.ErrorCodes.AUTH_FAILED;

@AllArgsConstructor
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Long DEFAULT_NEXT_POINT_ID = 1L;

    private final UsersRepository usersRepository;
    private final UsersMapper mapper;
    private final SocNetServiceImpl socNet;

    public AuthSuccessRsDto auth(AuthRqDto authRqDto) {

        var authSuccessed = socNet.checkAuth(authRqDto);
        if (!authSuccessed) {
            //@todo class ERRORS
            throw new HttpExceptionError(AUTH_FAILED);
        }
        UserEntity outUser;

        Optional<UserEntity> exitendUser = usersRepository
                .findBySocNetTypeIdAndSocNetUserId(authRqDto.getSocNetType().getId(), authRqDto.getSocNetUserId());

        if (exitendUser.isEmpty()) {
            var currentMills = System.currentTimeMillis();
            var entitiy = mapper.forAuthNewUser(
                    authRqDto,
                    currentMills / 1000,
                    DEFAULT_NEXT_POINT_ID
            );
            outUser = usersRepository.save(entitiy);
        } else {
            outUser = exitendUser.orElseThrow();
            var newLoginTime = System.currentTimeMillis() / 1000;
            updateLogin(outUser.getId(), newLoginTime);
            outUser.setLoginTm(newLoginTime);
        }

        //socNet.getUrl();
        //// var url = SocNet(user.socNetTypeId).getUserProfileUrl(user.socNetUserId);
        //    // Logs.log("🥰 ", Logs.LEVEL_NOTIFY, url, Logs.CHANNEL_TELEGRAM);


        return mapper.entityToAuthSuccessRsDto(outUser);
    }

    private void updateLogin(Long id, Long newLoginTime) {
        usersRepository.updateLogin(id, newLoginTime);
    }


    /**
     * // * Авторизация пользователя из соц сети вКонтакте.
     * // * @param socNetUserId
     * // * @param authParams
     * // * @param cntx
     * //
     */


    public UpdateUserListInfoRsDto getUsers(SendMeUserInfoRqDto rq) {

        List<UserEntity> usersList = usersRepository.findAllByIdIn(rq.getIds());

        var list = usersList.stream()
                .map(user -> {
                    return mapper.entityToDto(user);
                })
                .toList();

        return UpdateUserListInfoRsDto.builder()
                .toUserId(rq.getToUserId())
                .list(list)
                .build();
    }

    public void updateLastLogout(UpdateLastLogoutRqDto rq) {
        usersRepository.updateLastLogout(rq.getUserId(), System.currentTimeMillis());
    }
}