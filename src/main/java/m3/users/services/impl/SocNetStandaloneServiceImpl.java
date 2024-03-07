package m3.users.services.impl;

import m3.users.dto.rq.AuthRqDto;
import m3.users.services.SocNetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("socNetStandalone")
@Transactional
public class SocNetStandaloneServiceImpl implements SocNetService {

    @Override
    public boolean checkAuth(AuthRqDto authRqDto) {
        return true;
    }

}
