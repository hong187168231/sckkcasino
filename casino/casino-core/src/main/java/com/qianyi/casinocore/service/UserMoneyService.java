package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.repository.UserMoneyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    /**
     *
     * @param userId 用户id
     * @param codeNum 打码量 充值传正数，扣减传负数
     */
    @CacheEvict(key = "#userId")
    public void updateCodeNum(Long userId, BigDecimal codeNum) {
        synchronized (userId) {
            userMoneyRepository.updateCodeNum(userId, codeNum);
        }
    }

    /**
     *
     * @param userId 用户id
     * @param money 用户金额
     */
    @CacheEvict(key = "#userId")
    public void updateMoney(Long userId, BigDecimal money) {
        synchronized (userId) {
            userMoneyRepository.updateMoney(userId, money);
        }
    }

    public Page<UserMoney> findUserMoneyPage(Specification<UserMoney> condition, Pageable pageable){
        return userMoneyRepository.findAll(condition,pageable);
    }
}
