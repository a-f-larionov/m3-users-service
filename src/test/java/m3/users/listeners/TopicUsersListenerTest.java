package m3.users.listeners;

import m3.lib.enums.SocNetType;
import m3.users.dto.rq.*;
import m3.users.dto.rs.AuthSuccessRsDto;
import m3.users.dto.rs.GotFriendsIdsRsDto;
import m3.users.dto.rs.GotMapFriendIdsRsDto;
import m3.users.dto.rs.UpdateUserListInfoRsDto;
import m3.users.services.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TopicUsersListenerTest {

    private final UserServiceImpl service = mock(UserServiceImpl.class);

    private final TopicUsersListener listener = new TopicUsersListener(service);

    //@todo  check it! Most of these are self-explanatory, but the one we should highlight is the consumer property auto-offset-reset: earliest.
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
        var rq = new SendUserListInfoRqDto();
        var rs = new UpdateUserListInfoRsDto();
        when(service.getUsers(any(), any())).thenReturn(rs);

        // when
        var result = listener.sendUserListInfo(rq);

        // then
        verify(service).getUsers(eq(rq.getUserId()), eq(rq.getIds()));
        assertThat(result).isInstanceOf(rs.getClass()).isEqualTo(rs);
    }

    @Test
    void sendMeMapFriends() {
        // given
        var rq = SendMapFriendsRqDto.builder().build();
        var rs = GotMapFriendIdsRsDto.builder().build();
        when(service.getMapFriends(any(), any(), any())).thenReturn(rs);

        // when
        GotMapFriendIdsRsDto result = listener.sendMapFriends(rq);

        // then
        verify(service).getMapFriends(eq(rq.getUserId()), eq(rq.getMapId()), eq(rq.getFids()));
        assertThat(result).isInstanceOf(GotMapFriendIdsRsDto.class).isEqualTo(rs);
    }

    @Test
    void updateLastLogout() {
        // given
        var rq = UpdateLastLogoutRqDto.builder().build();

        // when
        listener.updateLastLogout(rq);

        // then
        verify(service).updateLastLogout(eq(rq.getUserId()));
    }

    @Test
    void sendFriendIdsBySocNet() {
        // given
        var rq = SendFriendIdsBySocNetRqDto.builder().build();
        var rs = GotFriendsIdsRsDto.builder().build();
        when(service.getUserIdsFromSocNetIds(any(), any())).thenReturn(rs);

        // when
        var result = listener.sendFriendIdsBySocNet(rq);

        // then
        verify(service).getUserIdsFromSocNetIds(eq(rq.getUserId()), eq(rq.getFriendSocNetIds()));
        assertThat(rs).isInstanceOf(GotFriendsIdsRsDto.class).isEqualTo(result);
    }

    @Test
    void sendTopUsers() {
        // given
        var rq = SendTopUsersRqDto.builder().build();

        // when
        listener.sendTopUsers(rq);

        // then
        verify(service).getTopUsersRsDto(eq(rq.getUserId()), eq(rq.getFids()));
    }

    @Test
    void healthUp() {
        // given
        var rq = HealthBackRqDto.builder().build();

        // when
        listener.healthUp(rq);

        // then
        verify(service).healthUp(eq(rq.getUserId()));
    }

    @Test
    void healthDown() {
        // given
        var rq = HealthDownRqDto.builder()
                .userId(123L)
                .pointId(345L)
                .build();

        // when
        listener.healthDown(rq);

        // then
        verify(service).healthDown(eq(rq.getUserId()), eq(rq.getPointId()));
    }

    private AuthSuccessRsDto createAuthSuccessRsDto() {
        return AuthSuccessRsDto.builder()
                .id(1L)
                .nextPointId(10L)
                .socNetTypeId(SocNetType.VK.getId())
                .socNetUserId(34343343L)
                .createTm(123L)
                .loginTm(345L)
                .build();
    }
}
