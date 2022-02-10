package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.PromoteCommissionConfig;
import com.qianyi.casinocore.repository.PromoteCommissionConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromoteCommissionConfigService {

    @Autowired
    private PromoteCommissionConfigRepository promoteCommissionConfigRepository;

    public PromoteCommissionConfig findByGameType(Integer gameType) {
        return promoteCommissionConfigRepository.findByGameType(gameType);
    }
    public List<PromoteCommissionConfig> findAll() {
        return promoteCommissionConfigRepository.findAll();
    }

    public void save(List<PromoteCommissionConfig> promoteCommissionConfig) {
        promoteCommissionConfigRepository.saveAll(promoteCommissionConfig);
    }

}
