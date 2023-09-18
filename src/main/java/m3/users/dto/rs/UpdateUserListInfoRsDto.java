package m3.users.dto.rs;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserListInfoRsDto extends UserIdRsDto {
    private List<UpdateUserInfoRsDto> list;
}
