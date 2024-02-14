package m3.users.services.functional;

import m3.lib.dto.rs.UpdateUserInfoRsDto;
import m3.lib.enums.SocNetType;
import m3.lib.settings.CommonSettings;
import m3.lib.settings.MapSettings;
import m3.users.BaseSpringBootTest;
import m3.users.dto.rq.AuthRqDto;
import m3.users.dto.rq.SendMapFriendsRqDto;
import m3.users.dto.rs.*;
import m3.users.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest(properties = {
        "logging.level.org.hibernate.SQL=TRACE"
})
public class UserServiceFuncTest extends BaseSpringBootTest {

    @Autowired
    UserService userService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    public void authNewUser() {
        // given
        var testStart = System.currentTimeMillis() / 1000;
        long socNetUserId = getMaxSocNetUserId() + 1;
        var authRqDto = createNewAuthRqDto(socNetUserId);

        // when
        var rsDto = userService.auth(authRqDto);

        // then
        assertThat(rsDto.getId()).isNotNull();
        assertThat(rsDto.getSocNetTypeId()).isEqualTo(authRqDto.getSocNetType().getId());
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
        var rqDto = createNewAuthRqDto(socNetUserId);
        var created = userService.auth(rqDto);
        jdbcTemplate.update("UPDATE users SET create_tm = " + (beforeCreateTm - 1000) + " WHERE id=" + created.getId());
        jdbcTemplate.update("UPDATE users SET login_tm = " + (beforeCreateTm - 1000) + " WHERE id=" + created.getId());
        created.setCreateTm(beforeCreateTm - 1000);
        created.setLoginTm(beforeCreateTm - 1000);

        // when
        var rsDto = userService.auth(rqDto);

        // then
        assertThat(rsDto.getId()).isEqualTo(created.getId());
        assertThat(rsDto.getSocNetTypeId()).isEqualTo(rqDto.getSocNetType().getId());
        assertThat(rsDto.getSocNetUserId()).isEqualTo(rqDto.getSocNetUserId());
        assertThat(rsDto.getCreateTm()).isEqualTo(created.getCreateTm());
        assertThat(rsDto.getLoginTm()).isGreaterThanOrEqualTo(beforeCreateTm);
        assertThat(rsDto.getLoginTm()).isNotEqualTo(created.getLoginTm());
        assertThat(rsDto.getLogoutTm()).isNull();
        assertThat(rsDto.getFullRecoveryTime()).isEqualTo(created.getFullRecoveryTime());
        assertThat(rsDto.getNextPointId()).isEqualTo(1);

        assertRsDtoEqualsDBState(rsDto, rqDto.getConnectionId());
    }

    @Test
    public void getUsers() {
        // given
        var userId = 123L;
        // prepare server state
        deleteAllUsers();
        var userId1 = userService.auth(createNewAuthRqDto(1001L)).getId();
        var userId2 = userService.auth(createNewAuthRqDto(1002L)).getId();
        userService.auth(createNewAuthRqDto(1003L));
        userService.auth(createNewAuthRqDto(1004L));

        // when
        UpdateUserListInfoRsDto rsDto = userService.getUsers(userId, List.of(userId1, userId2));

        // then
        assertThat(rsDto.getUserId()).isEqualTo(userId);
        rsDto.getList().forEach(this::assertRsDtoEqualsDBState);
    }

    @Test
    public void updateLastLogout() {
        // given
        var user = userService.auth(createNewAuthRqDto(123232L));
        var userId = user.getId();
        jdbcTemplate.update("UPDATE users SET logout_tm = ? WHERE id=? ", 100, userId);

        // when
        userService.updateLastLogout(userId);

        // then
        Map<String, Object> userDb = jdbcTemplate.queryForMap("SELECT * FROM users WHERE id = ?", userId);

        assertThat((Long) userDb.get("LOGOUT_TM"))
                .isNotNull()
                .isBetween(
                        System.currentTimeMillis() / 1000 - 1000,
                        System.currentTimeMillis() / 1000 + 1000
                );
    }

