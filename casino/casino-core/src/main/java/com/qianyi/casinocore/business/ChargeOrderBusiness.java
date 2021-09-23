package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.modulecommon.Constants;
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

    @Autowired
    private AmountConfigService amountConfigService;

    /**
     * 成功订单确认
     * @param id 充值订单id
     * @param status 充值订单id状态
     * @param remark 充值订单备注
     */
    @Transactional
    public ResponseEntity checkOrderSuccess(Long id, Integer status,String remark) {
        ChargeOrder order = chargeOrderService.findChargeOrderByIdUseLock(id);
        if(order == null || order.getStatus() != 0){
            return ResponseUtil.custom("订单不存在或已被处理");
        }
        order.setStatus(status);
        order.setRemark(remark);
        if(status != Constants.yes){//拒绝订单直接保存
            order = chargeOrderService.saveOrder(order);
            return ResponseUtil.success(order);
        }
        return this.saveOrder(order);
    }
    /**
     * 新增充值订单，直接充钱
     * @param  chargeOrder 充值订单
     */
    @Transactional
    public ResponseEntity saveOrderSuccess(ChargeOrder chargeOrder) {
        return this.saveOrder(chargeOrder);
    }

    private ResponseEntity saveOrder(ChargeOrder chargeOrder){
        UserMoney user = userMoneyService.findUserByUserIdUseLock(chargeOrder.getUserId());
        if(user == null){
            return ResponseUtil.custom("用户钱包不存在");
        }
        AmountConfig amountConfig = amountConfigService.findAmountConfigById(1L);
        BigDecimal serviceCharge = BigDecimal.ZERO;
        if (amountConfig != null){
            //得到手续费
            serviceCharge = amountConfig.getServiceCharge(chargeOrder.getChargeAmount());
        }
        //计算余额
        BigDecimal subtract = chargeOrder.getChargeAmount().subtract(serviceCharge);
        chargeOrder.setPracticalAmount(subtract);
        chargeOrder.setServiceCharge(serviceCharge);
        chargeOrder = chargeOrderService.saveOrder(chargeOrder);
        user.setMoney(user.getMoney().add(subtract));
        //计算打码量 默认2倍
        BetRatioConfig betRatioConfig = betRatioConfigService.findOneBetRatioConfig();
        float codeTimes = (betRatioConfig == null || betRatioConfig.getCodeTimes() == null) ? 2F : betRatioConfig.getCodeTimes();
        BigDecimal codeNum = chargeOrder.getChargeAmount().multiply(BigDecimal.valueOf(codeTimes));
        user.setCodeNum(user.getCodeNum().add(codeNum));
        userMoneyService.save(user);
        //流水表记录
        RechargeTurnover turnover = getRechargeTurnover(chargeOrder,user, codeNum, codeTimes);
        rechargeTurnoverService.save(turnover);
        return ResponseUtil.success();
    }
    private RechargeTurnover getRechargeTurnover(ChargeOrder order,UserMoney user, BigDecimal codeNum, float codeTimes) {
        RechargeTurnover rechargeTurnover = new RechargeTurnover();
        rechargeTurnover.setCodeNum(codeNum);
        rechargeTurnover.setCodeNums(user.getCodeNum().add(codeNum));
        rechargeTurnover.setCodeTimes(codeTimes);
        rechargeTurnover.setOrderMoney(order.getPracticalAmount());
        rechargeTurnover.setOrderId(order.getId());
        rechargeTurnover.setRemitType(order.getRemitType());
        rechargeTurnover.setUserId(order.getUserId());
        return rechargeTurnover;
    }
}
