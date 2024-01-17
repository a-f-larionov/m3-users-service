package m3.users.services.impl;

import lombok.RequiredArgsConstructor;
import m3.lib.enums.SocNetType;
import m3.users.dto.rq.AuthRqDto;
import m3.users.services.SocNetService;
import m3.users.services.SocNetSwitcher;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service("socNet")
public class SocNetServiceImpl implements SocNetSwitcher, SocNetService {

    private final SocNetService socNetVK;
    private final SocNetService socNetStandalone;

    @Override
    public SocNetService getByType(SocNetType socNetType) {

        return switch (socNetType) {
            case VK -> socNetVK;
            case Standalone -> socNetStandalone;
        };
    }

    @Override
    public boolean checkAuth(AuthRqDto authRqDto) {
        return getByType(authRqDto.getSocNetType())
                .checkAuth(authRqDto);
    }
}
