package m3.users.dto.rq;

import lombok.*;
import m3.users.enums.SocNetType;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class AuthRqDto {

    private SocNetType socNetType;
    private Long socNetUserId;
    private Long appId;
    private String authKey;
    private Long connectionId;
}
