package m3.users.services.impl;

import lombok.extern.slf4j.Slf4j;
import m3.users.dto.rq.AuthRqDto;
import m3.users.services.SocNetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Slf4j
@Service("socNetVK")
public class SocNetVKServiceImpl implements SocNetService {

    private final String secretKey;

    public SocNetVKServiceImpl(@Value("${socnet.vk.secretKey}") String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean checkAuth(AuthRqDto authRqDto) {

        var expectedAuthKey = calcSign(authRqDto);

        if (expectedAuthKey.equals(authRqDto.getAuthKey())) {
            return true;
        }
        log.warn("Check auth failed: expected: `" + expectedAuthKey + "`, but actual" + authRqDto.toString());
        // @todo telegram log
        return false;
    }

    private String calcSign(AuthRqDto authRqDto) {
        return DigestUtils.md5DigestAsHex((authRqDto.getAppId().toString() +
                "_" +
                authRqDto.getSocNetUserId().toString() +
                "_" +
                secretKey).getBytes());
    }

}
