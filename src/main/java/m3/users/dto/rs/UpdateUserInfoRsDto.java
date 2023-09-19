package m3.users.dto.rs;

import lombok.*;
import lombok.experimental.SuperBuilder;

@ToString
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserInfoRsDto extends  UserIdRsDto{
    private long id;
    private long nextPointId;
    private long socNetUserId;
    private long fullRecoveryTime;
}
