package m3.users.services;

import m3.lib.entities.UserEntity;

public interface HealthService {
    boolean isMaxHealths(UserEntity user);

    void setHealths(UserEntity user, Long value);

    Long getHealths(UserEntity user);
}
