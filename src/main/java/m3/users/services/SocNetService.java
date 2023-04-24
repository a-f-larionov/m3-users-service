package m3.users.services;

import m3.users.dto.rq.AuthRqDto;

public interface SocNetService {

    boolean checkAuth(AuthRqDto authRqDto);
}
