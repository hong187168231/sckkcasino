package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.BetRatioConfig;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.service.BetRatioConfigService;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
    private BetRatioConfigService betRatioConfigService;

    //默认最小清零打码量
    private static final BigDecimal DEFAULT_CLEAR=new BigDecimal("10");

    /**
     * @param validbet 有效投注额
     * @param userId   用户id
     * @return
     */
    @Transactional
    public ResponseEntity subCodeNum(BigDecimal validbet, Long userId) {
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
        BetRatioConfig betRatioConfig = betRatioConfigService.findOneBetRatioConfig();
        if (betRatioConfig != null && betRatioConfig.getMinMoney() != null) {
            //剩余打码量小于等于最小清零打码量时 直接清0
            if (codeNum.compareTo(betRatioConfig.getMinMoney()) < 1) {
                userMoneyService.subCodeNum(userId, codeNum);
                return ResponseUtil.success();
            }
        }else{
            if (codeNum.compareTo(DEFAULT_CLEAR) < 1) {
                userMoneyService.subCodeNum(userId, codeNum);
                return ResponseUtil.success();
            }
        }
        //有效投注额大于等于等于剩余打码量
        if (validbet.compareTo(codeNum) > -1) {
            userMoneyService.subCodeNum(userId, codeNum);
        } else {
            //有效投注额小于剩余打码量
            userMoneyService.subCodeNum(userId, validbet);
        }
        return ResponseUtil.success();
    }
}
