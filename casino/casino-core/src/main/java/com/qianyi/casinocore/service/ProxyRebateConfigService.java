package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ProxyRebateConfig;
import com.qianyi.casinocore.repository.ProxyRebateConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheNames = {"proxyRebateConfig"})
public class ProxyRebateConfigService {
    @Autowired
    private ProxyRebateConfigRepository rebateConfigRepository;
    @CachePut(key="#result.proxyUserId",condition = "#result != null")
    public ProxyRebateConfig save(ProxyRebateConfig proxyRebateConfig) {
        return rebateConfigRepository.save(proxyRebateConfig);
    }
    @Cacheable(key = "#proxyUserId")
    public ProxyRebateConfig findById(Long proxyUserId) {
        ProxyRebateConfig byProxyUserId = rebateConfigRepository.findByProxyUserId(proxyUserId);
        return byProxyUserId;
    }
    @CacheEvict(key = "#proxyUserId")
    public void delete(Long proxyUserId,ProxyRebateConfig proxyRebateConfig) {
        rebateConfigRepository.delete(proxyRebateConfig);
    }
}
