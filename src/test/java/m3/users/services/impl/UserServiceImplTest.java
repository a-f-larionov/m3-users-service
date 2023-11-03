package m3.users.services.impl;

import m3.lib.commons.ErrorCodes;
import m3.lib.commons.HttpExceptionError;
import m3.lib.entities.UserEntity;
import m3.lib.repositories.UsersRepository;
import m3.lib.settings.CommonSettings;
import m3.users.dto.rq.*;
import m3.users.dto.rs.*;
import m3.users.enums.SocNetType;
import m3.users.mappers.UsersMapper;
import m3.users.services.HealthService;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

    private final UsersMapper mapper = Mockito.mock(UsersMapper.class);
    private final UsersRepository repo = Mockito.mock(UsersRepository.class);
    private final SocNetServiceImpl socNet = Mockito.mock(SocNetServiceImpl.class);
    private final HealthService healthService = Mockito.mock(HealthService.class);
    private final UserServiceImpl service = new UserServiceImpl(repo, mapper, socNet, healthService);

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
        service.auth(authRqDto);

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
        service.auth(authRqDto);

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
        var rq = SendUserListInfoRqDto.builder()
                .userId(toUserId)
                .ids(ids)
                .build();
        var oneEntity = createUserEntity(ids.get(0));
        var entities = List.of(oneEntity);

        var rsOne = UpdateUserInfoRsDto.builder()
                .id(oneEntity.getId())
                .build();
        var expectedRs = UpdateUserListInfoRsDto.builder()
                .userId(toUserId)
                .list(List.of(rsOne))
                .build();

        when(repo.findAllByIdIn(any())).thenReturn(entities);
        when(mapper.entityToDto(any())).thenReturn(rsOne);

        // when
        var actualRs = service.getUsers(rq.getUserId(), rq.getIds());

        // then
        verify(repo).findAllByIdIn(eq(ids));
        verify(mapper).entityToDto(oneEntity);

        assertThat(actualRs)
                .isInstanceOf(UpdateUserListInfoRsDto.class)
                .isEqualTo(expectedRs);
    }

    @Test
    void lastLogout() {
        // given
        var userId = 123L;
        var rq = UpdateLastLogoutRqDto.builder().userId(userId).build();

        // when
        service.updateLastLogout(rq.getUserId());

        // then
        verify(repo).updateLastLogout(eq(userId), any());
    }

    @Test
    void getMapFriends() {
        // given
        Long toUserId = 123L;
        Long mapId = 10L;
        List<Long> fids = List.of(1L, 2L, 3L);
        List<Long> ids = List.of(1L, 2L);
        var rq = SendMapFriendsRqDto.builder()
                .userId(toUserId)
                .mapId(mapId)
                .fids(fids)
                .build();
        var expectedRs = GotMapFriendIdsRsDto.builder()
                .userId(toUserId)
                .mapId(mapId)
                .ids(ids)
                .build();

        when(repo.gotMapFriends(any(), any(), any())).thenReturn(ids);

        // when
        var actualRs = service.getMapFriends(rq.getUserId(), rq.getMapId(), rq.getFids());

        // then
        verify(repo).gotMapFriends(eq(163L), eq(180L), eq(fids));

        assertThat(actualRs)
                .isInstanceOf(GotMapFriendIdsRsDto.class)
                .isEqualTo(expectedRs);
    }

    @Test
    void getFriendIdsBySocNetRsDtoForVK() {
        // given
        var socNetTypeId = SocNetType.VK.getId();
        var userId = 123L;
        var requestedSocNetIds = List.of(5L, 6L, 7L);
        var rq = createSendFriendIdsBySocNetRqDto(userId, requestedSocNetIds);

        var expectedFids = List.of(1L, 2L, 45L);
        var expectedRs = createGotFriendsIdsRsDto(userId, expectedFids);

        when(repo.findById(any())).thenReturn(createOptionalUserEntity(userId, socNetTypeId));
        when(repo.findIdBySocNetTypeIdAndSocNetUserIdIn(any(), any())).thenReturn(expectedFids);

        // when
        var actualRs = service.getUserIdsFromSocNetIds(rq.getUserId(), rq.getFriendSocNetIds());

        // then
        verify(repo, never()).findAll();
        verify(repo).findById(eq(userId));
        verify(repo).findIdBySocNetTypeIdAndSocNetUserIdIn(eq(socNetTypeId), eq(requestedSocNetIds));

        assertThat(actualRs)
                .isInstanceOf(GotFriendsIdsRsDto.class)
                .isEqualTo(expectedRs);
    }

    @Test
    void getFriendIdsBySocNetRsDtoFoStandalone() {
        // given
        var socNetTypeId = SocNetType.Standalone.getId();
        var userId = 123L;
        var requestedSocNetIds = List.of(5L, 6L, 7L);
        var rq = createSendFriendIdsBySocNetRqDto(userId, requestedSocNetIds);

        var expectedFriendIds = List.of(1L, 2L, 45L);
        var expectedRs = createGotFriendsIdsRsDto(userId, expectedFriendIds);

        when(repo.findById(any())).thenReturn(createOptionalUserEntity(userId, socNetTypeId));
        when(repo.findAll()).thenReturn(createListUserEntityFromIds(expectedFriendIds));

        // when
        var actualRs = service.getUserIdsFromSocNetIds(rq.getUserId(), rq.getFriendSocNetIds());

        // then
        verify(repo, never()).findIdBySocNetTypeIdAndSocNetUserIdIn(any(), any());
        verify(repo).findById(eq(userId));
        verify(repo).findAll();

        assertThat(actualRs)
                .isInstanceOf(GotFriendsIdsRsDto.class)
                .isEqualTo(expectedRs);
    }

    @Test
    void sendTopUsersRsDto() {
        // given
        var userId = 123L;

        long expectedUserId = 1L;
        List<UpdateUserInfoRsDto> usersList = List.of(UpdateUserInfoRsDto.builder().id(expectedUserId).build());
        var rq = SendTopUsersRqDto.builder().userId(userId).fids(List.of(expectedUserId)).build();
        var expectedRs = GotTopUsersRsDto.builder().userId(userId).users(usersList).build();
        when(repo.findAllByIdInOrderByNextPointIdDesc(any(), any())).thenReturn(List.of(UserEntity.builder().id(expectedUserId).build()));
        when(mapper.entityToDto(any())).thenReturn(UpdateUserInfoRsDto.builder().id(expectedUserId).build());

        // when
        var actualRs = service.getTopUsersRsDto(rq.getUserId(), rq.getFids());

        // then
        verify(repo).findAllByIdInOrderByNextPointIdDesc(eq(rq.getFids()), eq(Pageable.ofSize(CommonSettings.TOP_USERS_LIMIT)));
        assertThat(actualRs)
                .isInstanceOf(GotTopUsersRsDto.class)
                .isEqualTo(expectedRs);
    }

    @Test
    void healthBack() {
        // given
        var userId = 123L;
        var rq = HealthBackRqDto.builder().userId(userId).build();
        var fullRecoveryTime = (System.currentTimeMillis() / 1000L) + 100;
        var expectedRs = SetOneHealthHideRsDto.builder()
                .userId(userId)
                .fullRecoveryTime(fullRecoveryTime)
                .oneHealthHide(false)
                .build();
        when(repo.findById(any())).thenReturn(Optional.of(UserEntity.builder()
                .id(userId)
                .fullRecoveryTime(fullRecoveryTime)
                .build()));

        // when
        var actualRs = service.healthUp(rq.getUserId());

        // then
        verify(repo).findById(userId);
        verify(healthService).isMaxHealths(any());
        verify(healthService).setHealths(any(), any());
        verify(healthService).getHealths(any());
        verify(repo).updateHealth(eq(userId), any());

        assertThat(actualRs)
                .isInstanceOf(SetOneHealthHideRsDto.class)
                .isEqualTo(expectedRs);
    }

    @Test
    void healthDown() {
        // given
        var userId = 123L;
        var rq = HealthBackRqDto.builder().userId(userId).build();
        var fullRecoveryTime = (System.currentTimeMillis() / 1000L) + 100;
        var expectedRs = SetOneHealthHideRsDto.builder()
                .userId(userId)
                .fullRecoveryTime(fullRecoveryTime)
                .oneHealthHide(true)
                .build();
        when(repo.findById(any())).thenReturn(Optional.of(UserEntity.builder()
                .id(userId)
                .fullRecoveryTime(fullRecoveryTime)
                .build()));
        when(healthService.getHealths(any())).thenReturn(3L);

        // when
        var actualRs = service.healthDown(rq.getUserId());

        // then
        verify(repo).findById(userId);
        verify(healthService).setHealths(any(), any());
        verify(healthService, times(2)).getHealths(any());
        verify(repo).updateHealth(eq(userId), any());

        assertThat(actualRs)
                .isInstanceOf(SetOneHealthHideRsDto.class)
                .isEqualTo(expectedRs);
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

    private static List<UserEntity> createListUserEntityFromIds(List<Long> ids) {
        return ids.stream().map(id -> UserEntity.builder().id(id).build()).toList();
    }

    private static Optional<UserEntity> createOptionalUserEntity(long userId, Long socNetTypeId) {
        return Optional.of(UserEntity.builder()
                .socNetUserId(userId)
                .socNetTypeId(socNetTypeId)
                .build());
    }

    private static GotFriendsIdsRsDto createGotFriendsIdsRsDto(long userId, List<Long> expectedFids) {
        return GotFriendsIdsRsDto.builder().userId(userId).fids(expectedFids).build();
    }

    private static SendFriendIdsBySocNetRqDto createSendFriendIdsBySocNetRqDto(long userId, List<Long> friendSocNetIds) {
        return SendFriendIdsBySocNetRqDto.builder().userId(userId).friendSocNetIds(friendSocNetIds).build();
    }
}
