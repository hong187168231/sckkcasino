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
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class ChargeOrderBusiness {

    @Autowired
    private ChargeOrderService chargeOrderService;

    @Autowired
    private RechargeTurnoverService rechargeTurnoverService;

    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private PlatformConfigService platformConfigService;

    @Autowired
    @Qualifier("accountChangeJob")
    AsyncService asyncService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 成功订单确认
     * @param id 充值订单id
     * @param status 充值订单id状态
     * @param remark 充值订单备注
     */
    @Transactional
    public ResponseEntity checkOrderSuccess(Long id, Integer status,String remark) {
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
        PlatformConfig platformConfig = platformConfigService.findFirst();
        BigDecimal serviceCharge = BigDecimal.ZERO;
        if (platformConfig != null){
            //得到手续费
            serviceCharge = platformConfig.getChargeServiceCharge(chargeOrder.getChargeAmount());
        }
        //计算余额
        BigDecimal subtract = chargeOrder.getChargeAmount().subtract(serviceCharge);
        if (BigDecimal.ZERO.compareTo(subtract) > 0){
            subtract = BigDecimal.ZERO;
            serviceCharge = chargeOrder.getChargeAmount();
        }
        chargeOrder.setPracticalAmount(subtract);
        chargeOrder.setServiceCharge(serviceCharge);
        chargeOrder.setStatus(status);
        chargeOrder = chargeOrderService.saveOrder(chargeOrder);
        user.setMoney(user.getMoney().add(subtract));
        //计算打码量 默认2倍
        BigDecimal codeTimes = (platformConfig == null || platformConfig.getBetRate() == null) ? new BigDecimal(2) : platformConfig.getBetRate();
        BigDecimal codeNum = subtract.multiply(codeTimes);
        user.setCodeNum(user.getCodeNum().add(codeNum));
        Integer isFirst = user.getIsFirst() == null ? 0 : user.getIsFirst();
        if (isFirst == 0){
            user.setIsFirst(1);
        }
        userMoneyService.save(user);
        //流水表记录
        RechargeTurnover turnover = getRechargeTurnover(chargeOrder,user, codeNum, codeTimes);
        rechargeTurnoverService.save(turnover);
        log.info("后台直接上分userId {} 类型 {}订单号 {} chargeAmount is {}, money is {}",user.getUserId(),
                changeEnum.getCode(),chargeOrder.getOrderNo(),subtract, user.getMoney());
        //用户账变记录
        this.saveAccountChang(changeEnum,user.getUserId(),subtract,user.getMoney(),chargeOrder.getOrderNo());
        //发送充值消息
        this.sendMessage(user.getUserId(),isFirst,chargeOrder.getChargeAmount(),new ArrayList<>());
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
    private RechargeTurnover getRechargeTurnover(ChargeOrder order,UserMoney user, BigDecimal codeNum, BigDecimal codeTimes) {
        RechargeTurnover rechargeTurnover = new RechargeTurnover();
        rechargeTurnover.setCodeNum(codeNum);
        rechargeTurnover.setCodeNums(user.getCodeNum());
        rechargeTurnover.setCodeTimes(codeTimes.floatValue());
        rechargeTurnover.setOrderMoney(order.getPracticalAmount());
        rechargeTurnover.setOrderId(order.getId());
        rechargeTurnover.setRemitType(order.getRemitType());
        rechargeTurnover.setUserId(order.getUserId());
        return rechargeTurnover;
    }

    private void sendMessage(Long userId,Integer isFirst,BigDecimal chargeAmount,List<Long> list){
        Map<String,Object> map= new HashMap<>();
        map.put("userId",userId);
        map.put("isFirst",isFirst);
        map.put("chargeAmount",chargeAmount);
        map.put("list",list);
        rabbitTemplate.convertAndSend("ChargeOrderExchange","chargeOrder",map,new CorrelationData(UUID.randomUUID().toString()));
        log.info("success");
    }
}
