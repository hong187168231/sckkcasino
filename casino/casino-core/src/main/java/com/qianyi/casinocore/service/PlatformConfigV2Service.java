package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.PlatformConfigV2;
import com.qianyi.casinocore.repository.PlatformConfigV2Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class PlatformConfigV2Service {


    PlatformConfigV2Repository platformConfigV2Repository;


    public PlatformConfigV2 findFirst() {
        List<PlatformConfigV2> configList = platformConfigV2Repository.findAll();
        if (!CollectionUtils.isEmpty(configList)) {
            return configList.get(0);
        }
        return null;
    }
}
