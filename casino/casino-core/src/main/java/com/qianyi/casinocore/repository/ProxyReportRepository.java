package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ProxyReport;
import org.springframework.data.jpa.repository.*;

import javax.persistence.LockModeType;
import java.math.BigDecimal;

public interface ProxyReportRepository extends JpaRepository<ProxyReport,Long>, JpaSpecificationExecutor<ProxyReport> {


    ProxyReport findProxyReportByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    ProxyReport findByUserId(Long userId);

    @Modifying
    @Query("update ProxyReport r set r.allBetAmount=r.allBetAmount+?1,r.allProfitAmount=r.allProfitAmount+?2,r.allBetNum=r.allBetNum+?3,"
        + "r.directBetAmount=r.directBetAmount+?1,r.directProfitAmount=r.directProfitAmount+?2,r.directBetNum=r.directBetNum+?3 where r.userId=?4")
    void updateDirect(BigDecimal directBetAmount, BigDecimal directProfitAmount,Integer directBetNum,Long userId);

    @Modifying
    @Query("update ProxyReport r set r.allBetAmount=r.allBetAmount+?1,r.allProfitAmount=r.allProfitAmount+?2,r.allBetNum=r.allBetNum+?3,"
        + "r.otherBetAmount=r.otherBetAmount+?1,r.otherProfitAmount=r.otherProfitAmount+?2,r.otherBetNum=r.otherBetNum+?3 where r.userId=?4")
    void updateOther(BigDecimal otherBetAmount, BigDecimal otherProfitAmount,Integer otherBetNum,Long userId);

    @Modifying
    @Query("update ProxyReport r set r.allGroupNum=r.allGroupNum+?1,r.directGroupNum=r.directGroupNum+?2,r.otherGroupNum=r.otherGroupNum+?3 where r.userId=?4")
    void updateGroupNum(Integer allGroupNum, Integer directGroupNum,Integer otherGroupNum,Long userId);
}
