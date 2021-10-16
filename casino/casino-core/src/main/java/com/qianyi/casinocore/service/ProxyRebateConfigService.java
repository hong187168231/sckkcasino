package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ProxyRebateConfig;
import com.qianyi.casinocore.repository.ProxyRebateConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class ProxyRebateConfigService {

    @Autowired
    private ProxyRebateConfigRepository configRepository;
    public List<ProxyRebateConfig> findAll(){
        return configRepository.findAll();
    }

    @Cacheable(value = "proxyRebateConfig")
    public ProxyRebateConfig findFirst() {
        List<ProxyRebateConfig> configList = configRepository.findAll();
        if (!CollectionUtils.isEmpty(configList)) {
            return configList.get(0);
        }
        return null;
    }
    @CacheEvict(value = "proxyRebateConfig")
    public ProxyRebateConfig save(ProxyRebateConfig proxyRebateConfig) {
        return configRepository.save(proxyRebateConfig);
    }
}
