package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ProxyRebateConfig;
import com.qianyi.casinocore.repository.ProxyRebateConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProxyRebateConfigService {
    @Autowired
    private ProxyRebateConfigRepository rebateConfigRepository;

    public ProxyRebateConfig save(ProxyRebateConfig proxyRebateConfig) {
        return rebateConfigRepository.save(proxyRebateConfig);
    }

    public ProxyRebateConfig findById(Long proxyUserId) {
        ProxyRebateConfig byProxyUserId = rebateConfigRepository.findByProxyUserId(proxyUserId);
        return byProxyUserId;
    }
    public void deleteById(Long proxyUserId) {
        rebateConfigRepository.deleteById(proxyUserId);
    }
}
