package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ChargeOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ChargeOrderRepository extends JpaRepository<ChargeOrder,Long>, JpaSpecificationExecutor<ChargeOrder> {
}
