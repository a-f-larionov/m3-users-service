package m3.users.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = { "classpath:/application-test.yml" })
public class UsersRepositoryTest {

  @Autowired
  private UsersRepository usersRepository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void testUpdateLastLogout() {
    // given
    jdbcTemplate.update("INSERT INTO users (socNetTypeId, socNetUserId) VALUES (?,?)", 1, 1);
    jdbcTemplate.update("INSERT INTO users (socNetTypeId, socNetUserId) VALUES (?,?)", 1, 2);
    var userId = (Long) jdbcTemplate.queryForMap("SELECT MAX(id) as maxId FROM users").get("maxId");
    var tm = System.currentTimeMillis() / 1000;

    // when
    int result = usersRepository.updateLastLogout(userId, tm);

    // then
    assertEquals(1, result);
    Map<String, Object> queryResult = jdbcTemplate.queryForMap("SELECT logout_tm FROM users WHERE id=" + userId);
    assertTrue((long) (queryResult.get("logout_tm")) >= tm);
  }

}
