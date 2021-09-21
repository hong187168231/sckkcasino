package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.AmountConfig;
import com.qianyi.casinocore.repository.AmountConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AmountConfigService {
    @Autowired
    private AmountConfigRepository amountConfigRepository;

    public AmountConfig findAmountConfigById(Long id){
        Optional<AmountConfig> optional = amountConfigRepository.findById(id);
        if (optional.isPresent()){
            return optional.get();
        }
        return null;
    }
    public void save(AmountConfig amountConfig){
        amountConfigRepository.save(amountConfig);
    }
}
