package m3.users.dto.rs;

import lombok.*;
import m3.users.enums.SocNetType;

@Builder
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class AuthSuccessRsDto {

    private Long connectionId;

    private Long userId;
    private SocNetType socNetType;
    private Long socNetUserId;

    private Long nextPointId;
    private Long createTm;
    private Long loginTm;
    private Long logoutTm;
    private Long fullRecoveryTime;
}
