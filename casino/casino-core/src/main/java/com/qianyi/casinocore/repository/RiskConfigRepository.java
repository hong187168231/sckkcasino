package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.RiskConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RiskConfigRepository  extends JpaRepository<RiskConfig, Long>, JpaSpecificationExecutor<RiskConfig> {
}
