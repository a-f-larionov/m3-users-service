package m3.users.dto.rs;

import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SetOneHealthHideRsDto extends UserIdRsDto {
    public Long fullRecoveryTime;
}
