package com.qianyi.casinoproxy.repository;

import com.qianyi.casinoproxy.model.ProxyHomePageReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProxyHomePageReportRepository extends JpaRepository<ProxyHomePageReport, Long>, JpaSpecificationExecutor<ProxyHomePageReport> {
}
