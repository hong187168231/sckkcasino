package com.qianyi.casinocore.service;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.exception.UserMoneyChangeException;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.repository.UserMoneyRepository;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.HttpClient4Util;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@CacheConfig(cacheNames = {"userMoney"})
public class UserMoneyService {

    @Autowired
    private UserMoneyRepository userMoneyRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserService userService;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private PlatformConfigService platformConfigService;

    private String refreshUrl = "/wm/getWmBalanceApi?";

    private String recycleUrl = "/wm/oneKeyRecoverApi?";

    private String PG_refreshUrl = "/golednf/getBalanceApi?";

    private String PG_recycleUrl = "/golednf/oneKeyRecoverApi?";

    private String OB_refreshUrl = "/obdjGame/getBalanceApi?";

    private String AE_refreshUrl = "/aeGame/getBalanceApi?";

    private String OB_recycleUrl = "/obdjGame/oneKeyRecoverApi?";

    private String AE_recycleUrl = "/aeGame/oneKeyRecoverApi?";

    private String OBTY_refreshUrl = "/obtyGame/getBalanceApi?";

    private String OBTY_recycleUrl = "/obtyGame/oneKeyRecoverApi?";

    private String VNC_refreshUrl = "/vncGame/getBalanceApi?";

    private String VNC_recycleUrl = "/vncGame/oneKeyRecoverApi?";

    public UserMoney findUserByUserIdUseLock(Long userId) {
        // return userMoneyRepository.findUserByUserIdUseLock(userId);
        return userMoneyRepository.findByUserId(userId);
    }

    public UserMoney findUserByUserIdNotLock(Long userId) {
        return userMoneyRepository.findByUserId(userId);
    }

    public UserMoney findUserByUserIdUse(Long userId) {
        return userMoneyRepository.findByUserId(userId);
        // return userMoneyRepository.findUserMoneyByUserId(userId);
    }

    public List<UserMoney> saveAll(List<UserMoney> userMoneyList) {
        return userMoneyRepository.saveAll(userMoneyList);
    }

    @CacheEvict(key = "#userId")
    /*    @Transactional*/ public void changeProfit(Long userId, BigDecimal shareProfit) {
        userMoneyRepository.changeProfit(userId, shareProfit);
    }

    /**
     * @param userId 用户id
     * @param money 金额
     */
    //    @CacheEvict(key = "#userId")
    //    @Transactional
    //    public void subMoney(Long userId, BigDecimal money) {
    //        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
    //        try {
    //            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
    //            UserMoney userMoneyLock2 = findUserByUserIdUseLock(userId);
    //            // 扣减余额大于剩余余额
    //            if (money.compareTo(userMoneyLock2.getMoney()) == 1) {
    //                redisUtil.delete(RedisUtil.USERMONEY_KEY + userId);
    //                throw new UserMoneyChangeException("扣减余额超过本地剩余额度");
    //            }
    //            userMoneyRepository.subMoney(userId, money);
    //        } catch (Exception e) {
    //            throw new RuntimeException(e);
    //        } finally {
    //            // 释放锁
    //            RedisKeyUtil.unlock(userMoneyLock);
    //            log.info("subMoney用户减少money释放锁{}", userId);
    //        }
    //    }
    @CacheEvict(key = "#userId")
    @org.springframework.transaction.annotation.Transactional
    public void subMoney(Long userId, BigDecimal money,UserMoney userMoney) {
        // 扣减余额大于剩余余额
        if (money.compareTo(userMoney.getMoney()) == 1) {
            redisUtil.delete(RedisUtil.USERMONEY_KEY + userId);
            throw new UserMoneyChangeException("扣减余额超过本地剩余额度");
        }
        userMoneyRepository.subMoney(userId, money);
    }

    public Page<UserMoney> findUserMoneyPage(Specification<UserMoney> condition, Pageable pageable) {
        return userMoneyRepository.findAll(condition, pageable);
    }

