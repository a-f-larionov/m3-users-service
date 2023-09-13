package m3.users.services;

import m3.users.dto.rq.AuthRqDto;
import m3.users.dto.rq.SendMeMapFriendsRqDto;
import m3.users.dto.rq.SendMeUserListInfoRqDto;
import m3.users.dto.rq.UpdateLastLogoutRqDto;
import m3.users.dto.rs.AuthSuccessRsDto;
import m3.users.dto.rs.GotMapFriendIdsRsDto;
import m3.users.dto.rs.UpdateUserListInfoRsDto;

public interface UserService {

    AuthSuccessRsDto auth(AuthRqDto authRqDto);

    UpdateUserListInfoRsDto getUsers(SendMeUserListInfoRqDto rq);

    void updateLastLogout(UpdateLastLogoutRqDto rq);

    GotMapFriendIdsRsDto getMapFriends(SendMeMapFriendsRqDto sendMeMapFriendsRqDto);
}
