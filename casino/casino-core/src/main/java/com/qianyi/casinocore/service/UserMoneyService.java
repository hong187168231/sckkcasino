package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.repository.UserMoneyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;

@Service
@Transactional
@CacheConfig(cacheNames = {"userMoney"})
public class UserMoneyService {

    @Autowired
    private UserMoneyRepository userMoneyRepository;

    public UserMoney findUserByUserIdUseLock(Long userId) {
        return userMoneyRepository.findUserByUserIdUseLock(userId);
    }

    @CacheEvict(key = "#userId")
    public void updateCodeNum(Long userId, BigDecimal codeNum) {
        synchronized (userId) {
            userMoneyRepository.updateCodeNum(userId, codeNum);
        }
    }
}
