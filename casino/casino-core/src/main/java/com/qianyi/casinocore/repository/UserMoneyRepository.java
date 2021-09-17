package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface UserMoneyRepository extends JpaRepository<UserMoney,Long> {

    @Query(value = "select * from user_money u where u.user_id = ? for update",nativeQuery = true)
    UserMoney findUserByUserIdUseLock(Long userId);

    @Modifying
    @Query("update UserMoney u set u.codeNum=u.codeNum+?2 where u.userId=?1")
    void updateCodeNum(Long userId, BigDecimal codeNum);
}
