package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.repository.ProxyUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = {"proxyUser"})
public class ProxyUserService {

    @Autowired
    private ProxyUserRepository proxyUserRepository;

    public List<ProxyUser> findAll() {
        return proxyUserRepository.findAll();
    }

    public ProxyUser findByUserName(String userName) {
        return proxyUserRepository.findByUserName(userName);
    }

    public void setSecretById(Long id, String gaKey) {
        proxyUserRepository.setSecretById(id, gaKey);
    }
    @CachePut(key="#result.id",condition = "#result != null")
    public void save(ProxyUser proxyUser) {
        proxyUserRepository.save(proxyUser);
    }
    @Cacheable(key = "#id")
    public ProxyUser findAllById(Long id){
        return proxyUserRepository.findAllById(id);
    }
    @Cacheable(key = "#id")
    public ProxyUser findById(Long id){
        Optional<ProxyUser> optional = proxyUserRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
}
