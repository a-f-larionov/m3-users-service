package m3.users.services;

import m3.users.entities.UserEntity;

public interface HealthService {
    boolean isMaxHealths(UserEntity user);

    void changeHealths(UserEntity user, int i);
}
