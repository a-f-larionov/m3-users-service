package m3.users.services.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import m3.users.entities.UserEntity;
import m3.users.services.HealthService;
import m3.lib.settings.CommonSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Slf4j
@Service
@Transactional
public class HealthServiceImpl implements HealthService {

    @Override
    public boolean isMaxHealths(UserEntity user) {
        return getHealths(user).equals(CommonSettings.HEALTH_MAX);
    }

    @Override
    public void setHealths(UserEntity user, Long value) {
        user.setFullRecoveryTime(getTime() -
                (CommonSettings.HEALTH_RECOVERY_TIME * value) +
                (CommonSettings.HEALTH_MAX * CommonSettings.HEALTH_RECOVERY_TIME));
    }


    public Long getHealths(UserEntity user) {

        double timeLeft = (double) user.getFullRecoveryTime() - getTime();

        if (timeLeft <= 0) return CommonSettings.HEALTH_MAX;

        var healthsLeft = Math.floor(CommonSettings.HEALTH_MAX - (timeLeft / CommonSettings.HEALTH_RECOVERY_TIME));

        return Math.max(0L, (long) healthsLeft);
    }

    private long getTime() {
        /*
         * -1 секунда делает так, что сердце не дергается на клиенте, это костыль да
         */
        return (long) Math.floor(System.currentTimeMillis() / 1000D) ;
    }
}
