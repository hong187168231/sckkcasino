package com.qianyi.casinocore.business;

import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Autowired
    @Qualifier("accountChangeJob")
    AsyncService asyncService;

    /**
     * 成功订单确认
     * @param id 充值订单id
     * @param status 充值订单id状态
     * @param remark 充值订单备注
     */
    @Transactional
    public ResponseEntity checkOrderSuccess(Long id, Integer status,String remark,String money) {
        ChargeOrder order = chargeOrderService.findChargeOrderByIdUseLock(id);
        if(order == null || order.getStatus() != Constants.chargeOrder_wait){
            return ResponseUtil.custom("订单不存在或已被处理");
        }
        order.setRemark(remark);
        if(status == Constants.chargeOrder_fail){//拒绝订单直接保存
            order.setStatus(status);
            order = chargeOrderService.saveOrder(order);
            return ResponseUtil.success(order);
        }
        BigDecimal realityAmount = null;
        try {
            realityAmount = new BigDecimal(money);
        }catch (Exception e){
            realityAmount = new BigDecimal(-1);
        }
        if(realityAmount.compareTo(BigDecimal.ZERO)<1){
            return ResponseUtil.custom("金额类型错误");
        }
        order.setRealityAmount(realityAmount);
        return this.saveOrder(order,status,AccountChangeEnum.TOPUP_CODE);
    }
    /**
     * 新增充值订单，直接充钱
     * @param  chargeOrder 充值订单
     */
    @Transactional
    public ResponseEntity saveOrderSuccess(ChargeOrder chargeOrder) {
        return this.saveOrder(chargeOrder,Constants.chargeOrder_success,AccountChangeEnum.ADD_CODE);
    }

    private ResponseEntity saveOrder(ChargeOrder chargeOrder,Integer status,AccountChangeEnum changeEnum){
        UserMoney user = userMoneyService.findUserByUserIdUseLock(chargeOrder.getUserId());
        if(user == null){
            return ResponseUtil.custom("用户钱包不存在");
        }
        AmountConfig amountConfig = amountConfigService.findAmountConfigById(1L);
        BigDecimal serviceCharge = BigDecimal.ZERO;
        if (amountConfig != null){
            //得到手续费
            serviceCharge = amountConfig.getServiceCharge(chargeOrder.getRealityAmount());
        }
        //计算余额
        BigDecimal subtract = chargeOrder.getRealityAmount().subtract(serviceCharge);
        chargeOrder.setPracticalAmount(subtract);
        chargeOrder.setServiceCharge(serviceCharge);
        chargeOrder.setStatus(status);
        chargeOrder = chargeOrderService.saveOrder(chargeOrder);
        user.setMoney(user.getMoney().add(subtract));
        //计算打码量 默认2倍
        BetRatioConfig betRatioConfig = betRatioConfigService.findOneBetRatioConfig();
        float codeTimes = (betRatioConfig == null || betRatioConfig.getCodeTimes() == null) ? 2F : betRatioConfig.getCodeTimes();
        BigDecimal codeNum = chargeOrder.getRealityAmount().multiply(BigDecimal.valueOf(codeTimes));
        user.setCodeNum(user.getCodeNum().add(codeNum));
        userMoneyService.save(user);
        //流水表记录
        RechargeTurnover turnover = getRechargeTurnover(chargeOrder,user, codeNum, codeTimes);
        rechargeTurnoverService.save(turnover);
        log.info("后台直接上分userId {} 类型 {}订单号 {} chargeAmount is {}, money is {}",user.getUserId(),
                changeEnum.getCode(),chargeOrder.getOrderNo(),subtract, user.getMoney());
        //用户账变记录
        this.saveAccountChang(changeEnum,user.getUserId(),subtract,user.getMoney(),chargeOrder.getOrderNo());
        return ResponseUtil.success();
    }
    private void saveAccountChang(AccountChangeEnum changeEnum, Long userId, BigDecimal amount, BigDecimal amountAfter, String orderNo){
        AccountChangeVo vo=new AccountChangeVo();
        vo.setUserId(userId);
        vo.setOrderNo(orderNo);
        vo.setChangeEnum(changeEnum);
        vo.setAmount(amount);
        vo.setAmountBefore(amountAfter.subtract(amount));
        vo.setAmountAfter(amountAfter);
        asyncService.executeAsync(vo);
    }
    private RechargeTurnover getRechargeTurnover(ChargeOrder order,UserMoney user, BigDecimal codeNum, float codeTimes) {
        RechargeTurnover rechargeTurnover = new RechargeTurnover();
        rechargeTurnover.setCodeNum(codeNum);
        rechargeTurnover.setCodeNums(user.getCodeNum());
        rechargeTurnover.setCodeTimes(codeTimes);
        rechargeTurnover.setOrderMoney(order.getPracticalAmount());
        rechargeTurnover.setOrderId(order.getId());
        rechargeTurnover.setRemitType(order.getRemitType());
        rechargeTurnover.setUserId(order.getUserId());
        return rechargeTurnover;
    }
}