    /**
     * @param userId 用户id
     * @param money 金额
     */
    //    @CacheEvict(key = "#userId")
    //    @Transactional
    //    public void addMoney(Long userId, BigDecimal money) {
    //        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
    //        try {
    //            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
    //            userMoneyRepository.addMoney(userId, money);
    //        } catch (Exception e) {
    //            throw new RuntimeException(e);
    //        } finally {
    //            // 释放锁
    //            RedisKeyUtil.unlock(userMoneyLock);
    //            log.info("addMoney用户增加money释放锁{}", userId);
    //        }
    //    }
    @CacheEvict(key = "#userId")
    @org.springframework.transaction.annotation.Transactional
    public void addMoney(Long userId, BigDecimal money) {
        userMoneyRepository.addMoney(userId, money);
    }
    // /**
    // * @param userId 用户id
    // * @param integral 积分
    // */
    // @CacheEvict(key = "#userId")
    // @Transactional
    // public void subIntegral(Long userId, BigDecimal integral) {
    // RReadWriteLock lock = redissonClient.getReadWriteLock(RedisLockConstant.MONEY + userId);
    // boolean bool;
    // try {
    // bool = lock.writeLock().tryLock(2, 5, TimeUnit.SECONDS);
    // if (bool) {
    // UserMoney userMoneyLock = findUserByUserIdUseLock(userId);
    // //扣减余额大于剩余余额
    // if (integral.compareTo(userMoneyLock.getIntegral()) == 1) {
    // redisUtil.delete(RedisUtil.USERMONEY_KEY + userId);
    // throw new UserMoneyChangeException("扣减积分超过本地剩余额度");
    // }
    // userMoneyRepository.subIntegral(userId, integral);
    // } else {
    // log.error("subIntegral 用户扣减integral没拿到锁,{}", userId);
    // throw new BusinessException("操作integral失败");
    // }
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // } finally {
    // // 释放锁
    // lock.writeLock().unlock();
    // log.info("subIntegral 用户扣减integral释放锁", userId);
    // }
    //
    // }

    // /**
    // * @param userId 用户id
    // * @param integral 积分
    // */
    // @CacheEvict(key = "#userId")
    // @Transactional
    // public void addIntegral(Long userId, BigDecimal integral) {
    // RReadWriteLock lock = redissonClient.getReadWriteLock(RedisLockConstant.MONEY + userId);
    // boolean bool;
    // try {
    // bool = lock.writeLock().tryLock(2, 5, TimeUnit.SECONDS);
    // if (bool) {
    // userMoneyRepository.addIntegral(userId, integral);
    // } else {
    // log.error("addIntegral 用户增加integral没拿到锁,{}", userId);
    // throw new BusinessException("操作integral失败");
    // }
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // } finally {
    // // 释放锁
    // lock.writeLock().unlock();
    // log.info("addIntegral 用户增加money释放锁", userId);
    // }
    // }

    /**
     * 增加实时余额和打码量
     *
     * @param userId 用户id
     * @param money 余额
     * @param codeNum 打码量
     * @param balance 实时余额
     */
    // @CacheEvict(key = "#userId")
    // @Transactional
    // public void addBalanceAndCodeNumAndMoney(Long userId, BigDecimal money, BigDecimal codeNum, BigDecimal balance,
    // Integer isFirst) {
    // String moneySuffix = RedisLockConstant.MONEY + userId;
    // if (redisUtil.hasKey(moneySuffix)) {
    // throw new BusinessException("金额修改频繁,请稍后再试!");
    // }
    // userMoneyRepository.addBalanceAndCodeNumAndMoney(userId, money, codeNum, balance, isFirst);
    // }
    @CacheEvict(key = "#userId")
    @org.springframework.transaction.annotation.Transactional
    public void addBalanceAndCodeNumAndMoney(Long userId, BigDecimal money, BigDecimal codeNum, BigDecimal balance,
        Integer isFirst) {
        userMoneyRepository.addBalanceAndCodeNumAndMoney(userId, money, codeNum, balance, isFirst);
    }

