package m3.users.listeners;

import lombok.AllArgsConstructor;
import m3.users.dto.rq.*;
import m3.users.dto.rs.*;
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
    public UpdateUserListInfoRsDto sendUserListInfo(SendUserListInfoRqDto rq) {
        return service.getUsers(rq.getUserId(), rq.getIds());
    }

    @KafkaHandler
    @SendTo("t-node")
    public GotMapFriendIdsRsDto sendMapFriends(SendMapFriendsRqDto rq) {
        return service.getMapFriends(rq.getUserId(), rq.getMapId(), rq.getFids());
    }

    @KafkaHandler
    public void updateLastLogout(UpdateLastLogoutRqDto dto) {
        service.updateLastLogout(dto.getUserId());
    }

    @KafkaHandler
    @SendTo("t-node")
    public GotFriendsIdsRsDto sendFriendIdsBySocNet(SendFriendIdsBySocNetRqDto rq) {
        return service.getUserIdsFromSocNetIds(rq.getUserId(), rq.getFriendSocNetIds());
    }

    @KafkaHandler
    @SendTo("t-node")
    public GotTopUsersRsDto sendTopUsers(SendTopUsersRqDto rq) {
        return service.getTopUsersRsDto(rq.getUserId(), rq.getIds());
    }

    @KafkaHandler
    @SendTo("t-node")
    public SetOneHealthHideRsDto healthBack(HealthBackRqDto rq) {
        return service.healthBack(rq.getUserId());
    }

    @KafkaHandler
    @SendTo("t-node")
    public SetOneHealthHideRsDto healthDown(HealthDownRqDto rq) {
        return service.healthDown(rq.getUserId());
    }

    @KafkaHandler
    @SendTo("t-node")
    public SetOneHealthHideRsDto zeroLife(ZeroLifeRqDto rq){
        return service.zeroLife(rq.getUserId());
    }
}
