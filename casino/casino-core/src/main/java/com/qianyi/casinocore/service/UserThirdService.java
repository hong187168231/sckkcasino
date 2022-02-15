package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.repository.UserThirdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
@CacheConfig(cacheNames = {"userThird"})
public class UserThirdService {
    @Autowired
    UserThirdRepository userThirdRepository;

    @Cacheable(key = "#p0")
    public UserThird findByUserId(Long userId) {
        return userThirdRepository.findByUserId(userId);
    }

    @Caching(
            put = {@CachePut(key = "#result.userId"), @CachePut(key = "#result.account")}
    )
    public UserThird save(UserThird third) {
        return userThirdRepository.save(third);
    }

    @Cacheable(key = "#p0")
    public UserThird findByAccount(String account) {
        return userThirdRepository.findByAccount(account);
    }

    @Cacheable(key = "#p0")
    public UserThird findByGoldenfAccount(String account) {
        return userThirdRepository.findByGoldenfAccount(account);
    }
}
