package m3.users.repositories;

import m3.users.BaseDataJpaTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Commit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = {
        "logging.level.org.springframework.jdbc.core=DEBUG",
        "logging.level.org.hibernate.SQL=DEBUG"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Commit
public class UsersRepositoryTest extends BaseDataJpaTest {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testUpdateLastLogout() {
        // given
        jdbcTemplate.update("INSERT INTO users (socNetTypeId, socNetUserId) VALUES (?, ?)", 1, 1);
        jdbcTemplate.update("INSERT INTO users (socNetTypeId, socNetUserId) VALUES (?, ?)", 1, 2);
        var userId = (Long) jdbcTemplate.queryForMap("SELECT MAX(id) as maxId FROM users").get("maxId");
        var newLogoutTS = System.currentTimeMillis() / 1000;

        // when
        int result = usersRepository.updateLastLogout(userId, newLogoutTS);

        // then
        assertEquals(1, result);
        Long logout_tm = (long) jdbcTemplate.queryForMap("SELECT logout_tm FROM users WHERE id=" + userId).get("logout_tm");
        assertEquals(newLogoutTS, logout_tm);
    }

    @Test
    void testUpdateLogin() {
        // given
        jdbcTemplate.update("INSERT INTO users (socNetTypeId, socNetUserId) VALUES (?, ?)", 1, 1);
        jdbcTemplate.update("INSERT INTO users (socNetTypeId, socNetUserId) VALUES (?, ?)", 1, 2);
        var userId = (Long) jdbcTemplate.queryForMap("SELECT MAX(id) as maxId FROM users").get("maxId");
        var newLoginTS = System.currentTimeMillis() / 1000;

        // when
        int result = usersRepository.updateLogin(userId, newLoginTS);

        // then
        assertEquals(1, result);
        Long login_tm = (long) jdbcTemplate.queryForMap("SELECT login_tm FROM users WHERE id=" + userId).get("login_tm");
        assertEquals(newLoginTS, login_tm);
    }

    @Test
    void testGotMapFriends() {
        // given
        jdbcTemplate.update("DELETE FROM users ");
        jdbcTemplate.update("INSERT INTO users(socNetTypeId, socNetUserId, nextPointId) VALUES(?, ?, ?)", 1, 1, 1);
        jdbcTemplate.update("INSERT INTO users(socNetTypeId, socNetUserId, nextPointId) VALUES(?, ?, ?)", 1, 1, 9);
        jdbcTemplate.update("INSERT INTO users(socNetTypeId, socNetUserId, nextPointId) VALUES(?, ?, ?)", 1, 2, 10);
        jdbcTemplate.update("INSERT INTO users(socNetTypeId, socNetUserId, nextPointId) VALUES(?, ?, ?)", 1, 2, 50);
        jdbcTemplate.update("INSERT INTO users(socNetTypeId, socNetUserId, nextPointId) VALUES(?, ?, ?)", 1, 3, 100);
        jdbcTemplate.update("INSERT INTO users(socNetTypeId, socNetUserId, nextPointId) VALUES(?, ?, ?)", 1, 3, 101);
        jdbcTemplate.update("INSERT INTO users(socNetTypeId, socNetUserId, nextPointId) VALUES(?, ?, ?)", 1, 3, 1000);

        // when
        List<Long> ids = usersRepository.gotMapFriends(10L, 100L, List.of(1L, 2L, 3L));

        // then
        assertEquals(3, ids.size());
    }
}
