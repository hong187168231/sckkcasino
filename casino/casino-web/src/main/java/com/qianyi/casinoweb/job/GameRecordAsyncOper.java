package com.qianyi.casinoweb.job;

import com.qianyi.casinocore.business.ExtractPointsConfigBusiness;
import com.qianyi.casinocore.business.TelegramBotBusiness;
import com.qianyi.casinocore.business.UserMoneyBusiness;
import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * 开启异步洗码，打码，分润，单独写出来是为了解决异步事务失效问题
 */
@Service
@Slf4j
public class GameRecordAsyncOper {

    @Autowired
    private UserMoneyBusiness userMoneyBusiness;

    @Autowired
    private TelegramBotBusiness telegramBotBusiness;

    @Autowired
    private ExtractPointsConfigBusiness extractPointsConfigBusiness;

    @Autowired
    private RedisKeyUtil redisKeyUtil;

    /**
     * 异步洗码
     *
     * @param platform
     * @param gameRecord
     */
    @Async("asyncExecutor")
    public void washCode(String platform, GameRecord gameRecord) {
        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(gameRecord.getUserId().toString());
        try {
            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
            userMoneyBusiness.washCode(platform, gameRecord);
        } catch (Exception e) {
            log.error("异步洗码出现异常id{}platform{}userId{} {}",gameRecord.getId(),platform,gameRecord.getUserId(),e.getMessage());
        } finally {
            // 释放锁
            RedisKeyUtil.unlock(userMoneyLock);
            log.info("washCode 用户增加washCode释放锁", gameRecord.getUserId());
        }
    }

    /**
     * 异步抽点
     * @param platform
     * @param gameRecord
     */
    @Async("asyncExecutor")
    public void extractPoints(String platform, GameRecord gameRecord) {
        extractPointsConfigBusiness.extractPoints(platform, gameRecord);

        //        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(gameRecord.getUserId().toString());
        //        try {
        //            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
        //            extractPointsConfigBusiness.extractPoints(platform, gameRecord);
        //        } catch (Exception e) {
        //            log.error("异步抽点出现异常id{}platform{}userId{} {}",gameRecord.getId(),platform,gameRecord.getUserId(),e.getMessage());
        //        } finally {
        //            // 释放锁
        //            RedisKeyUtil.unlock(userMoneyLock);
        //            log.info("extractPoints 用户extractPoints释放锁", gameRecord.getUserId());
        //        }
    }

    /**
     * 异步打码
     *
     * @param platformConfig
     * @param record
     */
    @Async("asyncExecutor")
    public void subCodeNum(String platform, PlatformConfig platformConfig, GameRecord record) {
        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(record.getUserId().toString());
        try {
            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
            userMoneyBusiness.subCodeNum(platform, platformConfig, record);
        } catch (Exception e) {
            log.error("异步打码出现异常id{}platform{}userId{} {}",record.getId(),platform,record.getUserId(),e.getMessage());
        } finally {
            // 释放锁
            RedisKeyUtil.unlock(userMoneyLock);
            log.info("subCodeNum 用户subCodeNum释放锁", record.getUserId());
        }
    }

    /**
     * 异步分润
     *
     * @param record
     */
    @Async("asyncExecutor")
    public void shareProfit(String platform, GameRecord record) {
        userMoneyBusiness.shareProfit(platform, record);
    }

    /**
     * 异步返利
     * @param platform
     * @param record
     */
    @Async("asyncExecutor")
    public void rebate(String platform, GameRecord record) {
        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(record.getUserId().toString());
        try {
            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
            userMoneyBusiness.rebate(platform, record);
        } catch (Exception e) {
            log.error("异步返利出现异常id{}platform{}userId{} {}",record.getId(),platform,record.getUserId(),e.getMessage());
        } finally {
            // 释放锁
            RedisKeyUtil.unlock(userMoneyLock);
            log.info("rebate 用户rebate释放锁", record.getUserId());
        }
    }

    /**
     * 异步推送MQ消息
     * @param platform
     * @param record
     */
    @Async("asyncExecutor")
    public void proxyGameRecordReport(String platform, GameRecord record) {
        userMoneyBusiness.proxyGameRecordReport(platform, record);
    }

    /**
     * 更新用户balance
     * @param userId
     * @param betAmount
     * @param winAmount
     */
    @Async("asyncExecutor")
    public void changeUserBalance(Long userId, BigDecimal betAmount,BigDecimal winAmount) {
        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
        try {
            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
            userMoneyBusiness.changeUserBalance(userId, betAmount,winAmount);
        } catch (Exception e) {
            log.error("更新用户balance出现异常userId{} {}",userId,e.getMessage());
        } finally {
            // 释放锁
            RedisKeyUtil.unlock(userMoneyLock);
        }
    }

    /**
     * 异步更新等级流水
     * @param platform
     * @param record
     */
    @Async("asyncExecutor")
    public void levelWater(String platform, GameRecord record) {
        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(record.getUserId().toString());
        try {
            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
            userMoneyBusiness.changeLevelWater(platform, record);
        } catch (Exception e) {
            log.error("异步更新等级流水出现异常id{}platform{}userId{} {}",record.getId(),platform,record.getUserId(),e.getMessage());
        } finally {
            // 释放锁
            RedisKeyUtil.unlock(userMoneyLock);
            log.info("levelWater 用户levelWater释放锁", record.getUserId());
        }
    }

    /**
     * 异步给TG机器人发送消息
     *
     * @param msg
     */
    @Async("asyncExecutor")
    public void sendMsgToTelegramBot(String msg) {
        telegramBotBusiness.sendMsgToTelegramBot(msg);
    }
}
