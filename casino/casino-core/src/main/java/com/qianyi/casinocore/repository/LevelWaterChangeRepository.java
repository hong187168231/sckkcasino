package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.LevelWaterChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface LevelWaterChangeRepository extends JpaRepository<LevelWaterChange,Long>, JpaSpecificationExecutor<LevelWaterChange> {

    @Query(value = "select ifnull(sum(lwc.bet_water),0)  from level_water_change lwc WHERE lwc.user_id = ?1 ;",nativeQuery = true)
    BigDecimal findTotalBetWater(Long userId);

    @Query(value = "select ifnull(sum(lwc.bet_water),0)  from level_water_change lwc WHERE lwc.user_id = ?1 and date_sub(curdate(), INTERVAL 30 DAY) <= date(`create_time`) ;",nativeQuery = true)
    BigDecimal find10DayBetWater(Long userId);

    @Query
    LevelWaterChange findByUserId(Long userId);

    @Query
    LevelWaterChange findByGameRecordId(Long userId);

}