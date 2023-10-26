package m3.users.dto.rs;

import lombok.*;
import lombok.experimental.SuperBuilder;
import m3.lib.dto.rs.UserIdRsDto;

@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserInfoRsDto extends UserIdRsDto {
    private long id;
    private long nextPointId;
    private long socNetUserId;
    private long fullRecoveryTime;
}
