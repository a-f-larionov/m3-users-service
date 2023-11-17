package m3.users.repositories;

import m3.lib.entities.UserEntity;
import m3.lib.repositories.UserRepository;
import m3.users.BaseDataJpaTest;
import m3.users.enums.SocNetType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Commit;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = {
        "logging.level.org.springframework.jdbc.core=DEBUG",
        "logging.level.org.hibernate.SQL=DEBUG"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Commit
public class UserRepositoryTest extends BaseDataJpaTest {

    @Autowired
    private UserRepository userRepository;

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
        int result = userRepository.updateLastLogout(userId, newLogoutTS);

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
        int result = userRepository.updateLogin(userId, newLoginTS);

        // then
        assertEquals(1, result);
        Long login_tm = (long) jdbcTemplate.queryForMap("SELECT login_tm FROM users WHERE id=" + userId).get("login_tm");
        assertEquals(newLoginTS, login_tm);
    }

    @Test
    void testGotMapFriends() {
        // given
        deleteAllUsers();
        jdbcTemplate.update("INSERT INTO users(socNetTypeId, socNetUserId, nextPointId) VALUES(?, ?, ?)", 1, 1, 1);
        jdbcTemplate.update("INSERT INTO users(socNetTypeId, socNetUserId, nextPointId) VALUES(?, ?, ?)", 1, 2, 9);
        jdbcTemplate.update("INSERT INTO users(socNetTypeId, socNetUserId, nextPointId) VALUES(?, ?, ?)", 1, 3, 10);
        jdbcTemplate.update("INSERT INTO users(socNetTypeId, socNetUserId, nextPointId) VALUES(?, ?, ?)", 1, 4, 50);
        jdbcTemplate.update("INSERT INTO users(socNetTypeId, socNetUserId, nextPointId) VALUES(?, ?, ?)", 1, 5, 100);
        jdbcTemplate.update("INSERT INTO users(socNetTypeId, socNetUserId, nextPointId) VALUES(?, ?, ?)", 1, 6, 101);
        jdbcTemplate.update("INSERT INTO users(socNetTypeId, socNetUserId, nextPointId) VALUES(?, ?, ?)", 1, 7, 1000);

        // when
        List<Long> ids = userRepository.gotMapFriends(10L, 100L, List.of(3L, 4L, 5L, 6L, 2L));

        // then
        assertEquals(3, ids.size());
    }


    @Test
    void testFindIdBySocNetTypeIdAndSocNetUserIdIn() {
        // given
        var socNetTypeId = SocNetType.Standalone.getId();
        var requestedSocNetIds = List.of(10L, 20L);
        var otherSocNetIds = List.of(30L, 40L);
        // fill database
        deleteAllUsers();
        var allSocNetIds = new ArrayList<>(requestedSocNetIds);
        allSocNetIds.addAll(otherSocNetIds);
        allSocNetIds.forEach(id -> jdbcTemplate.update("INSERT INTO users(socNetTypeId, socNetUserId) VALUES(?, ?)", socNetTypeId, id));

        // when
        List<Long> result = userRepository.findIdBySocNetTypeIdAndSocNetUserIdIn(socNetTypeId, requestedSocNetIds);

        // then
        assertEquals(requestedSocNetIds.size(), result.size());
    }

    @Test
    void findAllByIdInOrderByNextPointIdDesc() {
        // given
        var existentIds = new ArrayList<Long>();

        deleteAllUsers();
        insertOneUserWithNextPointId(1001, 5);
        existentIds.add(insertOneUserWithNextPointId(1002, 10));
        insertOneUserWithNextPointId(1003, 5);
        existentIds.add(insertOneUserWithNextPointId(1004, 30));
        existentIds.add(insertOneUserWithNextPointId(1005, 20));
        insertOneUserWithNextPointId(1006, 25);
        existentIds.add(insertOneUserWithNextPointId(1007, 50));

        // when
        List<Long> result = userRepository.findAllByIdInOrderByNextPointIdDesc(existentIds, Pageable.ofSize(3))
                .stream().map(UserEntity::getNextPointId).toList();

        // then
        assertEquals(
                List.of(50L, 30L, 20L),
                result
        );
    }

    @Test
    void updateHealth() {
        // given
        // prepare db
        deleteAllUsers();
        var userId = insertOneUser(10001);
        var fullRecoveryTime = 10001L;

        // when
        userRepository.updateHealth(userId, fullRecoveryTime);

        // then
        var actualValue = jdbcTemplate.queryForMap("SELECT fullRecoveryTime FROM users WHERE id = ? ", userId).get("FULLRECOVERYTIME");
        assertEquals(fullRecoveryTime, actualValue);
    }

    private Long insertOneUser(int socNetUserId) {
        return insertOneUserWithNextPointId(socNetUserId, 1);
    }

    private Long insertOneUserWithNextPointId(int socNetUserId, int nextPointId) {
        jdbcTemplate.update("INSERT INTO users (socNetTypeId, socNetUserId, nextPointId) VALUES (1, ?, ? )", socNetUserId, nextPointId);
        return (Long) jdbcTemplate.queryForMap("SELECT MAX(id) as maxId FROM users").get("maxId");
    }

    private void deleteAllUsers() {
        jdbcTemplate.update("DELETE FROM users WHERE create_tm IS NOT NULL OR create_tm IS NULL");
    }
}
