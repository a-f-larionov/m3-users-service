package m3.users.dto.rs;

import lombok.*;
import lombok.experimental.SuperBuilder;
import m3.lib.dto.rs.UserIdRsDto;

@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AuthSuccessRsDto extends UserIdRsDto {

    private Long connectionId;

    private Long id;
    private Long socNetTypeId;
    private Long socNetUserId;

    private Long nextPointId;
    private Long createTm;
    private Long loginTm;
    private Long logoutTm;
    private Long fullRecoveryTime;
}
