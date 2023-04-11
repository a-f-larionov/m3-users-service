package m3.users.listeners;

import java.util.List;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import m3.users.dto.rq.SendMeUserInfoRqDto;
import m3.users.dto.rq.UpdateLastLogoutRqDto;
import m3.users.dto.rs.UpdateUserListInfoRsDto;
import m3.users.entities.UserEntity;
import m3.users.mappers.UsersMapper;
import m3.users.services.UserService;

@AllArgsConstructor
@Component
@KafkaListener(topics = "t-users", groupId = "2")
public class KafkaListenerHandlers {

    private final UserService service;
    private final UsersMapper mapper;

    @KafkaHandler
    @SendTo("t-node")
    public UpdateUserListInfoRsDto sendMeUserInfo(SendMeUserInfoRqDto sendMeUserInfoDto) {

        List<UserEntity> usersList = service.getUsers(sendMeUserInfoDto.getIds());

        var list = usersList.stream()
                .map(user -> {
                    return mapper.toDto(user);
                })
                .toList();

        return UpdateUserListInfoRsDto.builder()
                .toUserId(sendMeUserInfoDto.getToUserId())
                .list(list)
                .build();
    }

    @KafkaHandler
    public void updateLastLogout(UpdateLastLogoutRqDto dto){
        service.updateLastLogout(dto.getUserId());
    }

    // @KafkaHandler
    // public void updateLastLogin(UpdateLastLoginRqDto dto){
    //     // @todo
    //     //time = LogicTimeServer.getTime();
    //     // DB.query("UPDATE " + tableName + " SET login_tm = " +
    //     //     time + " WHERE id = " + userId, function () {
    //     //     }
    //     // );        
    // }
}
