package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.BetRatioConfig;
import com.qianyi.casinocore.repository.BetRatioConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public BetRatioConfig findById(Long id) {
        Optional<BetRatioConfig> optional = betRatioConfigRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public void save(BetRatioConfig betRatioConfig) {
        betRatioConfigRepository.save(betRatioConfig);
    }
}
