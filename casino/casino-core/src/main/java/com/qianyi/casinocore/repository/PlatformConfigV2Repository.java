package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.PlatformConfigV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PlatformConfigV2Repository  extends JpaRepository<PlatformConfigV2,Long>, JpaSpecificationExecutor<PlatformConfigV2> {
}
