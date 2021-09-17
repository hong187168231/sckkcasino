package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.User;
import org.springframework.data.jpa.repository.*;

import javax.persistence.LockModeType;
import javax.transaction.Transactional;
import java.math.BigDecimal;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    User findByAccount(String account);

    Integer countByRegisterIp(String ip);

    User getByName(String userName);

//    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select * from User u where u.id = ? for update",nativeQuery = true)
    User findUserByUserIdUseLock(Long userId);

}
