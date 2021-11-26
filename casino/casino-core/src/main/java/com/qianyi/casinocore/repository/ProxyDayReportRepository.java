package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ProxyDayReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;

public interface ProxyDayReportRepository extends JpaRepository<ProxyDayReport,Long>, JpaSpecificationExecutor<ProxyDayReport> {

    ProxyDayReport findProxyDayReportByUserIdAndDayTime(Long userId,String dayTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    ProxyDayReport findByUserIdAndDayTime(Long userId,String dayTime);
}
