package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordDMCEndTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GameRecordDMCEndTimeRepository extends JpaRepository<GameRecordDMCEndTime, Long>, JpaSpecificationExecutor<GameRecordDMCEndTime> {

    GameRecordDMCEndTime findFirstByPlatformAndStatusOrderByEndTimeDesc(String platform, Integer Status);
}
