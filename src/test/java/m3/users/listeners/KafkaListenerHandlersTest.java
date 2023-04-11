package m3.users.listeners;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import m3.users.dto.rq.SendMeUserInfoRqDto;
import m3.users.dto.rq.UpdateLastLogoutRqDto;
import m3.users.dto.rs.UpdateUserListInfoRsDto;
import m3.users.mappers.UsersMapper;
import m3.users.services.UserService;

public class KafkaListenerHandlersTest {

    private UserService userService = Mockito.mock(UserService.class);
    private UsersMapper usersMapper = Mockito.mock(UsersMapper.class);

    private KafkaListenerHandlers kafka = new KafkaListenerHandlers(userService, usersMapper);

    @Test
    void sendMeUserInfo() {
        // given
        var toUserId = 1L;
        var ids = List.of(1L, 2L, 3L);
        var dto = SendMeUserInfoRqDto.builder()
                .toUserId(toUserId)
                .ids(ids)
                .build();
        when(userService.getUsers(any())).thenReturn(emptyList());

        // when
        var result = kafka.sendMeUserInfo(dto);

        // then
        verify(userService).getUsers(eq(ids));

        assertThat(result)
                .isInstanceOf(UpdateUserListInfoRsDto.class);
        assertThat(result.getToUserId()).isEqualTo(toUserId);
        assertThat(result.getList()).hasSize(0);
    }

    @Test
    void lastLogout() {
        // given
        var userId = 1L;
        var dto = UpdateLastLogoutRqDto.builder()
                .userId(userId)
                .build();

        // when
        kafka.updateLastLogout(dto);

        // then
        verify(userService)
                .updateLastLogout(eq(userId));
    }
}