    @Test
    public void getMapFriends() {
        // given
        Long socNetUserId = 123L;
        Long mapId = 2L;
        var firstPointId = MapSettings.getFirstPointId(mapId);
        var lastPointId = MapSettings.getLastPointId(mapId);
        var friendOnMapId1 = 10001L;
        var friendOnMapId2 = 10002L;
        var friendOutsideMapId1 = 10003L;
        var friendOutsideMapId2 = 10004L;
        var friendsSocNetIds = List.of(friendOnMapId1, friendOnMapId2, friendOutsideMapId1, friendOutsideMapId2);
        var friendsOnMapIds = new ArrayList<Long>();

        // preparing
        deleteAllUsers();
        var userId = userService.auth(createNewAuthRqDto(socNetUserId)).getId();
        // friend on map left edge
        friendsOnMapIds.add(createUserAndSetPointId(friendOnMapId1, firstPointId));
        // friend on map right ede
        friendsOnMapIds.add(createUserAndSetPointId(friendOnMapId2, lastPointId));
        // friend outside map left
        createUserAndSetPointId(friendOutsideMapId1, firstPointId - 1);
        // friend outside map right
        createUserAndSetPointId(friendOutsideMapId2, lastPointId + 1);

        // other on map left edge
        createUserAndSetPointId(20001L, firstPointId);
        // other on map right edge
        createUserAndSetPointId(20002L, lastPointId);
        // other outside map left
        createUserAndSetPointId(20003L, firstPointId - 1);
        // other outside map right
        createUserAndSetPointId(20003L, lastPointId + 1);


        var rq = SendMapFriendsRqDto.builder().userId(userId).fids(friendsSocNetIds).mapId(mapId).build();
        var expectedRs = GotMapFriendIdsRsDto.builder().userId(userId).ids(friendsOnMapIds).mapId(mapId).build();

        // when
        var mapFriends = userService.getMapFriends(rq.getUserId(), rq.getMapId(), rq.getFids());

        // then
        assertThat(mapFriends)
                .isInstanceOf(GotMapFriendIdsRsDto.class)
                .isEqualTo(expectedRs);
    }

    @Test
    public void getUserIdsFromSocNetIds() {
        // given
        Long socNetUserId = 123L;
        Long mapId = 2L;
        var firstPointId = MapSettings.getFirstPointId(mapId);
        var lastPointId = MapSettings.getLastPointId(mapId);
        var friendOnMapId1 = 10001L;
        var friendOnMapId2 = 10002L;
        var friendOutsideMapId1 = 10003L;
        var friendOutsideMapId2 = 10004L;
        var friendsSocNetIds = List.of(friendOnMapId1, friendOnMapId2, friendOutsideMapId1, friendOutsideMapId2);
        var allFriendIds = new ArrayList<Long>();

        // preparing
        deleteAllUsers();
        var userId = userService.auth(createNewAuthRqDto(socNetUserId)).getId();
        // friend on map left edge
        allFriendIds.add(createUserAndSetPointId(friendOnMapId1, firstPointId));
        // friend on map right ede
        allFriendIds.add(createUserAndSetPointId(friendOnMapId2, lastPointId));
        // friend outside map left
        allFriendIds.add(createUserAndSetPointId(friendOutsideMapId1, firstPointId - 1));
        // friend outside map right
        allFriendIds.add(createUserAndSetPointId(friendOutsideMapId2, lastPointId + 1));

        // other on map left edge
        createUserAndSetPointId(20001L, firstPointId);
        // other on map right edge
        createUserAndSetPointId(20002L, lastPointId);
        // other outside map left
        createUserAndSetPointId(20003L, firstPointId - 1);
        // other outside map right
        createUserAndSetPointId(20003L, lastPointId + 1);

        // when
        var rs = userService.getUserIdsFromSocNetIds(userId, friendsSocNetIds);

        // then
        assertThat(rs).isInstanceOf(GotFriendsIdsRsDto.class);
        assertThat(rs.getUserId()).isEqualTo(userId);
        assertThat(rs.getFids()).hasSameElementsAs(allFriendIds);
    }

    @Test
    public void sendTopUsersRsDto() {
        // given
        var userId = 1L;
        List<UpdateUserInfoRsDto> usersList = new ArrayList<>();
        var expectedRs = GotTopUsersRsDto.builder()
                .userId(userId)
                .users(usersList)
                .build();

        // Unordered order of nextPointId!
        deleteAllUsers();
        createUserAndSetPointId(1001L, 10L);
        usersList.add(createUserAndSetPointIdAndReturnRsDto(1101L, 30L));
        createUserAndSetPointId(1002L, 25L);
        usersList.add(createUserAndSetPointIdAndReturnRsDto(1102L, 10L));
        usersList.add(createUserAndSetPointIdAndReturnRsDto(1103L, 20L));
        createUserAndSetPointId(1003L, 40L);

        //  when
        List<Long> requestIds = usersList.stream().map(UpdateUserInfoRsDto::getId).toList();
        usersList.sort(Comparator.comparing(UpdateUserInfoRsDto::getNextPointId).reversed());
        GotTopUsersRsDto topUsersRsDto = userService.getTopUsersRsDto(userId, requestIds);

        //then
        assertThat(topUsersRsDto.getUserId()).isEqualTo(expectedRs.getUserId());
        assertThat(topUsersRsDto.getUsers()).isEqualTo(expectedRs.getUsers());
    }

    @Test
    public void sendTopUsersRsDtoTopTest() {
        // given
        var userId = 123L;
        var requestedIds = new ArrayList<Long>();

        deleteAllUsers();
        for (int i = 0; i < 10; i++) {
            requestedIds.add(createUser(1000L + i));
        }

        // when
        GotTopUsersRsDto topUsersRsDto = userService.getTopUsersRsDto(userId, requestedIds);

        // then
        assertThat(topUsersRsDto.getUsers()).hasSize(CommonSettings.TOP_USERS_LIMIT);
    }

