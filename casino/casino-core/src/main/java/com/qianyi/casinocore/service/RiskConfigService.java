package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.RiskConfig;
import com.qianyi.casinocore.repository.RiskConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RiskConfigService {
    @Autowired
    private RiskConfigRepository riskConfigRepository;

    public List<RiskConfig> findAll(){
        return riskConfigRepository.findAll();
    }

    public RiskConfig save(RiskConfig riskConfig){
        return riskConfigRepository.save(riskConfig);
    }
}
