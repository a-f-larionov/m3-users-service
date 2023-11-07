package m3.users.dto.rq;

import lombok.*;
import lombok.experimental.SuperBuilder;
import m3.lib.dto.rs.UserIdRsDto;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;

@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SendFriendIdsBySocNetRqDto extends UserIdRsDto {
    @NonNull
    private List<Long> friendSocNetIds;
}


