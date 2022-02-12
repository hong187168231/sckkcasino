package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.ShareProfitMqVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    //默认最小清零打码量
    private static final BigDecimal DEFAULT_CLEAR = new BigDecimal("10");

    /***
     *
     * @param platformConfig
     * @param record
     * @return
     */
    @Transactional
    public void subCodeNum(String platform, PlatformConfig platformConfig, GameRecord record) {
        log.info("开始打码,平台={}，注单ID={},注单明细={}",platform,record.getBetId(), record.toString());
        BigDecimal validbet = new BigDecimal(record.getValidbet());
        Long userId = record.getUserId();
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        if (userMoney == null || userMoney.getCodeNum() == null) {
            log.error("打码时，userMoney或者userMoney.getCodeNum()为null,userId={}", userId);
            return;
        }
        BigDecimal codeNum = userMoney.getCodeNum();
        //剩余打码量大于0
        if (codeNum.compareTo(BigDecimal.ZERO) == 1) {
            //有效投注额大于等于等于剩余打码量，最多只扣减剩余的
            validbet = validbet.compareTo(codeNum) > -1 ? codeNum : validbet;
            userMoneyService.subCodeNum(userId, validbet);
            BigDecimal codeNumAfter = userMoney.getCodeNum().subtract(validbet);
            CodeNumChange codeNumChange = CodeNumChange.setCodeNumChange(userId, record, validbet.negate(), userMoney.getCodeNum(), codeNumAfter);
            codeNumChange.setType(0);
            codeNumChange.setPlatform(platform);
            codeNumChangeService.save(codeNumChange);
            userMoney.setCodeNum(codeNumAfter);
            //检查最小清零打码量
            checkClearCodeNum(platformConfig, userId, record, userMoney);
        }
        if(Constants.PLATFORM_WM.equals(platform)){
            gameRecordService.updateCodeNumStatus(record.getId(), Constants.yes);
        }else if(Constants.PLATFORM_PG.equals(platform)||Constants.PLATFORM_CQ9.equals(platform)){
            gameRecordGoldenFService.updateCodeNumStatus(record.getId(), Constants.yes);
        }
        log.info("打码结束,平台={},注单ID={}",platform, record.getBetId());
    }

    /**
     * 最小清0打码量检查
     *
     * @param platformConfig
     * @param userId
     * @param record
     * @param user
     * @return
     */
    public void checkClearCodeNum(PlatformConfig platformConfig, Long userId, GameRecord record, UserMoney user) {
        BigDecimal codeNum = user.getCodeNum();
        BigDecimal minCodeNumVal = DEFAULT_CLEAR;
        if (platformConfig != null && platformConfig.getClearCodeNum() != null) {
            minCodeNumVal = platformConfig.getClearCodeNum();
        }
        //剩余打码量小于等于最小清零打码量时 直接清0
        if (codeNum.compareTo(minCodeNumVal) < 1) {
            userMoneyService.subCodeNum(userId, codeNum);
            CodeNumChange codeNumChange = CodeNumChange.setCodeNumChange(userId, null, null, user.getCodeNum(), user.getCodeNum().subtract(codeNum));
            codeNumChange.setType(1);
            codeNumChange.setClearCodeNum(minCodeNumVal);
            codeNumChangeService.save(codeNumChange);
            log.info("触发最小清零打码量，打码量清0,最小清0点={},注单ID={},UserId={}",minCodeNumVal,record.getBetId(),userId);
        }
    }

    @Transactional
    public void washCode(String platform, GameRecord gameRecord) {
        BigDecimal validbet = new BigDecimal(gameRecord.getValidbet());
        Long userId = gameRecord.getUserId();
        log.info("开始洗码,平台={},注单ID={},注单明细={}",platform, gameRecord.getBetId(), gameRecord.toString());
        WashCodeConfig config = userWashCodeConfigService.getWashCodeConfigByUserIdAndGameId(platform, userId, gameRecord.getGid().toString());
        if (config != null && config.getRate() != null && config.getRate().compareTo(BigDecimal.ZERO) == 1) {
            log.info("游戏洗码配置={}", config.toString());
            //数据库存的10是代表百分之10
            BigDecimal rate = config.getRate().divide(new BigDecimal(100));//转换百分比
            BigDecimal washCodeVal = validbet.multiply(rate);
            WashCodeChange washCodeChange = new WashCodeChange();
            washCodeChange.setUserId(userId);
            washCodeChange.setAmount(washCodeVal);
            washCodeChange.setPlatform(platform);
            washCodeChange.setGameId(gameRecord.getGid().toString());
            washCodeChange.setGameName(gameRecord.getGname());
            washCodeChange.setRate(config.getRate());
            washCodeChange.setValidbet(validbet);
            washCodeChange.setGameRecordId(gameRecord.getId());
            washCodeChangeService.save(washCodeChange);
            userMoneyService.findUserByUserIdUseLock(userId);
            userMoneyService.addWashCode(userId, washCodeVal);
        }
        if(Constants.PLATFORM_WM.equals(platform)) {
            gameRecordService.updateWashCodeStatus(gameRecord.getId(), Constants.yes);
        }else if(Constants.PLATFORM_PG.equals(platform)||Constants.PLATFORM_CQ9.equals(platform)){
            gameRecordGoldenFService.updateWashCodeStatus(gameRecord.getId(), Constants.yes);
        }
        log.info("洗码完成,平台={},注单ID={}",platform, gameRecord.getBetId());
    }

    /**
     * 三级分润
     *
     * @param record
     */
    @Transactional
    public void shareProfit(String platform,GameRecord record) {
        log.info("开始三级分润,平台={},注单ID={},注单明细={}",platform,record.getBetId(), record.toString());
        BigDecimal validbet = new BigDecimal(record.getValidbet());
        Long userId = record.getUserId();
        ShareProfitMqVo shareProfitMqVo=new ShareProfitMqVo();
        shareProfitMqVo.setPlatform(platform);
        shareProfitMqVo.setUserId(userId);
        shareProfitMqVo.setValidbet(validbet);
        shareProfitMqVo.setGameRecordId(record.getId());
        shareProfitMqVo.setBetTime(record.getBetTime());
        rabbitTemplate.convertAndSend(RabbitMqConstants.SHAREPROFIT_DIRECTQUEUE_DIRECTEXCHANGE, RabbitMqConstants.SHAREPROFIT_DIRECT, shareProfitMqVo, new CorrelationData(UUID.randomUUID().toString()));
        log.info("分润消息发送成功,平台={},注单ID={},消息明细={}",platform, record.getBetId(), shareProfitMqVo);
    }
}
