package m3.users.services.impl;

import m3.users.entities.UserEntity;
import m3.users.services.HealthService;
import m3.users.settings.CommonSettings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HealthServiceImplTest {

    private final HealthService healthService = new HealthServiceImpl();

    @Test
    void isMaxHealthsFitted() {
        // given
        var user = new UserEntity();
        user.setFullRecoveryTime(System.currentTimeMillis() / 1000L);

        // when - then
        assertThat(healthService.isMaxHealths(user)).isTrue();
    }

    @Test
    void isMaxHealthWithMoreTime() {
        // given
        var user = new UserEntity();
        user.setFullRecoveryTime((System.currentTimeMillis() / 1000L) + 2);

        // when - then
        assertThat(healthService.isMaxHealths(user)).isFalse();
    }

    @Test
    void isMaxHealthWithLessTime() {
        // given
        var user = new UserEntity();
        user.setFullRecoveryTime((System.currentTimeMillis() / 1000L) - 2);

        // when - then
        assertThat(healthService.isMaxHealths(user)).isTrue();
    }

    @Test
    void setHealthToMax() {
        // given
        var user = new UserEntity();

        // when
        healthService.setHealths(user, CommonSettings.HEALTH_MAX);

        // then
        assertThat(user.getFullRecoveryTime()).isEqualTo(System.currentTimeMillis() / 1000L);
        assertThat(healthService.getHealths(user)).isEqualTo(CommonSettings.HEALTH_MAX);
    }

    @Test
    void setHealthToZero() {
        // given
        var user = new UserEntity();

        // when
        healthService.setHealths(user, 0L);

        // then
        assertThat(user.getFullRecoveryTime()).isEqualTo(
                (System.currentTimeMillis() / 1000L)
                        + CommonSettings.HEALTH_MAX * CommonSettings.HEALTH_RECOVERY_TIME);
        assertThat(healthService.getHealths(user)).isEqualTo(0);
    }

    @Test
    void setHealthToTwo() {
        // given
        var user = new UserEntity();

        // when
        healthService.setHealths(user, 2L);

        // then
        assertThat(user.getFullRecoveryTime()).isEqualTo(
                (System.currentTimeMillis() / 1000L)
                        + 3L * CommonSettings.HEALTH_RECOVERY_TIME);
        assertThat(healthService.getHealths(user)).isEqualTo(2);
    }
}