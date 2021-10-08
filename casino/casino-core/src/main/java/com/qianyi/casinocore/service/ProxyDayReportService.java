package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ProxyDayReport;
import com.qianyi.casinocore.repository.ProxyDayReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProxyDayReportService {
    @Autowired
    private ProxyDayReportRepository proxyDayReportRepository;

    public ProxyDayReport findByUserIdAndDay(Long userId,String day){
        return proxyDayReportRepository.findProxyDayReportByUserIdAndAndDayTime(userId,day);
    }

    public ProxyDayReport save(ProxyDayReport proxyDayReport){
        return proxyDayReportRepository.save(proxyDayReport);
    }
}
