package com.qianyi.casinoweb.job;

import com.qianyi.casinocore.business.ExtractPointsConfigBusiness;
import com.qianyi.casinocore.business.TelegramBotBusiness;
import com.qianyi.casinocore.business.UserMoneyBusiness;
import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.GameRecordGoldenF;
import com.qianyi.casinocore.model.PlatformConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 开启异步洗码，打码，分润，单独写出来是为了解决异步事务失效问题
 */
@Service
@Slf4j
public class GameRecordPgAsyncOper {

    @Autowired
    private UserMoneyBusiness userMoneyBusiness;

    @Autowired
    private TelegramBotBusiness telegramBotBusiness;

    @Autowired
    private ExtractPointsConfigBusiness extractPointsConfigBusiness;

    /**
     * 异步洗码
     *
     * @param platform
     * @param gameRecord
     */
    //    @Async("asyncExecutor")
    public void washCode(String platform, GameRecord gameRecord) {
        try {
            userMoneyBusiness.washCode(platform, gameRecord);
        } catch (Exception e) {
            log.error("异步洗码出现异常id{}platform{}userId{} {}", gameRecord.getId(), platform, gameRecord.getUserId(),
                e.getMessage());
        }
    }

    /**
     * 异步抽点
     *
     * @param platform
     * @param gameRecord
     */
    //    @Async("asyncExecutor")
    public void extractPoints(String platform, GameRecord gameRecord) {
        extractPointsConfigBusiness.extractPoints(platform, gameRecord);
    }

    /**
     * 异步打码
     *
     * @param platformConfig
     * @param record
     */
    //    @Async("asyncExecutor")
    public void subCodeNum(String platform, PlatformConfig platformConfig, GameRecord record) {
        try {
            userMoneyBusiness.subCodeNum(platform, platformConfig, record);
        } catch (Exception e) {
            log.error("异步打码出现异常id{}platform{}userId{} {}", record.getId(), platform, record.getUserId(),
                e.getMessage());
        }
    }

    /**
     * 异步分润
     *
     * @param record
     */
    //    @Async("asyncExecutor")
    public void shareProfit(String platform, GameRecord record) {
        userMoneyBusiness.shareProfit(platform, record);
    }

    /**
     * 异步返利
     *
     * @param platform
     * @param record
     */
    //    @Async("asyncExecutor")
    public void rebate(String platform, GameRecord record) {
        try {
            userMoneyBusiness.rebate(platform, record);
        } catch (Exception e) {
            log.error("异步返利出现异常id{}platform{}userId{} {}", record.getId(), platform, record.getUserId(),
                e.getMessage());
        }
    }

    /**
     * 异步推送MQ消息
     *
     * @param platform
     * @param record
     */
    //    @Async("asyncExecutor")
    public void proxyGameRecordReport(String platform, GameRecord record) {
        userMoneyBusiness.proxyGameRecordReport(platform, record);
    }

    /**
     * 异步更新等级流水
     *
     * @param platform
     * @param record
     */
    //    @Async("asyncExecutor")
    public void levelWater(String platform, GameRecord record) {
        try {
            userMoneyBusiness.changeLevelWater(platform, record);
        } catch (Exception e) {
            log.error("异步更新等级流水出现异常id{}platform{}userId{} {}", record.getId(), platform, record.getUserId(),
                e.getMessage());
        }
    }

    /**
     * 更新用户balance
     *
     * @param userId
     * @param betAmount
     * @param winAmount
     */
    //    @Async("asyncExecutor")
    public void changeUserBalance(Long userId, BigDecimal betAmount, BigDecimal winAmount) {
        try {
            userMoneyBusiness.changeUserBalance(userId, betAmount, winAmount);
        } catch (Exception e) {
            log.error("更新用户balance出现异常userId{} {}", userId, e.getMessage());
        }
    }

    /**
     * 改变用户实时余额
     */
    //@Async("asyncExecutor")
    public void changeUserBalancePg(GameRecordGoldenF gameRecordGoldenF) {
        Long userId = gameRecordGoldenF.getUserId();
        try {
            BigDecimal betAmount = gameRecordGoldenF.getBetAmount();
            BigDecimal winAmount = gameRecordGoldenF.getWinAmount();
            if (betAmount == null || winAmount == null) {
                return;
            }
            BigDecimal winLossAmount = winAmount.subtract(betAmount);
            // 下注金额大于0，扣减
            if (betAmount.compareTo(BigDecimal.ZERO) == 1) {
                userMoneyBusiness.subBalance(userId, betAmount);
            }
            // 派彩金额大于0，增加
            if (winLossAmount.compareTo(BigDecimal.ZERO) == 1) {
                userMoneyBusiness.addBalance(userId, winLossAmount);
            }
        } catch (Exception e) {
            log.error("改变用户实时余额时报错，msg={}", e.getMessage());
        }
    }

}