package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface UserMoneyRepository extends JpaRepository<UserMoney,Long>, JpaSpecificationExecutor<UserMoney> {

    @Query(value = "select * from user_money u where u.user_id = ? for update",nativeQuery = true)
    UserMoney findUserByUserIdUseLock(Long userId);

    @Modifying
    @Query("update UserMoney u set u.codeNum=u.codeNum+?2 where u.userId=?1")
    void addCodeNum(Long userId, BigDecimal codeNum);

    @Modifying
    @Query("update UserMoney u set u.codeNum=u.codeNum-?2 where u.userId=?1")
    void subCodeNum(Long userId, BigDecimal codeNum);

    @Modifying
    @Query("update UserMoney u set u.money=u.money+?2 where u.userId=?1")
    void addMoney(Long userId, BigDecimal money);

    @Modifying
    @Query("update UserMoney u set u.money=u.money-?2 where u.userId=?1")
    void subMoney(Long userId, BigDecimal money);

    UserMoney findByUserId(Long userId);

    @Modifying
    @Query("update UserMoney u set u.freezeMoney=u.freezeMoney+?2 where u.userId=?1")
    void addFreezeMoney(Long userId, BigDecimal freezeMoney);

    @Modifying
    @Query("update UserMoney u set u.freezeMoney=u.freezeMoney-?2 where u.userId=?1")
    void subFreezeMoney(Long userId, BigDecimal freezeMoney);
}
