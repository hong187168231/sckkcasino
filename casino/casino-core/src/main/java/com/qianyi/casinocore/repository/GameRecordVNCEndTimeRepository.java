package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordVNCEndTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GameRecordVNCEndTimeRepository extends JpaRepository<GameRecordVNCEndTime, Long>, JpaSpecificationExecutor<GameRecordVNCEndTime> {

    GameRecordVNCEndTime findFirstByPlatformAndStatusOrderByEndTimeDesc(String platform, Integer Status);
}
