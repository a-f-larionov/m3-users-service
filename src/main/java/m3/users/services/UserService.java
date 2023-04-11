package m3.users.services;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import m3.users.entities.UserEntity;
import m3.users.repositories.UsersRepository;

@Service
@AllArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;

    public List<UserEntity> getUsers(List<Long> ids) {
        return usersRepository.findAllByIdIn(ids);
    }

    public void updateLastLogout(long userId) {
        usersRepository.updateLastLogout(userId, System.currentTimeMillis());
    }
    
}