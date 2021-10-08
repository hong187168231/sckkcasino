package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ProxyReport;
import com.qianyi.casinocore.repository.ProxyReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProxyReportService {
    @Autowired
    private ProxyReportRepository proxyReportRepository;

    public ProxyReport findByUserId(Long userId){
        return proxyReportRepository.findProxyReportByUserId(userId);
    }

    public ProxyReport save(ProxyReport proxyReport){
        return proxyReportRepository.save(proxyReport);
    }
}
