package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordObEndTime;
import com.qianyi.casinocore.model.GameRecordObzrTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface GameRecordObzrTimeRepository extends JpaRepository<GameRecordObzrTime, Long>, JpaSpecificationExecutor<GameRecordObzrTime> {


    @Query(value = "select max(end_time) from game_record_obzr_time",nativeQuery = true)
    String findLastEndTime();

}