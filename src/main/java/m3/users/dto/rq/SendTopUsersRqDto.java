package m3.users.dto.rq;

import lombok.*;
import lombok.experimental.SuperBuilder;
import m3.users.dto.rs.UserIdRsDto;

import java.util.List;

@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class SendTopUsersRqDto extends UserIdRsDto {
    private List<Long> fids;
}
