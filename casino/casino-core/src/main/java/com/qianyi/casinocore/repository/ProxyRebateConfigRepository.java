package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ProxyRebateConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProxyRebateConfigRepository extends JpaRepository<ProxyRebateConfig,Long>, JpaSpecificationExecutor<ProxyRebateConfig> {
}
