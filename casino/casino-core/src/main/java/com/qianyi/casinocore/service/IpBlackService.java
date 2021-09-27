package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.IpBlack;
import com.qianyi.casinocore.repository.IpBlackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@CacheConfig(cacheNames = {"ipBlackList"})
public class IpBlackService {
	
    @Autowired
    private IpBlackRepository ipBlackRepository;

    @Transactional
    @CachePut(key="#po.ip")
    public IpBlack save(IpBlack po){
        return ipBlackRepository.save(po);
    }

    @Cacheable(key = "#ip")
    public IpBlack findByIp(String ip) {
        return ipBlackRepository.findByIp(ip);
    }

}
