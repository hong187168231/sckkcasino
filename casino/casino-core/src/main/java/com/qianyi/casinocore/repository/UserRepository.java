package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface UserRepository extends JpaRepository<User, Long> , JpaSpecificationExecutor<User> {
    User findByAccount(String account);

    @Query("update User u set u.money=u.money-?2 where u.id=?1")
    @Modifying
    void subMoney(Long id, BigDecimal money);

    @Query("update User u set u.money=u.money+?2 where u.id=?1")
    @Modifying
    void addMoney(Long id, BigDecimal money);

    Integer countByRegisterIp(String ip);

    User getByName(String userName);
}
