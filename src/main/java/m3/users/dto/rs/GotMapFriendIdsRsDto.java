package m3.users.dto.rs;

import lombok.*;
import lombok.experimental.SuperBuilder;
import m3.lib.dto.rs.UserIdRsDto;

import java.util.List;

@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GotMapFriendIdsRsDto extends UserIdRsDto {
    private Long mapId;
    private List<Long> ids;
}
