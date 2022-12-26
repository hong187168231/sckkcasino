package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ExportReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ExportReportRepository extends JpaRepository<ExportReport,Long>,
    JpaSpecificationExecutor<ExportReport> {

    @Modifying
    @Query(value = "DELETE from export_report where order_times = ?1 ;",nativeQuery = true)
    void deleteByOrderTimes(String orderTimes);

    @Modifying
    @Query(value = "DELETE from export_report where order_times >= ?1 and order_times <= ?2 ;",nativeQuery = true)
    void deleteByOrderTimes(String startTime,String endTime);

}
