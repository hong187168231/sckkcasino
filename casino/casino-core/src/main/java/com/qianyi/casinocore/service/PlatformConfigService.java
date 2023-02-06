package com.qianyi.casinocore.service;

import com.qianyi.casinocore.exception.BusinessException;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.repository.PlatformConfigRepository;
import com.qianyi.casinocore.util.CommonConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PlatformConfigService {

    @Autowired
    private PlatformConfigRepository platformConfigRepository;

    public List<PlatformConfig> findAll() {
        return platformConfigRepository.findAll();
    }

    @Cacheable(cacheNames = "platformConfig")
    public PlatformConfig findFirst() {
        List<PlatformConfig> configList = platformConfigRepository.findAll();
        if (!CollectionUtils.isEmpty(configList)) {
            return configList.get(0);
        }
        return null;
    }

    @Async("asyncExecutor")
    @CacheEvict(cacheNames = "platformConfig", allEntries = true)
    public void reception(Integer type, BigDecimal amount) {
        updateTotalPlatformQuota(type, amount);
    }

    @CacheEvict(cacheNames = "platformConfig", allEntries = true)
    public void backstage(Integer type, BigDecimal amount) {
        updateTotalPlatformQuota(type, amount);
    }

    /**
     * @param type   0:减 1:加
     * @param amount 操作金额
     */
    public synchronized void updateTotalPlatformQuota(Integer type, BigDecimal amount) {
        PlatformConfig platformConfig = findFirst();
        if (type.equals(CommonConst.NUMBER_0)) {
            platformConfig.setTotalPlatformQuota(platformConfig.getTotalPlatformQuota().subtract(amount));
        } else {
            platformConfig.setTotalPlatformQuota(platformConfig.getTotalPlatformQuota().add(amount));
        }
        if (platformConfig.getVipConfigInfo() != null && platformConfig.getVipConfigInfo().equals("")) {
            throw new BusinessException("vip配置不能为空");
        }
        platformConfigRepository.save(platformConfig);
    }

    public Boolean queryTotalPlatformQuota() {
        PlatformConfig platformConfig = findFirst();
        if (platformConfig.getTotalPlatformQuota().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return true;
    }


    @CacheEvict(cacheNames = "platformConfig", allEntries = true)
    public void save(PlatformConfig platformConfig) {
        if (platformConfig.getVipConfigInfo() != null && platformConfig.getVipConfigInfo().equals("")) {
            throw new BusinessException("vip配置不能为空");
        }
        platformConfigRepository.save(platformConfig);
    }
}