package com.qianyi.casinocore.business;

import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.casinocore.vo.RechargeRecordVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
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

    @Autowired
    private UserService userService;

    @Autowired
    private CodeNumChangeService codeNumChangeService;

    /**
     * 成功订单确认
     * @param id 充值订单id
     * @param status 充值订单id状态
     * @param remark 充值订单备注
     */
    @Transactional
    public ResponseEntity checkOrderSuccess(Long id, Integer status,String remark,String lastModifier) {
        ChargeOrder order = chargeOrderService.findChargeOrderByIdUseLock(id);
        if(order == null || order.getStatus() != Constants.chargeOrder_wait){
            return ResponseUtil.custom("订单不存在或已被处理");
        }
        order.setRemark(remark);
        order.setLastModifier(lastModifier);
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
    public ResponseEntity saveOrderSuccess(User user,ChargeOrder chargeOrder,Integer status,Integer remitType) {
        chargeOrder.setFirstProxy(user.getFirstProxy());
        chargeOrder.setSecondProxy(user.getSecondProxy());
        chargeOrder.setThirdProxy(user.getThirdProxy());
        chargeOrder.setRemitType(remitType);
        return this.saveOrder(chargeOrder,status,AccountChangeEnum.ADD_CODE);
    }

    private ResponseEntity saveOrder(ChargeOrder chargeOrder,Integer status,AccountChangeEnum changeEnum){
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(chargeOrder.getUserId());
        if(userMoney == null){
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
        userMoney.setMoney(userMoney.getMoney().add(subtract));
        //计算打码量 默认2倍
        BigDecimal codeTimes = (platformConfig == null || platformConfig.getBetRate() == null) ? new BigDecimal(2) : platformConfig.getBetRate();
        BigDecimal codeNum = subtract.multiply(codeTimes);
        userMoney.setCodeNum(userMoney.getCodeNum().add(codeNum));
        Integer isFirst = userMoney.getIsFirst() == null ? 0 : userMoney.getIsFirst();
        if (isFirst == 0){
            userMoney.setIsFirst(1);
        }
        userMoneyService.save(userMoney);
        //流水表记录
        RechargeTurnover turnover = getRechargeTurnover(chargeOrder,userMoney, codeNum, codeTimes);
        rechargeTurnoverService.save(turnover);
        //打吗记录
        CodeNumChange codeNumChange = getCodeNumCharge(userMoney.getUserId(),chargeOrder.getOrderNo(),codeNum,userMoney.getCodeNum().subtract(codeNum),userMoney.getCodeNum());
        codeNumChangeService.save(codeNumChange);
        log.info("后台上分userId {} 类型 {}订单号 {} chargeAmount is {}, money is {}",userMoney.getUserId(),
                changeEnum.getCode(),chargeOrder.getOrderNo(),subtract, userMoney.getMoney());
        //用户账变记录
        this.saveAccountChang(changeEnum,userMoney.getUserId(),subtract,userMoney.getMoney(),chargeOrder.getOrderNo());
        //发送充值消息
        this.sendMessage(userMoney.getUserId(),isFirst,chargeOrder);
        return ResponseUtil.success();
    }

    private CodeNumChange getCodeNumCharge(Long userId, String orderNo, BigDecimal codeNum, BigDecimal subtract, BigDecimal amountAfter) {
        CodeNumChange codeNumChange = new CodeNumChange();
        codeNumChange.setUserId(userId);
        codeNumChange.setBetId(orderNo);
        codeNumChange.setAmount(codeNum);
        codeNumChange.setAmountBefore(subtract);
        codeNumChange.setAmountAfter(amountAfter);
        codeNumChange.setType(2);
        return codeNumChange;
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

    private void sendMessage(Long userId,Integer isFirst,ChargeOrder chargeOrder){
        try {
            User user = userService.findById(userId);
            RechargeRecordVo rechargeRecordVo = new RechargeRecordVo();
            rechargeRecordVo.setUserId(userId);
            rechargeRecordVo.setIsFirst(isFirst);
            rechargeRecordVo.setChargeAmount(chargeOrder.getChargeAmount());
            rechargeRecordVo.setChargeOrderId(chargeOrder.getId());
            rechargeRecordVo.setFirstUserId(user.getFirstPid());
            rechargeRecordVo.setSecondUserId(user.getSecondPid());
            rechargeRecordVo.setThirdUserId(user.getThirdPid());
            rechargeRecordVo.setCreateTime(new Date());
            rabbitTemplate.convertAndSend(RabbitMqConstants.CHARGEORDER_DIRECTQUEUE_DIRECTEXCHANGE,
                    RabbitMqConstants.INGCHARGEORDER_DIRECT,rechargeRecordVo,new CorrelationData(UUID.randomUUID().toString()));
            log.info("充值发送消息成功 userId {} isFirst{} chargeAmount {}",userId,isFirst,chargeOrder.getChargeAmount());
        }catch (Exception ex){
            log.error("充值发送消息失败 userId {} isFirst{} chargeAmount {} 错误{} ",userId,isFirst,chargeOrder.getChargeAmount(),ex);
        }

    }
}
