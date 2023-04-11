package m3.users.dto.rq;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SendMeUserInfoRqDto {

    private Long toUserId;
    private List<Long> ids;
}