    /**
     * @param userId 用户id
     * @param codeNum 打码量
     */
    // @CacheEvict(key = "#userId")
    // @Transactional
    // public void subCodeNum(Long userId, BigDecimal codeNum) {
    // RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
    // try {
    // userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
    // UserMoney userMoneyLock2 = findUserByUserIdUseLock(userId);
    // //扣减余额大于剩余余额
    // if (codeNum.compareTo(userMoneyLock2.getCodeNum()) == 1) {
    // redisUtil.delete(RedisUtil.USERMONEY_KEY + userId);
    // throw new UserMoneyChangeException("扣减打码量超过本地剩余额度");
    // }
    // userMoneyRepository.subCodeNum(userId, codeNum);
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // } finally {
    // // 释放锁
    // RedisKeyUtil.unlock(userMoneyLock);
    // log.info("subCodeNum 用户增加codeNum释放锁", userId);
    // }
    // }
    @CacheEvict(key = "#userId")
    @org.springframework.transaction.annotation.Transactional
    public void subCodeNum(Long userId, BigDecimal codeNum, UserMoney userMoney) {
        // 扣减余额大于剩余余额
        if (codeNum.compareTo(userMoney.getCodeNum()) == 1) {
            redisUtil.delete(RedisUtil.USERMONEY_KEY + userId);
            throw new UserMoneyChangeException("扣减打码量超过本地剩余额度");
        }
        userMoneyRepository.subCodeNum(userId, codeNum);
    }

    /**
     * 增加洗码金额
     *
     * @param userId 用户id
     * @param washCode 洗码金额
     */
    // @CacheEvict(key = "#userId")
    // @Transactional
    // public void addWashCode(Long userId, BigDecimal washCode) {
    // RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
    // try {
    // userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
    // userMoneyRepository.addWashCode(userId, washCode);
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // } finally {
    // // 释放锁
    // RedisKeyUtil.unlock(userMoneyLock);
    // log.info("addWashCode 用户增加washCode释放锁", userId);
    // }
    // }
    @CacheEvict(key = "#userId")
    @org.springframework.transaction.annotation.Transactional
    public void addWashCode(Long userId, BigDecimal washCode) {
        userMoneyRepository.addWashCode(userId, washCode);
    }

    /**
     * 扣减洗码金额
     *
     * @param userId 用户id
     * @param washCode 冻结金额
     */
    //    @CacheEvict(key = "#userId")
    //    @Transactional
    //    public void subWashCode(Long userId, BigDecimal washCode) {
    //        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
    //        try {
    //            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
    //            UserMoney userMoneyLock2 = findUserByUserIdUseLock(userId);
    //            // 扣减余额大于剩余余额
    //            if (washCode.compareTo(userMoneyLock2.getWashCode()) == 1) {
    //                redisUtil.delete(RedisUtil.USERMONEY_KEY + userId);
    //                throw new UserMoneyChangeException("扣减洗码额超过本地剩余额度");
    //            }
    //            userMoneyRepository.subWashCode(userId, washCode);
    //        } catch (Exception e) {
    //            throw new RuntimeException(e);
    //        } finally {
    //            // 释放锁
    //            RedisKeyUtil.unlock(userMoneyLock);
    //            log.info("subWashCode 用户扣减washCode释放锁", userId);
    //        }
    //    }
    @CacheEvict(key = "#userId")
    @org.springframework.transaction.annotation.Transactional
    public void subWashCode(Long userId, BigDecimal washCode,UserMoney userMoney) {
        // 扣减余额大于剩余余额
        if (washCode.compareTo(userMoney.getWashCode()) == 1) {
            redisUtil.delete(RedisUtil.USERMONEY_KEY + userId);
            throw new UserMoneyChangeException("扣减洗码额超过本地剩余额度");
        }
        userMoneyRepository.subWashCode(userId, washCode);
    }
    //    @CacheEvict(key = "#userId")
    //    @Transactional
    //    public void addCodeNum(Long userId, BigDecimal codeNum) {
    //        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
    //        try {
    //            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
    //            userMoneyRepository.addCodeNum(userId, codeNum);
    //        } catch (Exception e) {
    //            throw new RuntimeException(e);
    //        } finally {
    //            // 释放锁
    //            RedisKeyUtil.unlock(userMoneyLock);
    //            log.info("addCodeNum 用户增加codeNum释放锁", userId);
    //        }
    //    }
    @CacheEvict(key = "#userId")
    @org.springframework.transaction.annotation.Transactional
    public void addCodeNum(Long userId, BigDecimal codeNum) {
        userMoneyRepository.addCodeNum(userId, codeNum);
    }
    /**
     * 增加分润金额
     *
     * @param userId 用户id
     * @param shareProfit 分润金额
     */
    @CacheEvict(key = "#userId")
    @org.springframework.transaction.annotation.Transactional
    public void addShareProfit(Long userId, BigDecimal shareProfit) {
        userMoneyRepository.addShareProfit(userId, shareProfit);
    }

