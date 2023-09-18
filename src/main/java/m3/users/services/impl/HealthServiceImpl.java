package m3.users.services.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import m3.users.entities.UserEntity;
import m3.users.services.HealthService;
import m3.users.settings.CommonSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Slf4j
@Service
@Transactional
public class HealthServiceImpl implements HealthService {

    public Long getHealths(UserEntity user) {

        var fullRecoveryTime = user.getFullRecoveryTime();
        var now = 0L;
        var recoveryTime = 0L;
        var timeLeft = 0L;

        now = getTime();
        recoveryTime = CommonSettings.HEALTH_RECOVERY_TIME;

        timeLeft = fullRecoveryTime - now;

        if (timeLeft <= 0) return CommonSettings.HEALTH_MAX;

        return Math.max(0L, CommonSettings.HEALTH_MAX - timeLeft / recoveryTime);
    }

    private long getTime() {
        return System.currentTimeMillis() / 1000L;
    }

    @Override
    public boolean isMaxHealths(UserEntity user) {
        return false;
    }

    @Override
    public void changeHealths(UserEntity user, int i) {

    }
}
