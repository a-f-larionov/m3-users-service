package m3.users.services.impl;

import m3.users.dto.rq.AuthRqDto;
import m3.lib.enums.SocNetType;
import m3.users.services.SocNetService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SocNetServiceImplTest {

private final SocNetService socNetVK = new SocNetVKServiceImpl("secretKey");
    private final SocNetStandaloneServiceImpl socNetStandalone = new SocNetStandaloneServiceImpl();
    private final SocNetServiceImpl socNet = new SocNetServiceImpl(socNetVK, socNetStandalone);

    @Test
    void switcherVKReturns() {
        // given-when
        var actual = socNet.getByType(SocNetType.VK);

        // then
        assertThat(actual).isInstanceOf(SocNetVKServiceImpl.class);
    }

    @Test
    void switcherStandaloneReturns() {
        // given-when
        var actual = socNet.getByType(SocNetType.Standalone);

        // then
        assertThat(actual).isInstanceOf(SocNetStandaloneServiceImpl.class);
    }

    @Test
    void socNetVKAuthWithCorrectData() {
        // given
        var authRqDto = createAuthPqDto(SocNetType.VK, "cbabcb24c5a1d3bdaafc07230fd8da15");

        // when
        var actual = socNet.checkAuth(authRqDto);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void socNetVKAuthWithWrongData() {
        // given
        var authRqDto = createAuthPqDto(SocNetType.VK, "wrongKey");

        // when
        var actual = socNet.checkAuth(authRqDto);

        // then
        assertThat(actual).isFalse();
    }

    @Test
    void setSocNetStandaloneAnyData() {
        // given
        var authRqDto = createAuthPqDto(SocNetType.Standalone, "anyKey");

        // when
        var actual = socNet.checkAuth(authRqDto);

        // then
        assertThat(actual).isTrue();
    }

    private AuthRqDto createAuthPqDto(SocNetType vk, String authKey) {
        return AuthRqDto.builder()
                .socNetType(vk)
                .appId(12345678L)
                .socNetUserId(23456789L)
                .authKey(authKey)
                .build();
    }
}
