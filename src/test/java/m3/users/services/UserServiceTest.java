package m3.users.services;

import m3.users.commons.ErrorCodes;
import m3.users.commons.HttpExceptionError;
import m3.users.dto.rq.AuthRqDto;
import m3.users.dto.rq.SendMeUserListInfoRqDto;
import m3.users.dto.rq.UpdateLastLogoutRqDto;
import m3.users.dto.rs.AuthSuccessRsDto;
import m3.users.dto.rs.UpdateUserInfoRsDto;
import m3.users.dto.rs.UpdateUserListInfoRsDto;
import m3.users.entities.UserEntity;
import m3.users.enums.SocNetType;
import m3.users.mappers.UsersMapper;
import m3.users.repositories.UsersRepository;
import m3.users.services.impl.SocNetServiceImpl;
import m3.users.services.impl.UserServiceImpl;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private final UsersMapper mapper = Mockito.mock(UsersMapper.class);
    private final UsersRepository repo = Mockito.mock(UsersRepository.class);
    private final SocNetServiceImpl socNet = Mockito.mock(SocNetServiceImpl.class);
    private final UserServiceImpl service = new UserServiceImpl(repo, mapper, socNet);

    @Test
    void authNewUser() {
        // given
        var authRqDto = AuthRqDto.builder()
                .socNetType(SocNetType.VK)
                .socNetUserId(123L)
                .build();
        when(socNet.checkAuth(any())).thenReturn(true);
        when(repo.findBySocNetTypeIdAndSocNetUserId(any(), any())).thenReturn(Optional.empty());

        // when
        AuthSuccessRsDto auth = service.auth(authRqDto);

        // then
        verify(socNet).checkAuth(any());
        verify(repo).findBySocNetTypeIdAndSocNetUserId(authRqDto.getSocNetType().getId(), authRqDto.getSocNetUserId());
        verify(repo).save(any());
    }

    @Test
    void authExistentUser() {
        // given
        var authRqDto = AuthRqDto.builder()
                .socNetType(SocNetType.VK)
                .socNetUserId(123L)
                .build();
        when(socNet.checkAuth(any())).thenReturn(true);
        when(repo.findBySocNetTypeIdAndSocNetUserId(any(), any())).thenReturn(Optional.of(UserEntity.builder().build()));

        // when
        AuthSuccessRsDto auth = service.auth(authRqDto);

        // then
        verify(socNet).checkAuth(any());
        verify(repo).findBySocNetTypeIdAndSocNetUserId(authRqDto.getSocNetType().getId(), authRqDto.getSocNetUserId());
        verify(repo, never()).save(any());
        verify(repo).updateLogin(any(), any());
    }

    @Test
    void authCheckFailed() {
        // given
        var authRqDto = AuthRqDto.builder()
                .build();
        when(socNet.checkAuth(any())).thenReturn(false);

        // when
        ThrowingCallable serviceAuth = () -> service.auth(authRqDto);

        // then
        assertThatThrownBy(serviceAuth)
                .isInstanceOf(HttpExceptionError.class)
                .hasMessage(ErrorCodes.AUTH_FAILED.getMessage());
        verify(socNet).checkAuth(any());
        verifyNoInteractions(repo);
    }


    @Test
    void getUsers() {
        // given
        var toUserId = 1L;
        var ids = List.of(1L, 2L, 3L);
        var rq = SendMeUserListInfoRqDto.builder()
                .toUserId(toUserId)
                .ids(ids)
                .build();
        var oneEntity = createUserEntity(ids.get(0));
        var entities = List.of(oneEntity);

        var rsOne = UpdateUserInfoRsDto.builder()
                .id(oneEntity.getId())
                .build();
        var rs = UpdateUserListInfoRsDto.builder()
                .toUserId(toUserId)
                .list(List.of(rsOne))
                .build();

        when(repo.findAllByIdIn(any())).thenReturn(entities);
        when(mapper.entityToDto(any())).thenReturn(rsOne);

        // when
        var result = service.getUsers(rq);

        // then
        verify(repo).findAllByIdIn(eq(ids));
        verify(mapper).entityToDto(oneEntity);

        assertThat(result)
                .isInstanceOf(UpdateUserListInfoRsDto.class)
                .isEqualTo(rs);
    }

    @Test
    void lastLogout() {
        // given
        var userId = 123L;
        var rq = UpdateLastLogoutRqDto.builder().userId(userId).build();

        // when
        service.updateLastLogout(rq);

        // then
        verify(repo).updateLastLogout(eq(userId), any());
    }

    private UserEntity createUserEntity(Long id) {
        var oneEntity = new UserEntity();
        oneEntity.setId(id);
        oneEntity.setCreateTm(123L);
        oneEntity.setFullRecoveryTime(345L);
        oneEntity.setLoginTm(555L);
        oneEntity.setLogoutTm(666L);
        oneEntity.setNextPointId(55L);
        oneEntity.setSocNetTypeId(SocNetType.VK.getId());
        oneEntity.setSocNetUserId(23L);
        return oneEntity;
    }
}
