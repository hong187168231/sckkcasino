package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.HomePageReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HomePageReportRepository  extends JpaRepository<HomePageReport, Long>, JpaSpecificationExecutor<HomePageReport> {
}
