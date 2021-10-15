package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.ProxyRebateConfig;
import com.qianyi.casinocore.repository.ProxyRebateConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class ProxyRebateConfigService {

    @Autowired
    private ProxyRebateConfigRepository configRepository;


    public ProxyRebateConfig findFirst() {
        List<ProxyRebateConfig> configList = configRepository.findAll();
        if (!CollectionUtils.isEmpty(configList)) {
            return configList.get(0);
        }
        return null;
    }

    public void save(ProxyRebateConfig proxyRebateConfig) {
        configRepository.save(proxyRebateConfig);
    }
}
