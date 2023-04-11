package m3.users.dto.rs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserInfoRsDto {

    private long id;
    private long nextPointId;
    private long socNetUserId;
    private long fullRecoveryTime;
}
