package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ProxyDayReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProxyDayReportRepository extends JpaRepository<ProxyDayReport,Long>, JpaSpecificationExecutor<ProxyDayReport> {

    public ProxyDayReport findProxyDayReportByUserIdAndAndDayTime(Long userId,String dayTime);
}
