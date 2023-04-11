package m3.users.services;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import m3.users.repositories.UsersRepository;

public class UserServiceTest {

    private UsersRepository usersRepository = Mockito.mock(UsersRepository.class);

    private UserService userService = new UserService(usersRepository);

    @Test
    void testGetUsers() {
        // given
        var ids = List.of(1L, 2L, 3L);

        // when
        userService.getUsers(ids);

        // thenf
        verify(usersRepository)
                .findAllByIdIn(eq(ids));

    }

    @Test
    void testUpdateLastLogout() {
        // given
        var userId = 1L;

        // when
        userService.updateLastLogout(userId);

        // then
        verify(usersRepository)
                .updateLastLogout(eq(userId), anyLong());
    }
}
