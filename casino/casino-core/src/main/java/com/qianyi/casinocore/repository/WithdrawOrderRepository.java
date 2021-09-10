package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.WithdrawOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WithdrawOrderRepository extends JpaRepository<WithdrawOrder,Long>, JpaSpecificationExecutor<WithdrawOrder> {
}
