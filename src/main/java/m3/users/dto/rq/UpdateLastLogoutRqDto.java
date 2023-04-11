package m3.users.dto.rq;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class UpdateLastLogoutRqDto {
  
  private long userId;
}