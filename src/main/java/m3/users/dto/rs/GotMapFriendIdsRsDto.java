package m3.users.dto.rs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GotMapFriendIdsRsDto {
    private Long toUserId;
    private Long mapId;
    private List<Long> ids;
}
