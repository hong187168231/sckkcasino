package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ExtractPointsChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface ExtractPointsChangeRepository extends JpaRepository<ExtractPointsChange,Long>, JpaSpecificationExecutor<ExtractPointsChange> {
    @Query(value = "select ifnull(sum(amount),0) amount  from extract_points_change e where create_time >= ?1 and create_time <= ?2 ",nativeQuery = true)
    BigDecimal sumAmount(String startTime, String endTime);

    @Query(value = "select ifnull(sum(amount),0) amount  from extract_points_change e ",nativeQuery = true)
    BigDecimal sumAmount();
}
