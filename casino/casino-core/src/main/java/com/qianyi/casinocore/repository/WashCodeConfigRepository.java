package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.WashCodeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface WashCodeConfigRepository extends JpaRepository<WashCodeConfig,Long>, JpaSpecificationExecutor<WashCodeConfig> {

    List<WashCodeConfig> findByPlatform(String platform);
}
