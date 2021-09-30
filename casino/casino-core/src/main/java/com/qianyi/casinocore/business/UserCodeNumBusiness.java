package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 用户打码量
 */
@Slf4j
@Service
public class UserCodeNumBusiness {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private WashCodeChangeService washCodeChangeService;
    @Autowired
    private CodeNumChangeService codeNumChangeService;

    //默认最小清零打码量
    private static final BigDecimal DEFAULT_CLEAR=new BigDecimal("10");

    /**
     * @param validbet 有效投注额
     * @param userId   用户id
     * @return
     */
    @Transactional
    public ResponseEntity subCodeNum(SysConfig minCodeNum,BigDecimal validbet, Long userId,Long gameRecordId) {
        if (validbet == null || userId == null) {
            return ResponseUtil.fail();
        }
        if (BigDecimal.ZERO.compareTo(validbet) == 0) {
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
        //最小清零打码量
        if (minCodeNum != null && !ObjectUtils.isEmpty(minCodeNum.getValue())) {
            BigDecimal minCodeNumVal = new BigDecimal(minCodeNum.getValue());
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
        //有效投注额大于等于等于剩余打码量
        if (validbet.compareTo(codeNum) > -1) {
            userMoneyService.subCodeNum(userId, codeNum);
            codeNumChangeService.save(userId,gameRecordId,codeNum.negate(),user.getCodeNum(),user.getCodeNum().subtract(codeNum));
        } else {
            //有效投注额小于剩余打码量
            userMoneyService.subCodeNum(userId, validbet);
            codeNumChangeService.save(userId,gameRecordId,validbet.negate(),user.getCodeNum(),user.getCodeNum().subtract(validbet));
        }
        return ResponseUtil.success();
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
        return washCodeVal;
    }
}
