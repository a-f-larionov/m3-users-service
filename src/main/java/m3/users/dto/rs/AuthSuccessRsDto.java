package m3.users.dto.rs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import m3.users.enums.SocNetType;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthSuccessRsDto {

    private Long userId;
    private SocNetType socNetType;
    private Long socNetUserId;

    private Long nextPointId;

    private Long createTm;
    private Long loginTm;
    private Long logoutTm;
    private Long fullRecoveryTime;

    private Long connectionId;

    private int jkl = 5;

}
