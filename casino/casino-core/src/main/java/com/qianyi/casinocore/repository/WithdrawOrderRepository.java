package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.WithdrawOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface WithdrawOrderRepository extends JpaRepository<WithdrawOrder,Long>, JpaSpecificationExecutor<WithdrawOrder> {

    @Query(value = "select * from WithdrawOrder u where u.id = ? for update",nativeQuery = true)
    WithdrawOrder findUserByWithdrawIdOrderLock(Long id);
}
