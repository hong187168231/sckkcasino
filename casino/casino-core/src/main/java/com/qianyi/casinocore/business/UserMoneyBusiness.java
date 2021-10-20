package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.ShareProfitMqVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 用户钱包操作
 */
@Slf4j
@Service
public class UserMoneyBusiness {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private WashCodeChangeService washCodeChangeService;
    @Autowired
    private CodeNumChangeService codeNumChangeService;
    @Autowired
    private GameRecordService gameRecordService;
    @Autowired
    private UserWashCodeConfigService userWashCodeConfigService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    //默认最小清零打码量
    private static final BigDecimal DEFAULT_CLEAR = new BigDecimal("10");

    /***
     *
     * @param platformConfig
     * @param record
     * @return
     */
    @Async("asyncExecutor")
    @Transactional
    public ResponseEntity subCodeNum(PlatformConfig platformConfig, GameRecord record) {
        BigDecimal validbet = new BigDecimal(record.getValidbet());
        Long userId = record.getUserId();
        log.info("开始打码={}", record.toString());
        if (validbet == null || userId == null) {
            return ResponseUtil.fail();
        }
        if (BigDecimal.ZERO.compareTo(validbet) == 0) {
            record.setCodeNumStatus(Constants.yes);
            gameRecordService.save(record);
            return ResponseUtil.success();
        }
        UserMoney user = userMoneyService.findUserByUserIdUseLock(userId);
        if (user == null || user.getCodeNum() == null) {
            return ResponseUtil.fail();
        }
        BigDecimal codeNum = user.getCodeNum();
        //剩余打码量小于等于0时
        if (codeNum.compareTo(BigDecimal.ZERO) < 1) {
            return ResponseUtil.fail();
        }
        Long gameRecordId = record.getId();
        //最小清零打码量
        ResponseEntity responseEntity = checkClearCodeNum(platformConfig, userId, gameRecordId, user);
        if (responseEntity.getCode() == 0) {
            record.setCodeNumStatus(Constants.yes);
            gameRecordService.save(record);
            return ResponseUtil.success();
        }
        //有效投注额大于等于等于剩余打码量
        if (validbet.compareTo(codeNum) > -1) {
            userMoneyService.subCodeNum(userId, codeNum);
            BigDecimal codeNumAfter = user.getCodeNum().subtract(codeNum);
            codeNumChangeService.save(userId, gameRecordId, codeNum.negate(), user.getCodeNum(), codeNumAfter);
            user.setCodeNum(codeNumAfter);
        } else {
            //有效投注额小于剩余打码量
            userMoneyService.subCodeNum(userId, validbet);
            BigDecimal codeNumAfter = user.getCodeNum().subtract(validbet);
            codeNumChangeService.save(userId, gameRecordId, validbet.negate(), user.getCodeNum(), codeNumAfter);
            user.setCodeNum(codeNumAfter);
        }
        //扣减完毕后再次检查最小清零打码量
        checkClearCodeNum(platformConfig, userId, gameRecordId, user);
        record.setCodeNumStatus(Constants.yes);
        gameRecordService.save(record);
        return ResponseUtil.success();
    }

    /**
     * 最小清0打码量检查
     *
     * @param platformConfig
     * @param userId
     * @param gameRecordId
     * @param user
     * @return
     */
    @Transactional
    public ResponseEntity checkClearCodeNum(PlatformConfig platformConfig, Long userId, Long gameRecordId, UserMoney user) {
        BigDecimal codeNum = user.getCodeNum();
        if (platformConfig != null && platformConfig.getClearCodeNum() != null) {
            BigDecimal minCodeNumVal = platformConfig.getClearCodeNum();
            //剩余打码量小于等于最小清零打码量时 直接清0
            if (codeNum.compareTo(minCodeNumVal) < 1) {
                userMoneyService.subCodeNum(userId, codeNum);
                codeNumChangeService.save(userId, gameRecordId, codeNum.negate(), user.getCodeNum(), user.getCodeNum().subtract(codeNum));
                return ResponseUtil.success();
            }
        } else {
            if (codeNum.compareTo(DEFAULT_CLEAR) < 1) {
                userMoneyService.subCodeNum(userId, codeNum);
                codeNumChangeService.save(userId, gameRecordId, codeNum.negate(), user.getCodeNum(), user.getCodeNum().subtract(codeNum));
                return ResponseUtil.success();
            }
        }
        return ResponseUtil.fail();
    }

    @Async("asyncExecutor")
    @Transactional
    public void washCode(String platform, GameRecord gameRecord) {
        BigDecimal validbet = new BigDecimal(gameRecord.getValidbet());
        Long userId = gameRecord.getUserId();
        log.info("开始洗码={}", gameRecord.toString());
        WashCodeConfig config = userWashCodeConfigService.getWashCodeConfigByUserIdAndGameId(platform, userId, gameRecord.getGid().toString());
        log.info("开始洗码游戏配置={}", config.toString());
        if (config == null || config.getRate() == null || validbet == null || BigDecimal.ZERO.compareTo(config.getRate()) == 0 || BigDecimal.ZERO.compareTo(validbet) == 0) {
            gameRecord.setWashCodeStatus(Constants.yes);
            gameRecordService.save(gameRecord);
            return;
        }
        //数据库存的10是代表百分之10
        BigDecimal rate = config.getRate().divide(new BigDecimal(100));//转换百分比
        BigDecimal washCodeVal = validbet.multiply(rate);
        WashCodeChange washCodeChange = new WashCodeChange();
        washCodeChange.setUserId(userId);
        washCodeChange.setAmount(washCodeVal);
        washCodeChange.setPlatform(platform);
        washCodeChange.setGameId(gameRecord.getGid().toString());
        washCodeChange.setGameName(gameRecord.getGname());
        washCodeChange.setRate(rate);
        washCodeChange.setValidbet(validbet);
        washCodeChange.setGameRecordId(gameRecord.getId());
        washCodeChangeService.save(washCodeChange);
        userMoneyService.findUserByUserIdUseLock(userId);
        userMoneyService.addWashCode(userId, washCodeVal);
        gameRecord.setWashCodeStatus(Constants.yes);
        gameRecordService.save(gameRecord);
    }

    /**
     * 三级分润
     *
     * @param record
     */
    @Async("asyncExecutor")
    @Transactional
    public void shareProfit(GameRecord record) {
        log.info("开始三级分润={}", record.toString());
        BigDecimal validbet = new BigDecimal(record.getValidbet());
        Long userId = record.getUserId();
        ShareProfitMqVo shareProfitMqVo=new ShareProfitMqVo();
        shareProfitMqVo.setUserId(userId);
        shareProfitMqVo.setValidbet(validbet);
        shareProfitMqVo.setGameRecordId(record.getId());
        shareProfitMqVo.setBetTime(record.getBetTime());
        rabbitTemplate.convertAndSend(RabbitMqConstants.SHAREPROFIT_DIRECTQUEUE_DIRECTEXCHANGE, RabbitMqConstants.SHAREPROFIT_DIRECT, shareProfitMqVo, new CorrelationData(UUID.randomUUID().toString()));
        log.info("分润消息发送成功={}", shareProfitMqVo);
    }
}
