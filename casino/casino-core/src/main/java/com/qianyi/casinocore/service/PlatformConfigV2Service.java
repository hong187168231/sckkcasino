package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.PlatformConfigV2;
import com.qianyi.casinocore.repository.PlatformConfigV2Repository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@Slf4j
public class PlatformConfigV2Service {

    @Autowired
    PlatformConfigV2Repository platformConfigV2Repository;

    public List<PlatformConfigV2> findAll() {
        return platformConfigV2Repository.findAll();
    }

    @Cacheable(cacheNames = "platformConfigV2")
    public PlatformConfigV2 findFirst() {
        List<PlatformConfigV2> configList = platformConfigV2Repository.findAll();
        if (!CollectionUtils.isEmpty(configList)) {
            return configList.get(0);
        }
        return null;
    }

    /**
     * operator 操作人(可空)
     * service 服务名称比如 admin web proxy report
     * @return
     */
    @CacheEvict(cacheNames = "platformConfigV2", allEntries = true)
    public void save(PlatformConfigV2 platformConfigV2,String operator,String service) {
        log.error("{}修改充值凭证开关service:{}=======>:{}",operator,service,platformConfigV2.toString());
        platformConfigV2Repository.save(platformConfigV2);
    }
}