    @Test
    public void heathUp() {
        // given
        deleteAllUsers();
        var userId = createUser(1000L);
        var maxHealths = CommonSettings.HEALTH_MAX;
        // set 0 health
        jdbcTemplate.update("UPDATE users SET fullRecoveryTime = ? WHERE id = ? ",
                (System.currentTimeMillis() / 1000L) + CommonSettings.HEALTH_RECOVERY_TIME * maxHealths
                , userId);

        // when
        SetOneHealthHideRsDto actualRs = userService.healthUp(userId);

        // then
        assertThat(actualRs.getUserId()).isEqualTo(userId);
        assertThat(actualRs.getFullRecoveryTime()).isCloseTo(
                (System.currentTimeMillis() / 1000L) + CommonSettings.HEALTH_RECOVERY_TIME * (maxHealths - 1),
                within(1L)
        );
    }

    @Test
    public void healthDown() {
        // given
        deleteAllUsers();
        var userId = createUser(1000L);
        var pointId = 123L;

        jdbcTemplate.update("UPDATE users SET fullRecoveryTime = ? WHERE id = ? ",
                (System.currentTimeMillis() / 1000L) + CommonSettings.HEALTH_RECOVERY_TIME * 2
                , userId);

        // when
        SetOneHealthHideRsDto actualRs = userService.healthDown(userId, pointId);

        // then
        assertThat(actualRs.getUserId()).isEqualTo(userId);
        assertThat(actualRs.getFullRecoveryTime()).isCloseTo(
                (System.currentTimeMillis() / 1000L) + CommonSettings.HEALTH_RECOVERY_TIME * 3,
                within(1L)
        );
    }

    private UpdateUserInfoRsDto createUserAndSetPointIdAndReturnRsDto(Long socNetId, Long nextPointId) {
        var userId = createUserAndSetPointId(socNetId, nextPointId);
        return UpdateUserInfoRsDto.builder()
                .id(userId)
                .userId(userId)
                .socNetUserId(socNetId)
                .nextPointId(nextPointId)
                .fullRecoveryTime((Long) jdbcTemplate.queryForMap("SELECT fullRecoveryTime FROM users WHERE id= ? ", userId).get("FULLRECOVERYTIME"))
                .build();
    }

    private void assertRsDtoEqualsDBState(AuthSuccessRsDto rsDto, Long connectionId) {
        Map<String, Object> userDb = jdbcTemplate.queryForMap("SELECT * FROM users WHERE id = ?", rsDto.getId());

        assertThat(rsDto.getId()).isEqualTo(userDb.get("ID"));
        assertThat(rsDto.getSocNetTypeId()).isEqualTo(userDb.get("SOCNETTYPEID"));
        assertThat(rsDto.getSocNetUserId()).isEqualTo(userDb.get("SOCNETUSERID"));
        assertThat(rsDto.getCreateTm()).isEqualTo(userDb.get("CREATE_TM"));
        assertThat(rsDto.getLoginTm()).isEqualTo(userDb.get("LOGIN_TM"));
        assertThat(rsDto.getLogoutTm()).isEqualTo(userDb.get("LOGOUT_TM"));
        assertThat(rsDto.getNextPointId()).isEqualTo(userDb.get("NEXTPOINTID"));
        assertThat(rsDto.getFullRecoveryTime()).isEqualTo(userDb.get("FULLRECOVERYTIME"));
        assertThat(rsDto.getConnectionId()).isEqualTo(connectionId);
    }

    private AuthRqDto createNewAuthRqDto(Long socNetUserId) {
        var appId = 123L;
        var secretKey = "vk_secret_key";
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
        return DigestUtils.md5DigestAsHex((appId.toString() + "_" + socNetUserId.toString() + "_" + secretKey).getBytes());
    }

    private void assertRsDtoEqualsDBState(UpdateUserInfoRsDto rsDto) {
        Map<String, Object> userDb = jdbcTemplate.queryForMap("SELECT id, nextPointId, socNetUserId, fullRecoveryTime FROM users WHERE id = ?", rsDto.getId());

        assertThat(rsDto.getId()).isEqualTo(userDb.get("ID"));
        assertThat(rsDto.getNextPointId()).isEqualTo(userDb.get("NEXTPOINTID"));
        assertThat(rsDto.getSocNetUserId()).isEqualTo(userDb.get("SOCNETUSERID"));
        assertThat(rsDto.getFullRecoveryTime()).isEqualTo(userDb.get("FULLRECOVERYTIME"));
    }

    private Long createUser(Long socNetUserId) {
        return createUserAndSetPointId(socNetUserId, 1L);
    }

    private Long createUserAndSetPointId(Long socNetId, Long nextPointId) {
        var userId = userService.auth(createNewAuthRqDto(socNetId)).getId();
        jdbcTemplate.update("UPDATE users SET nextPointId = ? WHERE id = ? ", nextPointId, userId);
        return userId;
    }

    private void deleteAllUsers() {
        jdbcTemplate.update("DELETE FROM users WHERE create_tm IS NOT NULL OR create_tm IS NULL");
    }
}