    /**
     * 扣减分润金额
     *
     * @param userId 用户id
     * @param shareProfit 分润金额
     */
    @CacheEvict(key = "#userId")
    @org.springframework.transaction.annotation.Transactional
    public void subShareProfit(Long userId, BigDecimal shareProfit) {
        UserMoney userMoneyLock2 = findUserByUserIdUseLock(userId);
        // 扣减余额大于剩余余额
        if (shareProfit.compareTo(userMoneyLock2.getShareProfit()) == 1) {
            redisUtil.delete(RedisUtil.USERMONEY_KEY + userId);
            throw new UserMoneyChangeException("扣减分润金额超过本地剩余额度");
        }
        userMoneyRepository.subShareProfit(userId, shareProfit);
    }

    /**
     * 增加实时余额
     *
     * @param userId 用户id
     * @param balance 实时余额
     */
    // @CacheEvict(key = "#userId")
    // @Transactional
    // public void addBalance(Long userId, BigDecimal balance) {
    // RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
    // try {
    // userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
    // userMoneyRepository.addBalance(userId, balance);
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // } finally {
    // // 释放锁
    // RedisKeyUtil.unlock(userMoneyLock);
    // log.info("addBalance 用户增加balance释放锁", userId);
    // }
    // }
    @CacheEvict(key = "#userId")
    @org.springframework.transaction.annotation.Transactional
    public void addBalance(Long userId, BigDecimal balance) {
        userMoneyRepository.addBalance(userId, balance);
    }

    /**
     * 扣减实时余额
     *
     * @param userId 用户id
     * @param balance 实时余额
     */
    // @CacheEvict(key = "#userId")
    // @Transactional
    // public void subBalance(Long userId, BigDecimal balance) {
    // RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
    // try {
    // userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
    // UserMoney userMoneyLock2 = findUserByUserIdUseLock(userId);
    // //扣减余额大于剩余余额
    // if (balance.compareTo(userMoneyLock2.getBalance()) == 1) {
    // redisUtil.delete(RedisUtil.USERMONEY_KEY + userId);
    // throw new UserMoneyChangeException("扣减实时余额超过本地剩余额度");
    // }
    // userMoneyRepository.subBalance(userId, balance);
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // } finally {
    // // 释放锁
    // RedisKeyUtil.unlock(userMoneyLock);
    // log.info("subBalance 用户扣减banance释放锁", userId);
    // }
    // }
    @CacheEvict(key = "#userId")
    @org.springframework.transaction.annotation.Transactional
    public void subBalance(Long userId, BigDecimal balance, UserMoney user) {
        // 扣减余额大于剩余余额
        if (balance.compareTo(user.getBalance()) == 1) {
            redisUtil.delete(RedisUtil.USERMONEY_KEY + userId);
            throw new UserMoneyChangeException("扣减实时余额超过本地剩余额度");
        }
        userMoneyRepository.subBalance(userId, balance);
    }

