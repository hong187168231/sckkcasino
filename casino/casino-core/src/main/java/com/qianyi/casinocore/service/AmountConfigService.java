package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.AmountConfig;
import com.qianyi.casinocore.repository.AmountConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
//@CacheConfig(cacheNames = {"amountConfig"})
public class AmountConfigService {
    @Autowired
    private AmountConfigRepository amountConfigRepository;
//    @Cacheable(key = "#id")
    public AmountConfig findAmountConfigById(Long id){
        Optional<AmountConfig> optional = amountConfigRepository.findById(id);
        if (optional.isPresent()){
            return optional.get();
        }
        return null;
    }
//    @CachePut(key="#result.id",condition = "#result != null")
    public void save(AmountConfig amountConfig){
        amountConfigRepository.save(amountConfig);
    }
}
