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
    @Query("update UserMoney u set u.washCode=u.washCode+?2 where u.userId=?1")
    void addWashCode(Long userId, BigDecimal washCode);

    @Modifying
    @Query("update UserMoney u set u.washCode=u.washCode-?2 where u.userId=?1")
    void subWashCode(Long userId, BigDecimal washCode);

    @Modifying
    @Query("update UserMoney u set u.shareProfit=u.shareProfit+?2 where u.userId=?1")
    void addShareProfit(Long userId, BigDecimal shareProfit);

    @Modifying
    @Query("update UserMoney u set u.shareProfit=u.shareProfit-?2 where u.userId=?1")
    void subShareProfit(Long userId, BigDecimal shareProfit);
}
