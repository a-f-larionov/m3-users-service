package m3.users.listeners;

import lombok.AllArgsConstructor;
import m3.users.dto.rq.AuthRqDto;
import m3.users.dto.rq.SendMeUserInfoRqDto;
import m3.users.dto.rq.UpdateLastLogoutRqDto;
import m3.users.dto.rs.AuthSuccessRsDto;
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
        var rs = service.auth(authRqDto);
        rs.setConnectionId(authRqDto.getConnectionId());
        return rs;
    }

    @KafkaHandler
    @SendTo("t-node")
    public UpdateUserListInfoRsDto sendMeUserInfo(SendMeUserInfoRqDto sendMeUserInfoDto) {
        return service.getUsers(sendMeUserInfoDto);
    }

    @KafkaHandler
    public void updateLastLogout(UpdateLastLogoutRqDto dto) {
        service.updateLastLogout(dto);
    }
}
