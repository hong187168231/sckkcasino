package com.qianyi.casinocore.util;

import org.redisson.api.RAtomicDouble;
import org.redisson.api.RAtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;

@Component
public class ExpirationTimeUtil {

    /**
     * 一天时间的毫秒值
     */
    private final static long oneDay = 24 * 60 * 60 * 1000;

    /**
     * 普通游戏三天未操作取缓存
     */
    public final static long universality = oneDay * 3;
    /**
     * 体育游戏十四天未操作取缓存
     */
    public final static long sports = oneDay * 14;

    public static ExpirationTimeUtil expirationTimeUtil;

    @Autowired
    private RedisKeyUtil redisKeyUtil;

    @PostConstruct // 初始化
    public void init() {
        expirationTimeUtil = this;
        expirationTimeUtil.redisKeyUtil = this.redisKeyUtil;
    }

    public static Boolean check(String platform, String userId, long time, long expirationTime) {
        RAtomicLong lastLoginInt = expirationTimeUtil.redisKeyUtil.getLastLoginInt(platform, userId);
        if (lastLoginInt.get() == CommonConst.LONG_0) {
            lastLoginInt.set(time);
            return false;
        }
        if ((time - lastLoginInt.get()) >= expirationTime) {
            return true;
        }
        return false;
    }

    public static BigDecimal getTripartiteBalance(String platform, String userId) {
        RAtomicDouble atomicDouble = expirationTimeUtil.redisKeyUtil.getTripartiteBalance(platform, userId);
        return new BigDecimal(atomicDouble.get());
    }

    public static void resetExpirationTime(String platform, String userId) {
        expirationTimeUtil.redisKeyUtil.getLastLoginInt(platform, userId).set(System.currentTimeMillis());
    }

    public static void resetTripartiteBalance(String platform, String userId,BigDecimal Balance) {
        expirationTimeUtil.redisKeyUtil.getTripartiteBalance(platform, userId).set(Balance.doubleValue());
    }
}
