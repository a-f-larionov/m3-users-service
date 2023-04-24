package m3.users.repositories;

import m3.users.entities.UserEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends CrudRepository<UserEntity, Long> {

    List<UserEntity> findAllByIdIn(List<Long> ids);

    @Modifying
    @Query(value = "UPDATE users SET logout_tm = ?2 WHERE id = ?1", nativeQuery = true)
    int updateLastLogout(@Param("id") Long id, @Param("logout_tm") Long time);

    @Modifying
    @Query(value = "UPDATE users SET login_tm = ?2 WHERE id = ?1", nativeQuery = true)
    int updateLogin(@Param("id") Long id, @Param("login_tm") Long newLoginTimeStamp);

    Optional<UserEntity> findBySocNetTypeIdAndSocNetUserId(Long socNetTypeId, Long socNetUserId);
}