package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.SysConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysConfigRepository extends JpaRepository<SysConfig,Long>, JpaSpecificationExecutor<SysConfig> {
}
