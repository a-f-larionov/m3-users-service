package m3.users.dto.rq;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import m3.lib.enums.SocNetType;

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
    @NotNull
    private Long appId;
    private String authKey;
    private Long connectionId;
}
