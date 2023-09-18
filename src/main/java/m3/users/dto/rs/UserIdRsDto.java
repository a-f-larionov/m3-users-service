package m3.users.dto.rs;

import lombok.*;
import lombok.experimental.SuperBuilder;


@SuperBuilder
@NoArgsConstructor
//@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class UserIdRsDto {
    private Long userId;
}
