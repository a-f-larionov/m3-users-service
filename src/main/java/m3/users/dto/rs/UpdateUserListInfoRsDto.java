package m3.users.dto.rs;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserListInfoRsDto {

    private long toUserId;
    private List<UpdateUserInfoRsDto> list;
}
