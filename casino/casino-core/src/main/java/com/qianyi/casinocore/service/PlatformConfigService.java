package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.repository.PlatformConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class PlatformConfigService {

    @Autowired
    private PlatformConfigRepository platformConfigRepository;

    public List<PlatformConfig> findAll() {
        return platformConfigRepository.findAll();
    }

    public PlatformConfig findFirst() {
        List<PlatformConfig> configList = platformConfigRepository.findAll();
        if (!CollectionUtils.isEmpty(configList)) {
            return configList.get(0);
        }
        return null;
    }

    public void save(PlatformConfig platformConfig) {
        platformConfigRepository.save(platformConfig);
    }
}
