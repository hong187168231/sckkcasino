package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ProxyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProxyReportRepository extends JpaRepository<ProxyReport,Long>, JpaSpecificationExecutor<ProxyReport> {

    ProxyReport findProxyReportByUserId(Long userId);
}
