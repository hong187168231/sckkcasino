package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.AmountConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AmountConfigRepository extends JpaRepository<AmountConfig,Long>, JpaSpecificationExecutor<AmountConfig> {
}
