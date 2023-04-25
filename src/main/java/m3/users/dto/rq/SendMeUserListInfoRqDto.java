package m3.users.dto.rq;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMeUserListInfoRqDto {

    private Long toUserId;
    private List<Long> ids;
}
