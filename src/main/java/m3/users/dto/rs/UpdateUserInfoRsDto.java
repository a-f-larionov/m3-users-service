package m3.users.dto.rs;

import lombok.*;
import lombok.experimental.SuperBuilder;

@ToString
@EqualsAndHashCode
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserInfoRsDto {
    private long id;
    private long nextPointId;
    private long socNetUserId;
    private long fullRecoveryTime;
}