    /**
     * 增加等级流水
     *
     * @param userId 用户id
     * @param levelWater 等级流水
     */
    //    @CacheEvict(key = "#userId")
    //    @Transactional
    //    public void addLevelWater(Long userId, BigDecimal levelWater) {
    //        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
    //        try {
    //            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
    //            userMoneyRepository.addLevelWater(userId, levelWater);
    //        } catch (Exception e) {
    //            throw new RuntimeException(e);
    //        } finally {
    //            // 释放锁
    //            RedisKeyUtil.unlock(userMoneyLock);
    //            log.info("addLevelWater 用户增加levelWater释放锁", userId);
    //        }
    //    }
    @CacheEvict(key = "#userId")
    @org.springframework.transaction.annotation.Transactional
    public void addLevelWater(Long userId, BigDecimal levelWater) {
        userMoneyRepository.addLevelWater(userId, levelWater);
    }
    /**
     * 扣减等级流水
     *
     * @param userId 用户id
     * @param levelWater 等级流水
     */
    //    @CacheEvict(key = "#userId")
    //    @Transactional
    //    public void subLevelWater(Long userId, BigDecimal levelWater) {
    //        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
    //        try {
    //            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
    //            UserMoney userMoneyLock2 = findUserByUserIdUseLock(userId);
    //            // 扣减余额大于剩余余额
    //            if (levelWater.compareTo(userMoneyLock2.getLevelWater()) == 1) {
    //                redisUtil.delete(RedisUtil.USERMONEY_KEY + userId);
    //                throw new UserMoneyChangeException("扣减洗码额超过本地剩余额度");
    //            }
    //            userMoneyRepository.subLevelWater(userId, levelWater);
    //
    //        } catch (Exception e) {
    //            throw new RuntimeException(e);
    //        } finally {
    //            // 释放锁
    //            RedisKeyUtil.unlock(userMoneyLock);
    //            log.info("subLevelWater 用户扣减subLevelWater释放锁", userId);
    //        }
    //    }
    @CacheEvict(key = "#userId")
    @org.springframework.transaction.annotation.Transactional
    public void subLevelWater(Long userId, BigDecimal levelWater,UserMoney userMoney) {
        // 扣减余额大于剩余余额
        if (levelWater.compareTo(userMoney.getLevelWater()) == 1) {
            redisUtil.delete(RedisUtil.USERMONEY_KEY + userId);
            throw new UserMoneyChangeException("扣减洗码额超过本地剩余额度");
        }
        userMoneyRepository.subLevelWater(userId, levelWater);
    }
    // @CacheEvict(key = "#userId")
    // @Transactional
    // public void modifyLevelWater(Long userId, BigDecimal balance) {
    // RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
    // try {
    // userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
    // userMoneyRepository.modifyLevelWater(userId, balance);
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // } finally {
    // // 释放锁
    // RedisKeyUtil.unlock(userMoneyLock);
    // log.info("modifyLevelWater 用户修改等级流水释放锁", userId);
    // }
    // }
    @CacheEvict(key = "#userId")
    @org.springframework.transaction.annotation.Transactional
    public void modifyLevelWater(Long userId, BigDecimal balance) {
        userMoneyRepository.modifyLevelWater(userId, balance);
    }

