package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;

@Slf4j
@Service
public class ChargeOrderBusiness {

    @Autowired
    private ChargeOrderService chargeOrderService;

    @Autowired
    private BetRatioConfigService betRatioConfigService;

    @Autowired
    private RechargeTurnoverService rechargeTurnoverService;

    @Autowired
    private UserMoneyService userMoneyService;

    /**
     * 成功订单确认
     * @param order
     */
    @Transactional
    public ResponseEntity checkOrderSuccess(ChargeOrder order) {
        UserMoney user = userMoneyService.findUserByUserIdUseLock(order.getUserId());
        if(user == null){
            return ResponseUtil.custom("用户钱包不存在");
        }
        ChargeOrder orde = chargeOrderService.saveOrder(order);
        //计算打码量
        userMoneyService.updateMoney(user.getId(), orde.getChargeAmount());
        BetRatioConfig betRatioConfig = betRatioConfigService.findOneBetRatioConfig();
        //默认2倍
        float codeTimes = (betRatioConfig == null || betRatioConfig.getCodeTimes() == null) ? 2F : betRatioConfig.getCodeTimes();
        BigDecimal codeNum = order.getChargeAmount().multiply(BigDecimal.valueOf(codeTimes));
        userMoneyService.updateCodeNum(user.getId(), codeNum);
        //流水表记录
        RechargeTurnover turnover = getRechargeTurnover(order, codeNum, codeTimes);
        rechargeTurnoverService.save(turnover);
        return ResponseUtil.success();
    }

    private RechargeTurnover getRechargeTurnover(ChargeOrder order, BigDecimal codeNum, float codeTimes) {
        RechargeTurnover rechargeTurnover = new RechargeTurnover();
        rechargeTurnover.setCodeNum(codeNum);
        rechargeTurnover.setCodeTimes(codeTimes);
        rechargeTurnover.setOrderMoney(order.getChargeAmount());
        rechargeTurnover.setOrderId(order.getId());
        return rechargeTurnover;
    }
}
