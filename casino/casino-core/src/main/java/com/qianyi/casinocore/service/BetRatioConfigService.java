package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.BetRatioConfig;
import com.qianyi.casinocore.repository.BetRatioConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BetRatioConfigService {

    @Autowired
    private BetRatioConfigRepository betRatioConfigRepository;

    public BetRatioConfig findOneBetRatioConfig() {
        List<BetRatioConfig> betRatioConfigList = betRatioConfigRepository.findAll();
        if(betRatioConfigList !=null && betRatioConfigList.size() > 0){
            return betRatioConfigList.get(0);
        }
        return null;
    }

    public List<BetRatioConfig> findAll() {
        List<BetRatioConfig> betRatioConfigList = betRatioConfigRepository.findAll();
        return betRatioConfigList;
    }
}