    /**
     * 增加等级流水
     *
     * @param userId 用户id
     * @param riseWater 等级流水
     */
    //    @CacheEvict(key = "#userId")
    //    @Transactional
    //    public void addRiseWater(Long userId, BigDecimal riseWater) {
    //        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
    //        try {
    //            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
    //            userMoneyRepository.addRiseWater(userId, riseWater);
    //
    //        } catch (Exception e) {
    //            throw new RuntimeException(e);
    //        } finally {
    //            // 释放锁
    //            RedisKeyUtil.unlock(userMoneyLock);
    //            log.info("addRiseWater 用户增加levelWater释放锁", userId);
    //        }
    //    }
    @CacheEvict(key = "#userId")
    @org.springframework.transaction.annotation.Transactional
    public void addRiseWater(Long userId, BigDecimal riseWater) {
        userMoneyRepository.addRiseWater(userId, riseWater);
    }
    /**
     * 扣减等级流水
     *
     * @param userId 用户id
     * @param riseWater 等级流水
     */
    //    @CacheEvict(key = "#userId")
    //    @Transactional
    //    public void subRiseWater(Long userId, BigDecimal riseWater) {
    //        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
    //        try {
    //            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
    //            UserMoney userMoneyLock2 = findUserByUserIdUseLock(userId);
    //            // 扣减余额大于剩余余额
    //            if (riseWater.compareTo(userMoneyLock2.getRiseWater()) == 1) {
    //                redisUtil.delete(RedisUtil.USERMONEY_KEY + userId);
    //                throw new UserMoneyChangeException("扣减洗码额超过本地剩余额度");
    //            }
    //            userMoneyRepository.subRiseWater(userId, riseWater);
    //        } catch (Exception e) {
    //            throw new RuntimeException(e);
    //        } finally {
    //            // 释放锁
    //            RedisKeyUtil.unlock(userMoneyLock);
    //            log.info("subRiseWater 用户扣减subLevelWater释放锁", userId);
    //        }
    //    }
    @CacheEvict(key = "#userId")
    @org.springframework.transaction.annotation.Transactional
    public void subRiseWater(Long userId, BigDecimal riseWater,UserMoney userMoneyLock2) {
        // 扣减余额大于剩余余额
        if (riseWater.compareTo(userMoneyLock2.getRiseWater()) == 1) {
            redisUtil.delete(RedisUtil.USERMONEY_KEY + userId);
            throw new UserMoneyChangeException("扣减洗码额超过本地剩余额度");
        }
        userMoneyRepository.subRiseWater(userId, riseWater);
    }

