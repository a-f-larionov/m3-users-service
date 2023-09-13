package m3.users.dto.rq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMeMapFriendsRqDto {

    public Long toUserId;
    public Long mapId;
    public List<Long> fids;
}
