package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ChargeOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface ChargeOrderRepository extends JpaRepository<ChargeOrder,Long>, JpaSpecificationExecutor<ChargeOrder> {
    @Query(value = "select * from charge_order c where c.id = ? for update",nativeQuery = true)
    ChargeOrder findChargeOrderByIdUseLock(Long id);

    @Modifying
    @Query(value = "update charge_order c set c.status=3 where c.status = ?1 and c.create_time <= ?2",nativeQuery = true)
    void updateChargeOrders(Integer status,String time);

    Integer countByUserIdAndStatus(Long userId,int status);

    @Modifying
    @Query(value = "update charge_order c set c.remark = ?1 where   c.id = ?2",nativeQuery = true)
    void updateChargeOrdersRemark(String remark,Long id);

    @Query(value = "select IFNULL( SUM(charge_amount),0) from charge_order where `status` in (1,4,5)",nativeQuery = true)
    BigDecimal sumChargeAmount();

    @Modifying(clearAutomatically = true)
    @Query("update ChargeOrder c set c.remark=?2,c.lastModifier=?3,c.status=?4,c.betRate=?5,c.updateTime=?6 where c.id=?1")
    void updateChargeOrder(Long id, String remark,String lastModifier,Integer status,BigDecimal betRate,Date updateTime);

    @Modifying
    @Query(value = "update charge_order w set w.succeed_time = w.update_time where w.succeed_time is null ",nativeQuery = true)
    void updateChargeOrderSucceedTime();
}
