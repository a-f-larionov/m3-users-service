package m3.users.services.functional;

import m3.users.dto.rq.AuthRqDto;
import m3.users.dto.rs.AuthSuccessRsDto;
import m3.users.enums.SocNetType;
import m3.users.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.DigestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "logging.level.org.hibernate.SQL=TRACE")
public class UserServiceFuncTest {

    @Autowired
    UserService userService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    public void sendMeUserListInfo() {
        // given
        // when
        // then
    }

    @Test
    public void authNewUser() {
        // given
        var testStart = System.currentTimeMillis() / 1000;
        long socNetUserId = getMaxSocNetUserId() + 1;
        var authRqDto = createNewAuthRqDto(socNetUserId);

        // when
        var rsDto = userService.auth(authRqDto);

        // then
        assertThat(rsDto.getUserId()).isNotNull();
        assertThat(rsDto.getSocNetType()).isEqualTo(authRqDto.getSocNetType());
        assertThat(rsDto.getSocNetUserId()).isEqualTo(authRqDto.getSocNetUserId());
        assertThat(rsDto.getCreateTm()).isGreaterThanOrEqualTo(testStart);
        assertThat(rsDto.getLoginTm()).isGreaterThanOrEqualTo(testStart);
        assertThat(rsDto.getLogoutTm()).isNull();
        assertThat(rsDto.getNextPointId()).isEqualTo(1);
        assertThat(rsDto.getFullRecoveryTime()).isGreaterThanOrEqualTo(testStart);

        assertRsDtoEqualsDBState(rsDto, authRqDto.getConnectionId());
    }


    @Test
    public void authExistingUser() {
        // given
        var beforeCreateTm = System.currentTimeMillis() / 1000;
        var socNetUserId = getMaxSocNetUserId() + 1;
        var authRqDto = createNewAuthRqDto(socNetUserId);
        var created = userService.auth(authRqDto);
        jdbcTemplate.update("UPDATE users SET create_tm = " + (beforeCreateTm - 1000) + " WHERE id=" + created.getUserId());
        jdbcTemplate.update("UPDATE users SET login_tm = " + (beforeCreateTm - 1000) + " WHERE id=" + created.getUserId());
        created.setCreateTm(beforeCreateTm - 1000);
        created.setLoginTm(beforeCreateTm - 1000);

        // when
        var rsDto = userService.auth(authRqDto);

        // then
        assertThat(rsDto.getUserId()).isEqualTo(created.getUserId());
        assertThat(rsDto.getSocNetType()).isEqualTo(authRqDto.getSocNetType());
        assertThat(rsDto.getSocNetUserId()).isEqualTo(authRqDto.getSocNetUserId());
        assertThat(rsDto.getCreateTm()).isEqualTo(created.getCreateTm());
        assertThat(rsDto.getLoginTm()).isGreaterThanOrEqualTo(beforeCreateTm);
        assertThat(rsDto.getLoginTm()).isNotEqualTo(created.getLoginTm());
        assertThat(rsDto.getLogoutTm()).isNull();
        assertThat(rsDto.getFullRecoveryTime()).isEqualTo(created.getFullRecoveryTime());
        assertThat(rsDto.getNextPointId()).isEqualTo(1);

        assertRsDtoEqualsDBState(rsDto, authRqDto.getConnectionId());
    }

    private void assertRsDtoEqualsDBState(AuthSuccessRsDto rsDto, Long connectionId) {
        Map<String, Object> userDb = jdbcTemplate.queryForMap("SELECT * FROM users WHERE id = ?", rsDto.getUserId());

        assertThat(rsDto.getUserId()).isEqualTo(userDb.get("ID"));
        assertThat(rsDto.getSocNetType().getId()).isEqualTo(userDb.get("SOCNETTYPEID"));
        assertThat(rsDto.getSocNetUserId()).isEqualTo(userDb.get("SOCNETUSERID"));
        assertThat(rsDto.getCreateTm()).isEqualTo(userDb.get("CREATE_TM"));
        assertThat(rsDto.getLoginTm()).isEqualTo(userDb.get("LOGIN_TM"));
        assertThat(rsDto.getLogoutTm()).isEqualTo(userDb.get("LOGOUT_TM"));
        assertThat(rsDto.getNextPointId()).isEqualTo(userDb.get("NEXTPOINTID"));
        assertThat(rsDto.getFullRecoveryTime()).isEqualTo(userDb.get("FULLRECOVERYTIME"));
        assertThat(rsDto.getConnectionId()).isEqualTo(connectionId);
    }

    private AuthRqDto createNewAuthRqDto(Long socNetUserId) {
        var secretKey = "123";
        var appId = 123L;
        return AuthRqDto.builder()
                .socNetType(SocNetType.VK)
                .socNetUserId(socNetUserId)
                .authKey(calcAuthKey(socNetUserId, appId, secretKey))
                .appId(appId)
                .connectionId(123L)
                .build();
    }

    private long getMaxSocNetUserId() {
        return (long) jdbcTemplate.queryForMap("SELECT MAX(socNetUserId) maxSocNetUserId FROM users").get("maxSocNetUserId");
    }

    private String calcAuthKey(Long socNetUserId, Long appId, String secretKey) {
        return DigestUtils.md5DigestAsHex((appId.toString() + socNetUserId.toString() + secretKey).getBytes());
    }
}
