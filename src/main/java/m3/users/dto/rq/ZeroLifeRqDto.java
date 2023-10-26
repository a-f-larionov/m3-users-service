package m3.users.dto.rq;

import lombok.*;
import lombok.experimental.SuperBuilder;
import m3.lib.dto.rs.UserIdRsDto;

@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class ZeroLifeRqDto extends UserIdRsDto {

}

