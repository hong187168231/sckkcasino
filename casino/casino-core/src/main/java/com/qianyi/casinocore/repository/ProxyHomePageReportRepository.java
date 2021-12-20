package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ProxyHomePageReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProxyHomePageReportRepository extends JpaRepository<ProxyHomePageReport, Long>, JpaSpecificationExecutor<ProxyHomePageReport> {
    List<ProxyHomePageReport> findByStaticsTimes(String staticsTimes);
}