    //    @CacheEvict(key = "#userId")
    //    @Transactional
    //    public void modifyRiseWater(Long userId, BigDecimal riseWater) {
    //        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
    //        try {
    //            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
    //            userMoneyRepository.modifyRiseWater(userId, riseWater);
    //        } catch (Exception e) {
    //            throw new RuntimeException(e);
    //        } finally {
    //            // 释放锁
    //            RedisKeyUtil.unlock(userMoneyLock);
    //            log.info("modifyLevelWater 用户修改等级流水释放锁", userId);
    //        }
    //    }
    @CacheEvict(key = "#userId")
    @org.springframework.transaction.annotation.Transactional
    public void modifyRiseWater(Long userId, BigDecimal riseWater) {
        userMoneyRepository.modifyRiseWater(userId, riseWater);
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
            BigDecimal levelWater = userMoney.getLevelWater() == null ? defaultVal : userMoney.getLevelWater();
            userMoney.setLevelWater(levelWater);
            BigDecimal riseWater = userMoney.getRiseWater() == null ? defaultVal : userMoney.getRiseWater();
            userMoney.setRiseWater(riseWater);
        }
        return userMoney;
    }

    @CachePut(key = "#userMoney.userId")
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
            param = MessageFormat.format(param, third.getAccount(), lang.toString());
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null ? "" : first.getWebConfiguration();
            WMurl = WMurl + refreshUrl;
            String s = HttpClient4Util.get(WMurl + param);
            log.info("{}查询web接口返回{}", user.getAccount(), s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
            //            Object data = parse.get("data");
            //            return new BigDecimal(data.toString());
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject getWMonetUser(UserThird third) {
        User byId = userService.findById(third.getUserId());
        Integer lang;
        if (byId == null) {
            lang = 0;
        } else {
            lang = byId.getLanguage();
        }
        if (lang == null) {
            lang = 0;
        }
        try {
            String param = "account={0}&lang={1}";
            param = MessageFormat.format(param, third.getAccount(), lang.toString());
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null ? "" : first.getWebConfiguration();
            WMurl = WMurl + refreshUrl;
            String s = HttpClient4Util.get(WMurl + param);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("出现异常:{}", e.getMessage());
            return null;
        }
    }

    public JSONObject getSABAonetUser(UserThird third) {

        try {
            String param = "userId={0}&vendorCode={1}";
            param = MessageFormat.format(param, third.getUserId(), Constants.PLATFORM_SABASPORT);
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null ? "" : first.getWebConfiguration();
            WMurl = WMurl + PG_refreshUrl;
            String s = HttpClient4Util.get(WMurl + param);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }


    public JSONObject oneKeyRecover(User user) {
        try {
            String param = "userId={0}";
            param = MessageFormat.format(param, user.getId().toString());
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null ? "" : first.getWebConfiguration();
            WMurl = WMurl + recycleUrl;
            String s = HttpClient4Util.getWeb(WMurl + param);
            log.info("{}回收余额web接口返回{}", user.getAccount(), s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 查询欧博余额
     *
     * @param userId
     * @return
     */
    public JSONObject refreshOB(Long userId) {
        try {
            String param = "userId={0}";
            param = MessageFormat.format(param, userId.toString());
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null ? "" : first.getWebConfiguration();
            WMurl = WMurl + OB_refreshUrl;
            String s = HttpClient4Util.getWeb(WMurl + param);
            log.info("{}查询OB余额web接口返回{}", userId, s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject refreshPGAndCQ9(Long userId) {
        try {
            String param = "userId={0}";
            param = MessageFormat.format(param, userId.toString());
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null ? "" : first.getWebConfiguration();
            WMurl = WMurl + PG_refreshUrl;
            String s = HttpClient4Util.get(WMurl + param);
            log.info("{}查询PG余额web接口返回{}", userId, s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject refreshSABA(Long userId) {
        try {
            String param = "userId={0}&vendorCode={1}";
            param = MessageFormat.format(param, userId.toString(), Constants.PLATFORM_SABASPORT);
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null ? "" : first.getWebConfiguration();
            WMurl = WMurl + PG_refreshUrl;
            log.info("沙巴查询余额路径：【{}】", WMurl + param);
            String s = HttpClient4Util.get(WMurl + param);
            log.info("{}查询沙巴余额web接口返回{}", userId, s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject oneKeySABAApi(User user) {
        try {
            String param = "userId={0}&vendorCode={1}";
            param = MessageFormat.format(param, user.getId().toString(), Constants.PLATFORM_SABASPORT);
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null ? "" : first.getWebConfiguration();
            WMurl = WMurl + PG_recycleUrl;
            String s = HttpClient4Util.getWeb(WMurl + param);
            log.info("{}回收PG余额web接口返回{}", user.getAccount(), s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject refreshPGAndCQ9UserId(String userId) {
        try {
            String param = "userId={0}";
            param = MessageFormat.format(param, userId);
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null ? "" : first.getWebConfiguration();
            WMurl = WMurl + PG_refreshUrl;
            String s = HttpClient4Util.get(WMurl + param);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject refreshSABAUserId(String userId) {
        try {
            String param = "userId={0}&vendorCode={1}";
            param = MessageFormat.format(param, userId, Constants.PLATFORM_SABASPORT);
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null ? "" : first.getWebConfiguration();
            WMurl = WMurl + PG_refreshUrl;
            String s = HttpClient4Util.getWeb(WMurl + param);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }


    public JSONObject oneKeyRecoverApi(User user) {
        try {
            String param = "userId={0}";
            param = MessageFormat.format(param, user.getId().toString());
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null ? "" : first.getWebConfiguration();
            WMurl = WMurl + PG_recycleUrl;
            String s = HttpClient4Util.getWeb(WMurl + param);
            log.info("{}回收PG余额web接口返回{}", user.getAccount(), s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject oneKeyOBRecoverApi(User user) {
        try {
            String param = "userId={0}";
            param = MessageFormat.format(param, user.getId().toString());
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null ? "" : first.getWebConfiguration();
            WMurl = WMurl + OB_recycleUrl;
            String s = HttpClient4Util.getWeb(WMurl + param);
            log.info("{}回收OB电竞余额web接口返回{}", user.getAccount(), s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject refreshOBTY(Long userId) {
        try {
            String param = "userId={0}";
            param = MessageFormat.format(param, userId.toString());
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null ? "" : first.getWebConfiguration();
            WMurl = WMurl + OBTY_refreshUrl;
            String s = HttpClient4Util.get(WMurl + param);
            log.info("{}查询OB体育web接口返回{}", userId, s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject oneKeyOBTYRecoverApi(User user) {
        try {
            String param = "userId={0}";
            param = MessageFormat.format(param, user.getId().toString());
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null ? "" : first.getWebConfiguration();
            WMurl = WMurl + OBTY_recycleUrl;
            String s = HttpClient4Util.getWeb(WMurl + param);
            log.info("{}回收OB体育余额web接口返回{}", user.getAccount(), s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    public UserMoney findUserMoneySum(UserMoney userMoney) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserMoney> query = builder.createQuery(UserMoney.class);
        Root<UserMoney> root = query.from(UserMoney.class);

        query.multiselect(builder.sum(root.get("money").as(BigDecimal.class)).alias("money"), builder.sum(root.get("washCode").as(BigDecimal.class)).alias("washCode"));

        List<Predicate> predicates = new ArrayList();

        query.where(predicates.toArray(new Predicate[predicates.size()]));
        UserMoney singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }

    public UserMoney findsumUserIntegral(UserMoney userMoney) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserMoney> query = builder.createQuery(UserMoney.class);
        Root<UserMoney> root = query.from(UserMoney.class);

        query.multiselect(builder.sum(root.get("integral").as(BigDecimal.class)).alias("integral"));

        List<Predicate> predicates = new ArrayList();
        if (userMoney.getUserId() != null) {
            predicates.add(builder.equal(root.get("userId").as(Long.class), userMoney.getUserId()));
        }
        query.where(predicates.toArray(new Predicate[predicates.size()]));
        UserMoney singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }

    public JSONObject refreshAE(Long userId) {
        try {
            String param = "";
            if(userId != null && userId != 0){
                param = MessageFormat.format("userId={0}",userId.toString());
            }

            PlatformConfig first = platformConfigService.findFirst();

            if(first == null){
                return null;
            }
            String aEUrl = first.getWebConfiguration() + AE_refreshUrl;
            if(!CommonUtil.checkNull(param)){
                aEUrl = aEUrl +  param;
            }
            String s = HttpClient4Util.getWeb(aEUrl);
            log.info("查询AE余额web接口返回{}",s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject oneKeyAERecoverApi(User user) {
        try {
            String param = "userId={0}";
            param = MessageFormat.format(param, user.getId().toString());
            PlatformConfig first = platformConfigService.findFirst();
            String aeUrl = first == null ? "" : first.getWebConfiguration();
            aeUrl = aeUrl + AE_recycleUrl;
            String s = HttpClient4Util.getWeb(aeUrl + param);
            log.info("{}回收AE余额web接口返回{}", user.getAccount(), s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject refreshVNC(Long userId) {
        try {
            String param = "";
            if (Objects.nonNull(userId) && userId.longValue() != 0L) {
                param = MessageFormat.format("userId={0}", userId.toString());
            } else {
                param = "userId=";
            }
            PlatformConfig first = platformConfigService.findFirst();
            if (first == null) {
                return null;
            }
            String aEUrl = first.getWebConfiguration() + VNC_refreshUrl;
            if (!CommonUtil.checkNull(param)) {
                aEUrl = aEUrl + param;
            }
            String s = HttpClient4Util.getWeb(aEUrl);
            log.info("查询VNC余额web接口返回{}", s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject oneKeyVNCRecoverApi(Long userId) {
        try {
            String param = "userId={0}";
            param = MessageFormat.format(param, userId.toString());
            PlatformConfig first = platformConfigService.findFirst();
            String aeUrl = first == null ? "" : first.getWebConfiguration();
            aeUrl = aeUrl + VNC_recycleUrl;
            String s = HttpClient4Util.getWeb(aeUrl + param);
            log.info("{}回收VNC余额web接口返回{}", userId, s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }
}
