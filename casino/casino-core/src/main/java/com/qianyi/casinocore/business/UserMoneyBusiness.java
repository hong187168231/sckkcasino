package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.ShareProfitVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

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

    //默认最小清零打码量
    private static final BigDecimal DEFAULT_CLEAR=new BigDecimal("10");

    /**
     * @param validbet 有效投注额
     * @param userId   用户id
     * @return
     */
    @Transactional
    public ResponseEntity subCodeNum(PlatformConfig platformConfig,BigDecimal validbet, Long userId,GameRecord record) {
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
            codeNumChangeService.save(userId,gameRecordId,codeNum.negate(),user.getCodeNum(),codeNumAfter);
            user.setCodeNum(codeNumAfter);
        } else {
            //有效投注额小于剩余打码量
            userMoneyService.subCodeNum(userId, validbet);
            BigDecimal codeNumAfter = user.getCodeNum().subtract(validbet);
            codeNumChangeService.save(userId,gameRecordId,validbet.negate(),user.getCodeNum(),codeNumAfter);
            user.setCodeNum(codeNumAfter);
        }
        //扣减完毕后再次检查最小清零打码量
        checkClearCodeNum(platformConfig,userId,gameRecordId,user);
        record.setCodeNumStatus(Constants.yes);
        gameRecordService.save(record);
        return ResponseUtil.success();
    }

    /**
     * 最小清0打码量检查
     * @param platformConfig
     * @param userId
     * @param gameRecordId
     * @param user
     * @return
     */
    @Transactional
    public ResponseEntity checkClearCodeNum(PlatformConfig platformConfig,Long userId,Long gameRecordId,UserMoney user){
        BigDecimal codeNum = user.getCodeNum();
        if (platformConfig != null && platformConfig.getClearCodeNum() != null) {
            BigDecimal minCodeNumVal = platformConfig.getClearCodeNum();
            //剩余打码量小于等于最小清零打码量时 直接清0
            if (codeNum.compareTo(minCodeNumVal) < 1) {
                userMoneyService.subCodeNum(userId, codeNum);
                codeNumChangeService.save(userId,gameRecordId,codeNum.negate(),user.getCodeNum(),user.getCodeNum().subtract(codeNum));
                return ResponseUtil.success();
            }
        }else{
            if (codeNum.compareTo(DEFAULT_CLEAR) < 1) {
                userMoneyService.subCodeNum(userId, codeNum);
                codeNumChangeService.save(userId,gameRecordId,codeNum.negate(),user.getCodeNum(),user.getCodeNum().subtract(codeNum));
                return ResponseUtil.success();
            }
        }
        return ResponseUtil.fail();
    }

    @Transactional
    public BigDecimal washCode(Map<String, BigDecimal> washCode,String platform, GameRecord gameRecord, BigDecimal validbet, Long userId){
        BigDecimal rate = washCode.get(gameRecord.getGid().toString());
        if (rate == null || validbet == null || BigDecimal.ZERO.compareTo(rate) == 0 || BigDecimal.ZERO.compareTo(validbet) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal washCodeVal = validbet.multiply(rate);
        WashCodeChange washCodeChange=new WashCodeChange();
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
        userMoneyService.addWashCode(userId,washCodeVal);
        gameRecord.setWashCodeStatus(Constants.yes);
        gameRecordService.save(gameRecord);
        return washCodeVal;
    }

    /**
     * 三级分润
     * @param validbet
     * @param userId
     * @param record
     */
    @Transactional
    public void shareProfit(BigDecimal validbet, Long userId, GameRecord record) {
        if (validbet == null) {
            updateShareProfitStatus(record);
            return;
        }
        User user = userService.findById(userId);
        if (user == null) {
            updateShareProfitStatus(record);
            return;
        }
        Long firstPid = user.getFirstPid();
        if (firstPid == null) {
            updateShareProfitStatus(record);
            return;
        }
        UserMoney firstUser = userMoneyService.findUserByUserIdUseLock(firstPid);
        if (firstUser == null) {
            updateShareProfitStatus(record);
            return;
        }
        BigDecimal firstRate = new BigDecimal("0.0014");
        BigDecimal secondRate = new BigDecimal("0.0002");
        BigDecimal thirdRate = new BigDecimal("0.00005");
        ShareProfitVo shareProfitVo = new ShareProfitVo();
        //查询当前用户是否是首次下注
        if (user.getIsFirstBet() != null && user.getIsFirstBet() == Constants.no) {
            shareProfitVo.setIsFirst(true);
            user.setIsFirstBet(Constants.yes);
            userService.save(user);
        }
        //一级分润
        BigDecimal firstMoney = validbet.multiply(firstRate).setScale(2, BigDecimal.ROUND_HALF_UP);
        shareProfitVo.setFirstUserId(firstPid);
        shareProfitVo.setFirstMoney(firstMoney);
        userMoneyService.addShareProfit(firstPid, firstMoney);
        //二级分润
        Long secondPid = user.getSecondPid();
        UserMoney secondUser = null;
        if (secondPid != null) {
            secondUser = userMoneyService.findUserByUserIdUseLock(secondPid);
        }
        if (secondUser != null) {
            BigDecimal secondMoney = validbet.multiply(secondRate).setScale(2, BigDecimal.ROUND_HALF_UP);
            shareProfitVo.setSecondUserId(secondPid);
            shareProfitVo.setSecondMoney(secondMoney);
            userMoneyService.addShareProfit(secondPid, secondMoney);
        }
        //三级分润
        Long thirdPid = user.getThirdPid();
        UserMoney thirdUser = null;
        if (thirdPid != null) {
            thirdUser = userMoneyService.findUserByUserIdUseLock(thirdPid);
        }
        if (thirdUser != null) {
            BigDecimal thirdMoney = validbet.multiply(thirdRate).setScale(2, BigDecimal.ROUND_HALF_UP);
            shareProfitVo.setThirdUserId(thirdPid);
            shareProfitVo.setThirdMoney(thirdMoney);
            userMoneyService.addShareProfit(thirdPid, thirdMoney);
        }
        updateShareProfitStatus(record);
    }

    /**
     * 更新分润状态
     * @param record
     */
    @Transactional
    public void updateShareProfitStatus(GameRecord record){
        record.setShareProfitStatus(Constants.yes);
        gameRecordService.save(record);
    }
}
