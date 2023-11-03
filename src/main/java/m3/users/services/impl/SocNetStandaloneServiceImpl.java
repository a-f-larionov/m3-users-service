package m3.users.services.impl;

import m3.users.dto.rq.AuthRqDto;
import m3.users.services.SocNetService;
import org.springframework.stereotype.Service;

@Service("socNetStandalone")
public class SocNetStandaloneServiceImpl implements SocNetService {

    @Override
    public boolean checkAuth(AuthRqDto authRqDto) {
        return true;
    }

}
