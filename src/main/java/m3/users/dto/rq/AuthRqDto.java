package m3.users.dto.rq;

import jakarta.validation.constraints.NotBlank;
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
    @NotNull
    private SocNetType socNetType;
    @NotNull
    private Long socNetUserId;
    @NotNull
    private Long appId;
    @NotBlank
    private String authKey;
    @NotNull
    private Long connectionId;
}
