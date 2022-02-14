package com.qianyi.casinocore.service;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.repository.UserMoneyRepository;
import com.qianyi.casinocore.util.CommonConst;
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
@CacheConfig(cacheNames = {"userMoney"})
public class UserMoneyService {

    @Autowired
    private UserMoneyRepository userMoneyRepository;

    @Autowired
    private PlatformConfigService platformConfigService;

    private String refreshUrl = "/wm/getWmBalanceApi?";

    private String recycleUrl = "/wm/oneKeyRecoverApi?";

    public UserMoney findUserByUserIdUseLock(Long userId) {
      // return userMoneyRepository.findUserByUserIdUseLock(userId);
       return userMoneyRepository.findUserMoneyByUserId(userId);
    }


    public UserMoney findUserByUserIdUse(Long userId) {
        return userMoneyRepository.findByUserId(userId);
        // return userMoneyRepository.findUserMoneyByUserId(userId);
    }

    public List<UserMoney> saveAll(List<UserMoney> userMoneyList){
        return userMoneyRepository.saveAll(userMoneyList);
    }

    @CacheEvict(key = "#userId")
    @Transactional
    public void changeProfit(Long userId,BigDecimal shareProfit){
        userMoneyRepository.changeProfit(userId,shareProfit);
    }

    /**
     *
     * @param userId 用户id
     * @param money 金额
     */
    @CacheEvict(key = "#userId")
    @Transactional
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
    @Transactional
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
    @Transactional
    public void subCodeNum(Long userId, BigDecimal codeNum) {
        synchronized (userId) {
            userMoneyRepository.subCodeNum(userId, codeNum);
        }
    }

    /**
     * 增加洗码金额
     * @param userId 用户id
     * @param washCode 洗码金额
     */
    @CacheEvict(key = "#userId")
    @Transactional
    public void addWashCode(Long userId, BigDecimal washCode) {
        synchronized (userId) {
            userMoneyRepository.addWashCode(userId, washCode);
        }
    }


    /**
     * 扣减洗码金额
     * @param userId 用户id
     * @param washCode 冻结金额
     */
    @CacheEvict(key = "#userId")
    @Transactional
    public void subWashCode(Long userId, BigDecimal washCode) {
        synchronized (userId) {
            userMoneyRepository.subWashCode(userId, washCode);
        }
    }

    @CacheEvict(key = "#userId")
    @Transactional
    public void addCodeNum(Long userId, BigDecimal codeNum) {
        synchronized (userId) {
            userMoneyRepository.addCodeNum(userId, codeNum);
        }
    }


    /**
     * 增加分润金额
     * @param userId 用户id
     * @param shareProfit 分润金额
     */
    @CacheEvict(key = "#userId")
    @Transactional
    public void addShareProfit(Long userId, BigDecimal shareProfit) {
        synchronized (userId) {
            userMoneyRepository.addShareProfit(userId, shareProfit);
        }
    }


    /**
     * 扣减分润金额
     * @param userId 用户id
     * @param shareProfit 分润金额
     */
    @CacheEvict(key = "#userId")
    @Transactional
    public void subShareProfit(Long userId, BigDecimal shareProfit) {
        synchronized (userId) {
            userMoneyRepository.subShareProfit(userId, shareProfit);
        }
    }

    @Cacheable(key = "#userId")
    public UserMoney findByUserId(Long userId) {
        UserMoney userMoney = userMoneyRepository.findByUserId(userId);
        if (userMoney != null) {
            BigDecimal defaultVal = BigDecimal.ZERO;
            BigDecimal money = userMoney.getMoney() == null ? defaultVal : userMoney.getMoney();
            userMoney.setMoney(money);
            BigDecimal washCode = userMoney.getWashCode() == null ? defaultVal : userMoney.getWashCode();
            userMoney.setWashCode(washCode);
            BigDecimal codeNum = userMoney.getCodeNum() == null ? defaultVal : userMoney.getCodeNum();
            userMoney.setCodeNum(codeNum);
            BigDecimal freezeMoney = userMoney.getFreezeMoney() == null ? defaultVal : userMoney.getFreezeMoney();
            userMoney.setFreezeMoney(freezeMoney);
        }
        return userMoney;
    }

    @CachePut(key="#userMoney.userId")
    @Transactional
    public UserMoney save(UserMoney userMoney) {
        return userMoneyRepository.save(userMoney);
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
    public JSONObject getWMonetUser(User user, UserThird third) {
        Integer lang = user.getLanguage();
        if (lang == null) {
            lang = 0;
        }
        try {
            String param = "account={0}&lang={1}";
            param = MessageFormat.format(param,third.getAccount(),lang);
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null?"":first.getWebConfiguration();
            WMurl = WMurl + refreshUrl;
            String s = HttpClient4Util.get(WMurl + param);
            log.info("{}查询web接口返回{}",user.getAccount(),s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
//            Object data = parse.get("data");
//            return new BigDecimal(data.toString());
        } catch (Exception e) {
            return null;
        }
    }
    public JSONObject oneKeyRecover(User user){
        try {
            String param = "userId={0}";
            param = MessageFormat.format(param,user.getId().toString());
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null?"":first.getWebConfiguration();
            WMurl = WMurl + recycleUrl;
            String s = HttpClient4Util.get(WMurl + param);
            log.info("{}回收余额web接口返回{}",user.getAccount(),s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }
}
