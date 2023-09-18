package m3.users.services;

import m3.users.dto.rq.AuthRqDto;
import m3.users.dto.rs.*;

import java.util.List;

public interface UserService {

    AuthSuccessRsDto auth(AuthRqDto authRqDto);

    UpdateUserListInfoRsDto getUsers(Long userId, List<Long> ids);

    void updateLastLogout(Long userId);

    GotMapFriendIdsRsDto getMapFriends(Long userId, Long mapId, List<Long> fids);

    GotFriendsIdsRsDto getUserIdsFromSocNetIds(Long userId, List<Long> friendSocNetIds);

    GotTopUsersRsDto getTopUsersRsDto(Long userId, List<Long> ids);

    SetOneHealthHideRsDto healthBack(Long userId);

    SetOneHealthHideRsDto healthDown(Long userId);

    SetOneHealthHideRsDto zeroLife(Long userId);
}
