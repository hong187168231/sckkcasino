package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordAeEndTime;
import com.qianyi.casinocore.model.GameRecordObEndTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GameRecordAeEndTimeRepository extends JpaRepository<GameRecordAeEndTime, Long>, JpaSpecificationExecutor<GameRecordAeEndTime> {

    GameRecordAeEndTime findFirstByPlatformAndStatusOrderByEndTimeDesc(String platform, Integer Status);
}
