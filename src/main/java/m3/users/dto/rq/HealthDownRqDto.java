package m3.users.dto.rq;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import m3.lib.dto.rs.UserIdRsDto;

@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HealthDownRqDto extends UserIdRsDto {
    @NotNull
    private Long pointId;
}
