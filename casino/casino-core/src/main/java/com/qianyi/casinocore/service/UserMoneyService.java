package com.qianyi.casinocore.service;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.repository.UserMoneyRepository;
import com.qianyi.modulecommon.util.HttpClient4Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
@CacheConfig(cacheNames = {"userMoney"})
public class UserMoneyService {

    @Autowired
    private UserMoneyRepository userMoneyRepository;

    private String url = "http://127.0.0.1:9200/wm/getWmBalanceApi?";

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
     * 增加冻结余额
     * @param userId 用户id
     * @param freezeMoney 冻结金额
     */
    @CacheEvict(key = "#userId")
    public void addFreezeMoney(Long userId, BigDecimal freezeMoney) {
        synchronized (userId) {
            userMoneyRepository.addFreezeMoney(userId, freezeMoney);
        }
    }


    /**
     * 扣减冻结余额
     * @param userId 用户id
     * @param freezeMoney 冻结金额
     */
    @CacheEvict(key = "#userId")
    public void subFreezeMoney(Long userId, BigDecimal freezeMoney) {
        synchronized (userId) {
            userMoneyRepository.subFreezeMoney(userId, freezeMoney);
        }
    }

    @CacheEvict(key = "#userId")
    public void addCodeNum(Long userId, BigDecimal codeNum) {
        synchronized (userId) {
            userMoneyRepository.addCodeNum(userId, codeNum);
        }
    }

    @Cacheable(key = "#userId")
    public UserMoney findByUserId(Long userId){
        return userMoneyRepository.findByUserId(userId);
    }

    @CachePut(key="#userMoney.userId")
    public void save(UserMoney userMoney) {
        userMoneyRepository.save(userMoney);
    }

    public List<UserMoney> findAll(List<Long> userIds) {
        Specification<UserMoney> condition = getCondition(userIds);
        List<UserMoney> userMoneyList = userMoneyRepository.findAll(condition);
        return userMoneyList;
    }

    private Specification<UserMoney> getCondition(List<Long> userIds) {
        Specification<UserMoney> specification = new Specification<UserMoney>() {
            @Override
            public Predicate toPredicate(Root<UserMoney> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                Predicate predicate = cb.conjunction();
                if (userIds != null && userIds.size() > 0) {
                    Path<Object> userId = root.get("userId");
                    CriteriaBuilder.In<Object> in = cb.in(userId);
                    for (Long id : userIds) {
                        in.value(id);
                    }
                    list.add(cb.and(cb.and(in)));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
    public BigDecimal getWMonetUser(User user, UserThird third) {
        Integer lang = user.getLanguage();
        if (lang == null) {
            lang = 0;
        }
        try {
            String param = "account={0}&lang={1}";
            param = MessageFormat.format(param,third.getAccount(),lang);
            String s = HttpClient4Util.doGet(url + param);
            log.info("{}查询web接口返回{}",user.getAccount(),s);
            JSONObject parse = JSONObject.parseObject(s);
            Object data = parse.get("data");
            return new BigDecimal(data.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
