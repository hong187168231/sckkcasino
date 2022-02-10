package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.PromoteCommissionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PromoteCommissionConfigRepository extends JpaRepository<PromoteCommissionConfig,Long>, JpaSpecificationExecutor<PromoteCommissionConfig> {

     PromoteCommissionConfig findByGameType(Integer gameType);
}
