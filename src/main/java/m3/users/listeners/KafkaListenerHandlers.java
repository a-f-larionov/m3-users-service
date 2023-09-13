package m3.users.listeners;

import lombok.AllArgsConstructor;
import m3.users.dto.rq.AuthRqDto;
import m3.users.dto.rq.SendMeMapFriendsRqDto;
import m3.users.dto.rq.SendMeUserListInfoRqDto;
import m3.users.dto.rq.UpdateLastLogoutRqDto;
import m3.users.dto.rs.AuthSuccessRsDto;
import m3.users.dto.rs.GotMapFriendIdsRsDto;
import m3.users.dto.rs.UpdateUserListInfoRsDto;
import m3.users.services.impl.UserServiceImpl;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
@KafkaListener(topics = "t-users", groupId = "2")
public class KafkaListenerHandlers {

    private final UserServiceImpl service;

    @KafkaHandler
    @SendTo("t-node")
    public AuthSuccessRsDto auth(AuthRqDto authRqDto) {
        return service.auth(authRqDto);
    }

    @KafkaHandler
    @SendTo("t-node")
    public UpdateUserListInfoRsDto sendMeUserListInfo(SendMeUserListInfoRqDto sendMeUserListInfoDto) {
        return service.getUsers(sendMeUserListInfoDto);
    }

    @KafkaHandler
    @SendTo("t-node")
    public GotMapFriendIdsRsDto sendMeMapFriends(SendMeMapFriendsRqDto sendMeMapFriendsRqDto){
       return service.getMapFriends(sendMeMapFriendsRqDto);
    }

    @KafkaHandler
    public void updateLastLogout(UpdateLastLogoutRqDto dto) {
        service.updateLastLogout(dto);
    }
}
