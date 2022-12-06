package com.qianyi.casinocore.util;

import com.qianyi.casinocore.model.SysPermission;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RedisKeyUtil {

    @Autowired
    private RedissonClient redissonClient;

    //系统默认锁时间
    public static final int LOCK_TIME = 120;

    public static final String PROXY_CREDIT_LOCK = "PROXY-CREDIT-LOCK-";

    public static final String PROXY_WASH_CODE_REBATE_LOCK = "PROXY-WASH-CODE-REBATE-LOCK-";

    public static final String USER_MONEY_LOCK = "USER-MONEY-LOCK-";

    public static final String ADMIN_USER_SYSPERMISSION = "ADMIN-USER-SYSPERMISSION-";
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

}
