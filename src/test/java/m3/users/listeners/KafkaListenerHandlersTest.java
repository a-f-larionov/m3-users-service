package m3.users.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import m3.users.dto.rq.SendMeMapFriendsRqDto;
import m3.users.enums.SocNetType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import m3.users.dto.rq.AuthRqDto;
import m3.users.dto.rq.SendMeUserListInfoRqDto;
import m3.users.dto.rq.UpdateLastLogoutRqDto;
import m3.users.dto.rs.AuthSuccessRsDto;
import m3.users.dto.rs.UpdateUserListInfoRsDto;
import m3.users.services.impl.UserServiceImpl;

public class KafkaListenerHandlersTest {

    private final UserServiceImpl service = Mockito.mock(UserServiceImpl.class);

    private final KafkaListenerHandlers listener = new KafkaListenerHandlers(service);

    @Test
    void auth() {
        // given
        var rq = new AuthRqDto();
        var rs = createAuthSuccessRsDto();
        when(service.auth(any())).thenReturn(rs);

        // when
        var actualRs = listener.auth(rq);

        // then
        verify(service).auth(eq(rq));
        assertThat(actualRs)
                .isInstanceOf(AuthSuccessRsDto.class)
                .isEqualTo(rs);
    }

    @Test
    void sendMeUserListInfo() {
        // given
        var rq = new SendMeUserListInfoRqDto();
        var rs = new UpdateUserListInfoRsDto();
        when(service.getUsers(any())).thenReturn(rs);

        // when
        var result = listener.sendMeUserListInfo(rq);

        // then
        verify(service).getUsers(eq(rq));
        assertThat(result).isInstanceOf(rs.getClass()).isEqualTo(rs);
    }

    @Test
    void lastLogout() {
        // given
        var rq = UpdateLastLogoutRqDto.builder().build();

        // when
        listener.updateLastLogout(rq);

        // then
        verify(service).updateLastLogout(eq(rq));
    }

    @Test
    void sendMeMapFriends() {
        // given
        var rq = SendMeMapFriendsRqDto.builder().build();

        // when
        listener.sendMeMapFriends(rq);

        // then
        verify(service).getMapFriends(eq(rq));
    }

    private AuthSuccessRsDto createAuthSuccessRsDto() {
        return AuthSuccessRsDto.builder()
                .userId(1L)
                .nextPointId(10L)
                .socNetType(SocNetType.VK)
                .socNetUserId(34343343L)
                .createTm(123L)
                .loginTm(345L)
                .build();
    }
}
