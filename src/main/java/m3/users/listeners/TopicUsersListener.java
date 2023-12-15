package m3.users.listeners;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import m3.lib.dto.rs.UpdateUserInfoRsDto;
import m3.lib.kafka.KafkaListenerErrorHandler;
import m3.users.dto.rq.*;
import m3.users.dto.rs.*;
import m3.users.services.UserService;
import m3.users.services.impl.UserServiceImpl;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
@KafkaListener(topics = "topic-users", errorHandler = "kafkaListenerErrorHandler")
public class TopicUsersListener {

    private final UserService service;

//    @KafkaListener(topics = "topic-users")
//    public void receive(ConsumerRecord<?, ?> consumerRecord) {
//        System.out.println("!!!!");
//        System.out.println(consumerRecord);
//    }

    @KafkaHandler
    @SendTo("topic-client")
    public AuthSuccessRsDto auth(@Valid @Payload AuthRqDto authRqDto) {
//        System.out.println("!!!!!!!!!!" + authRqDto.toString());
        return service.auth(authRqDto);
    }

    @KafkaHandler
    @SendTo("topic-client")
    public UpdateUserListInfoRsDto sendUserListInfo(SendUserListInfoRqDto rq) {
        return service.getUsers(rq.getUserId(), rq.getIds());
    }

    @KafkaHandler
    @SendTo("topic-client")
    public GotMapFriendIdsRsDto sendMapFriends(SendMapFriendsRqDto rq) {
        return service.getMapFriends(rq.getUserId(), rq.getMapId(), rq.getFids());
    }

    @KafkaHandler
    public void updateLastLogout(UpdateLastLogoutRqDto dto) {
        service.updateLastLogout(dto.getUserId());
    }

    @KafkaHandler
    @SendTo("topic-client")
    public GotFriendsIdsRsDto sendFriendIdsBySocNet(@Payload SendFriendIdsBySocNetRqDto rq) {
        return service.getUserIdsFromSocNetIds(rq.getUserId(), rq.getFriendSocNetIds());
    }

    @KafkaHandler
    @SendTo("topic-client")
    public GotTopUsersRsDto sendTopUsers(SendTopUsersRqDto rq) {
        return service.getTopUsersRsDto(rq.getUserId(), rq.getFids());
    }

    @KafkaHandler
    @SendTo("topic-client")
    public SetOneHealthHideRsDto healthUp(HealthBackRqDto rq) {
        return service.healthUp(rq.getUserId());
    }

    @KafkaHandler
    @SendTo("topic-client")
    public SetOneHealthHideRsDto healthDown(HealthDownRqDto rq) {
        return service.healthDown(rq.getUserId(), rq.getPointId());
    }

    @KafkaHandler
    @SendTo("topic-client")
    public UpdateUserInfoRsDto zeroLife(ZeroLifeRqDto rq) {
        return service.zeroLife(rq.getUserId());
    }
}
