package m3.users.dto.rs;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GotMapFriendIdsRsDto extends UserIdRsDto{
    private Long mapId;
    private List<Long> ids;
}
