package m3.users.services;

import m3.users.entities.UserEntity;

public interface HealthService {
    boolean isMaxHealths(UserEntity user);

    void setHealths(UserEntity user, Long value);

    Long getHealths(UserEntity user);
}
