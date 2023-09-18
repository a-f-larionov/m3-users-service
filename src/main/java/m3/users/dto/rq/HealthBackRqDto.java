package m3.users.dto.rq;

import lombok.*;
import lombok.experimental.SuperBuilder;
import m3.users.dto.rs.UserIdRsDto;

@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HealthBackRqDto extends UserIdRsDto {
}
