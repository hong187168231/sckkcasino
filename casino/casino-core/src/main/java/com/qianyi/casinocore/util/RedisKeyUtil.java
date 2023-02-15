package com.qianyi.casinocore.util;

import com.qianyi.casinocore.model.SysPermission;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.MessageFormat;

@Service
public class RedisKeyUtil {

    @Autowired
    private RedissonClient redissonClient;

    //系统默认锁时间
    public static final int LOCK_TIME = 120;

    //游戏报表默认锁时间
    public static final int GAME_RECORD_LOCK_TIME = 30;

    public static final String PROXY_CREDIT_LOCK = "PROXY-CREDIT-LOCK::LOCK-";

    public static final String PROXY_WASH_CODE_REBATE_LOCK = "PROXY-WASH-CODE-REBATE-LOCK::LOCK-";

    public static final String USER_MONEY_LOCK = "USER-MONEY-LOCK::LOCK-";

    public static final String BANK_CARDS_LOCK = "BANK-CARDS-LOCK::LOCK-";

    public static final String ADMIN_USER_SYSPERMISSION = "ADMIN-USER::SYSPERMISSION-";

    public static final String TRIPARTITE_BALANCE = "TRIPARTITE-BALANCE::{0}::USERID-{1}";

    public static final String LAST_LOGIN_INT = "LAST-LOGIN-INT::{0}::USERID-{1}";

    public static final String GAME_RECORD_REPORT_BUSINESS ="GAME-RECORD-REPORT-BUSINESS-LOCK::{0}::LOCK-{1}";
    /**
     * 全局代理信誉分锁
     *
     * @param proxyUserId 代理唯一id
     * @return 信誉分锁
     */
    public RLock getCreditLock(String proxyUserId) {
        return redissonClient.getFairLock(RedisKeyUtil.PROXY_CREDIT_LOCK + proxyUserId);
    }

    /**
     * 全局代理洗码返利
     *
     * @param proxyUserId 代理唯一id
     * @return 洗码返利锁
     */
    public RLock getWashCodeRebateLock(String proxyUserId) {
        return redissonClient.getFairLock(RedisKeyUtil.PROXY_WASH_CODE_REBATE_LOCK + proxyUserId);
    }

    /**
     * 全局会员钱包锁
     *
     * @param userId 会员id
     * @return 全局会员钱包锁
     */
    public RLock getUserMoneyLock(String userId) {
        return redissonClient.getFairLock(RedisKeyUtil.USER_MONEY_LOCK + userId);
    }

    /**
     * 全局会员银行卡锁
     *
     * @param userId 会员id
     * @return 全局会员银行卡锁
     */
    public RLock getBankcardsLock(String userId) {
        return redissonClient.getFairLock(RedisKeyUtil.BANK_CARDS_LOCK + userId);
    }
    /**
     * 释放redis锁
     *
     * @param rLock redis锁
     */
    public static void unlock(RLock rLock) {
        if (rLock != null && rLock.isLocked()) {
            rLock.unlock();
        }
    }

    /**
     * 角色权限信息
     *
     * @param id 角色id
     */
    public RList<SysPermission> getSysPermissionList(String id) {
        return redissonClient.getList(RedisKeyUtil.ADMIN_USER_SYSPERMISSION + id);
    }

    /**
     * 三方平台余额缓存
     *
     * @param userId 会员id
     * @param platform 平台名称
     */
    public RAtomicDouble getTripartiteBalance(String platform,String userId) {
        RAtomicDouble atomicDouble = redissonClient.getAtomicDouble(MessageFormat.format(RedisKeyUtil.TRIPARTITE_BALANCE, platform,userId));
        return atomicDouble;
//        return new BigDecimal(atomicDouble.get());
    }

    /**
     * 三方平台最后登录时间
     *
     * @param userId 会员id
     * @param platform 平台名称
     */
    public RAtomicLong getLastLoginInt(String platform,String userId) {
        return redissonClient.getAtomicLong(MessageFormat.format(RedisKeyUtil.LAST_LOGIN_INT, platform,userId));
    }

    /**
     * 游戏报表锁
     *
     * @param gameId 游戏注单id
     * @param platform 平台编码
     * @return 全局会员钱包锁
     */
    public RLock getGameRecordLock(String platform,String gameId) {
        return redissonClient.getFairLock(MessageFormat.format(RedisKeyUtil.GAME_RECORD_REPORT_BUSINESS, platform,gameId));
    }
}
