package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameDetailVncEndTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GameDetailVncEndTimeRepository extends JpaRepository<GameDetailVncEndTime, Long>, JpaSpecificationExecutor<GameDetailVncEndTime> {


    GameDetailVncEndTime findFirstByPlatformAndStatusOrderByEndTimeDesc(String vendorCode, Integer status);
}
