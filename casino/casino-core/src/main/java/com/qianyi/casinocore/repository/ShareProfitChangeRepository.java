package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ShareProfitChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface ShareProfitChangeRepository extends JpaRepository<ShareProfitChange,Long>, JpaSpecificationExecutor<ShareProfitChange> {


    ShareProfitChange findByUserIdAndOrderNo(Long userId,String orderNo);

    @Query(value = "select s.from_user_id fromUserId,SUM(s.amount) amount from share_profit_change s where s.bet_time >= ?1 and s.bet_time <= ?2 GROUP BY s.from_user_id ",nativeQuery = true)
    List<Map<String, Object>> findSumAmount(String startTime, String endTime);
}
