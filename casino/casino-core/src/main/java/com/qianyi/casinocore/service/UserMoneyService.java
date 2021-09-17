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
     * @param money 金额
     */
    @CacheEvict(key = "#userId")
    public void subMoney(Long userId, BigDecimal money) {
        synchronized (userId) {
            userMoneyRepository.subMoney(userId, money);
        }
    }

    public Page<UserMoney> findUserMoneyPage(Specification<UserMoney> condition, Pageable pageable){
        return userMoneyRepository.findAll(condition,pageable);
    }

    /**
     *
     * @param userId 用户id
     * @param money 金额
     */
    @CacheEvict(key = "#userId")
    public void addMoney(Long userId, BigDecimal money) {
        synchronized (userId) {
            userMoneyRepository.addMoney(userId, money);
        }
    }

    /**
     *
     * @param userId 用户id
     * @param codeNum 打码量
     */
    @CacheEvict(key = "#userId")
    public void subCodeNum(Long userId, BigDecimal codeNum) {
        synchronized (userId) {
            userMoneyRepository.subCodeNum(userId, codeNum);
        }
    }

    /**
     *
     * @param userId 用户id
     * @param codeNum 打码量
     */
    @CacheEvict(key = "#userId")
    public void addCodeNum(Long userId, BigDecimal codeNum) {
        synchronized (userId) {
            userMoneyRepository.addCodeNum(userId, codeNum);
        }
    }
    public UserMoney findByUserId(Long userId){
        return userMoneyRepository.findByUserId(userId);
    }

    public void save(UserMoney userMoney) {
        userMoneyRepository.save(userMoney);
    }
}
