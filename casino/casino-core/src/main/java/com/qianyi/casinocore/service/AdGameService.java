package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.AdGame;
import com.qianyi.casinocore.repository.AdGameRepository;
import com.qianyi.modulecommon.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = {"adGame"})
public class AdGameService {

    @Autowired
    private AdGameRepository adGameRepository;

    @Cacheable(key = "'" + Constants.REDIS_GAMECODE + "'+#p0")
    public List<AdGame> findByGameCode(String gameCode){
        return adGameRepository.findByGameCode(gameCode);
    }


}
