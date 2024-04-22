package m3.users.listeners;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import m3.lib.dto.rs.UpdateUserInfoRsDto;
import m3.users.dto.rq.*;
import m3.users.dto.rs.*;
import m3.users.services.UserService;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
@KafkaListener(topics = "topic-users")
public class TopicUsersListener {

    private final UserService service;

    @KafkaHandler
    @SendTo("topic-client")
    public AuthSuccessRsDto auth(@Valid AuthRqDto authRqDto) {
        return service.auth(authRqDto);
    }

    @KafkaHandler
    @SendTo("topic-client")
    public UpdateUserListInfoRsDto sendUserListInfo(@Valid SendUserListInfoRqDto rq) {
        return service.getUsers(rq.getUserId(), rq.getIds());
    }

    @KafkaHandler
    @SendTo("topic-client")
    public GotMapFriendIdsRsDto sendMapFriends(@Valid SendMapFriendsRqDto rq) {
        return service.getMapFriends(rq.getUserId(), rq.getMapId(), rq.getFids());
    }

    @KafkaHandler
    public void updateLastLogout(@Valid UpdateLastLogoutRqDto dto) {
        service.updateLastLogout(dto.getUserId());
    }

    @KafkaHandler
    @SendTo("topic-client")
    public GotFriendsIdsRsDto sendFriendIdsBySocNet(@Valid SendFriendIdsBySocNetRqDto rq) {
        return service.getUserIdsFromSocNetIds(rq.getUserId(), rq.getFriendSocNetIds());
    }

    @KafkaHandler
    @SendTo("topic-client")
    public GotTopUsersRsDto sendTopUsers(@Valid SendTopUsersRqDto rq) {
        return service.getTopUsersRsDto(rq.getUserId(), rq.getFids());
    }

    @KafkaHandler
    @SendTo("topic-client")
    public SetOneHealthHideRsDto healthUp(@Valid HealthBackRqDto rq) {
        return service.healthUp(rq.getUserId());
    }

    @KafkaHandler
    @SendTo("topic-client")
    public SetOneHealthHideRsDto healthDown(@Valid HealthDownRqDto rq) {
        return service.healthDown(rq.getUserId(), rq.getPointId());
    }

    @KafkaHandler
    @SendTo("topic-client")
    public UpdateUserInfoRsDto zeroLife(@Valid ZeroLifeRqDto rq) {
        return service.zeroLife(rq.getUserId());
    }
}
