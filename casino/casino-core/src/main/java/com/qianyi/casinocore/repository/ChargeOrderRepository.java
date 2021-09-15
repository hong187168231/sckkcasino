package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ChargeOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChargeOrderRepository extends JpaRepository<ChargeOrder,Long>, JpaSpecificationExecutor<ChargeOrder> {
    @Query(value = "select * from charge_order c where c.id = ? for update",nativeQuery = true)
    ChargeOrder findChargeOrderByIdUseLock(Long id);

    @Query(value = "select * from charge_order c where c.status = ?1 and c.create_time <=?2 for update",nativeQuery = true)
    List<ChargeOrder> findChargeOrdersUseLock(Integer status,String time);
}
