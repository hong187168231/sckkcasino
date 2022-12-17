package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordDGEndTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GameRecordDGEndTimeRepository extends JpaRepository<GameRecordDGEndTime, Long>, JpaSpecificationExecutor<GameRecordDGEndTime> {

    GameRecordDGEndTime findFirstByPlatformAndStatusOrderByEndTimeDesc(String platform, Integer Status);
}
