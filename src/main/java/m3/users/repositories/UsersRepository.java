package m3.users.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import m3.users.entities.UserEntity;

public interface UsersRepository extends CrudRepository<UserEntity, Long> {

    public List<UserEntity> findAllByIdIn(List<Long> ids);

    @Modifying
    @Query(value = "UPDATE users SET logout_tm = ?2 WHERE id = ?1", nativeQuery = true)
    public int updateLastLogout(
            @Param("id") Long id,
            @Param("logout_tm") Long tm);
}