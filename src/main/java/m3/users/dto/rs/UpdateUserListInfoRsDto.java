package m3.users.dto.rs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserListInfoRsDto {

    private long toUserId;
    private List<UpdateUserInfoRsDto> list;
}
