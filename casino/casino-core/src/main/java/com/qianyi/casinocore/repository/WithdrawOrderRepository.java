package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.WithdrawOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface WithdrawOrderRepository extends JpaRepository<WithdrawOrder,Long>, JpaSpecificationExecutor<WithdrawOrder> {

    @Query(value = "select * from withdraw_order u where u.id = ? for update",nativeQuery = true)
    WithdrawOrder findUserByWithdrawIdOrderLock(Long id);

    Integer countByUserIdAndStatus(Long userId,int status);


    @Modifying
    @Query(value = "update withdraw_order c set c.remark = ?1 where  c.id = ?2",nativeQuery = true)
    void updateWithdrawOrderRemark(String remark,Long id);
}
