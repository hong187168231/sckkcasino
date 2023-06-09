package com.qianyi.casinocore.business;

import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.exception.BusinessException;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.RedisLockUtil;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.casinocore.vo.RechargeRecordVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.DateUtil;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.MessageFormat;
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
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private CodeNumChangeService codeNumChangeService;

    @Autowired
    private AccountChangeService accountChangeService;

    @Autowired
    private UserMoneyBusiness userMoneyBusiness;

    @Autowired
    private RedisLockUtil redisLockUtil;

    /**
     * 成功订单确认
     *
     * @param id 充值订单id
     * @param status 充值订单id状态
     * @param remark 充值订单备注
     */
    @Transactional
    public ResponseEntity checkOrderSuccess(ChargeOrder order, Integer status, String remark, String lastModifier) {
        try {
//            ChargeOrder order = chargeOrderService.findById(id);
//            if (order == null || order.getStatus() != Constants.chargeOrder_wait) {
//                return ResponseUtil.custom("订单不存在或已被处理");
//            }
            order.setRemark(remark);
            order.setLastModifier(lastModifier);
            if (status == Constants.chargeOrder_fail) {// 拒绝订单直接保存
                order.setStatus(status);
                order = chargeOrderService.saveOrder(order);
                return ResponseUtil.success(order);
            }
            return this.saveOrder(order, status, AccountChangeEnum.TOPUP_CODE, Constants.CODENUMCHANGE_CHARGE,true);
        } catch (Exception ex) {
            log.error("审核充值出现异常{}", ex);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseUtil.custom("系统异常请联系管理员");
        }
    }

    /**
     * 新增充值订单，直接充钱
     *
     * @param chargeOrder 充值订单
     */
    @Transactional
    public ResponseEntity saveOrderSuccess(User user, ChargeOrder chargeOrder, Integer status, Integer remitType,
        Integer type) {
        chargeOrder.setFirstProxy(user.getFirstProxy());
        chargeOrder.setSecondProxy(user.getSecondProxy());
        chargeOrder.setThirdProxy(user.getThirdProxy());
        chargeOrder.setRemitType(remitType);
        return this.saveOrder(chargeOrder, status, AccountChangeEnum.ADD_CODE, type,false);
    }

    @Transactional
    public ResponseEntity saveSystemOrderSuccess(String orderNo, User user, ChargeOrder chargeOrder, Integer status,
        Integer remitType, Integer type) {
        chargeOrder.setFirstProxy(user.getFirstProxy());
        chargeOrder.setSecondProxy(user.getSecondProxy());
        chargeOrder.setThirdProxy(user.getThirdProxy());
        chargeOrder.setRemitType(remitType);
        return this.saveSystemOrder(orderNo, chargeOrder, status, AccountChangeEnum.SYSTEM_UPP, type);
    }

    private ResponseEntity saveOrder(ChargeOrder chargeOrder, Integer status, AccountChangeEnum changeEnum,
        Integer type, Boolean tag) {
        Long userId = chargeOrder.getUserId();
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        if (userMoney == null) {
            return ResponseUtil.custom("用户钱包不存在");
        }
        // 输入打码倍率
        BigDecimal codeTimes =
            chargeOrder.getBetRate() == null ? new BigDecimal(CommonConst.NUMBER_2) : chargeOrder.getBetRate();
        chargeOrder.setStatus(status);
        chargeOrder.setSucceedTime(new Date());
        chargeOrder.setBetRate(codeTimes);
        if (tag) {
            chargeOrderService.updateChargeOrder(chargeOrder.getId(), chargeOrder.getRemark(),
                chargeOrder.getLastModifier(), chargeOrder.getStatus(), chargeOrder.getBetRate(), new Date());
        } else {
            chargeOrder = chargeOrderService.saveOrder(chargeOrder);
        }
        BigDecimal totalMoney = userMoney.getMoney().add(chargeOrder.getChargeAmount());
        BigDecimal codeNum = chargeOrder.getChargeAmount().multiply(codeTimes);
        BigDecimal totalCodeNum = userMoney.getCodeNum().add(codeNum);
        Integer isFirst = userMoney.getIsFirst() == null ? 0 : userMoney.getIsFirst();
        if (isFirst == 0) {
            isFirst = 1;
        }
        userMoneyService.addBalanceAndCodeNumAndMoney(userId, chargeOrder.getChargeAmount(), codeNum,
            chargeOrder.getChargeAmount(), isFirst);
        // 流水表记录
        RechargeTurnover turnover = getRechargeTurnover(chargeOrder, totalCodeNum, codeNum, codeTimes);
        rechargeTurnoverService.save(turnover);
        // 打吗记录
        CodeNumChange codeNumChange = getCodeNumCharge(userId, chargeOrder.getOrderNo(), codeNum,
            totalCodeNum.subtract(codeNum), totalCodeNum, type);
        codeNumChangeService.save(codeNumChange);
        log.info("后台上分userId {} 类型 {}订单号 {} chargeAmount is {}, money is {}", userId, changeEnum.getCode(),
            chargeOrder.getOrderNo(), chargeOrder.getChargeAmount(), totalMoney);
        // 用户账变记录
        this.saveAccountChang(changeEnum, userId, chargeOrder, totalMoney);
        // 发送充值消息
        Integer newIsFirst = isFirst;
        ChargeOrder newChargeOrder = chargeOrder;
        new Thread(()->this.sendMessage(userId, newIsFirst, newChargeOrder)).start();
        return ResponseUtil.success(chargeOrder);
    }

    private ResponseEntity saveSystemOrder(String orderNo, ChargeOrder chargeOrder, Integer status,
        AccountChangeEnum changeEnum, Integer type) {
        Long userId = chargeOrder.getUserId();
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(chargeOrder.getUserId());
        if (userMoney == null) {
            return ResponseUtil.custom("用户钱包不存在");
        }
        // 输入打码倍率
        BigDecimal codeTimes =
            chargeOrder.getBetRate() == null ? new BigDecimal(CommonConst.NUMBER_2) : chargeOrder.getBetRate();
        chargeOrder.setStatus(status);
        chargeOrder.setBetRate(codeTimes);
        chargeOrder = chargeOrderService.saveOrder(chargeOrder);
        BigDecimal totalMoney = userMoney.getMoney().add(chargeOrder.getChargeAmount());
        BigDecimal codeNum = chargeOrder.getChargeAmount().multiply(codeTimes);
        BigDecimal totalCodeNum = userMoney.getCodeNum().add(codeNum);
        Integer isFirst = userMoney.getIsFirst() == null ? 0 : userMoney.getIsFirst();
        if (isFirst == 0) {
            isFirst = 1;
        }
        userMoneyService.addBalanceAndCodeNumAndMoney(userId, chargeOrder.getChargeAmount(), codeNum,
            chargeOrder.getChargeAmount(), isFirst);
        // 流水表记录
        RechargeTurnover turnover = getRechargeTurnover(chargeOrder, totalCodeNum, codeNum, codeTimes);
        rechargeTurnoverService.save(turnover);
        // 打吗记录
        CodeNumChange codeNumChange = getCodeNumCharge(userId, chargeOrder.getOrderNo(), codeNum,
            totalCodeNum.subtract(codeNum), totalCodeNum, type);
        codeNumChangeService.save(codeNumChange);
        log.info("后台上分userId {} 类型 {}订单号 {} chargeAmount is {}, money is {}", userId, changeEnum.getCode(),
            chargeOrder.getOrderNo(), chargeOrder.getChargeAmount(), totalMoney);
        // 用户账变记录
        this.saveSystemAccountChang(orderNo, changeEnum, userMoney.getUserId(), chargeOrder, userMoney.getMoney());
        // 发送充值消息
        this.sendMessage(userMoney.getUserId(), isFirst, chargeOrder);
        return ResponseUtil.success(chargeOrder.getChargeAmount());
    }

    private CodeNumChange getCodeNumCharge(Long userId, String orderNo, BigDecimal codeNum, BigDecimal subtract,
        BigDecimal amountAfter, Integer type) {
        CodeNumChange codeNumChange = new CodeNumChange();
        codeNumChange.setUserId(userId);
        codeNumChange.setBetId(orderNo);
        codeNumChange.setAmount(codeNum);
        codeNumChange.setAmountBefore(subtract);
        codeNumChange.setAmountAfter(amountAfter);
        codeNumChange.setType(type);
        return codeNumChange;
    }

    private void saveAccountChang(AccountChangeEnum changeEnum, Long userId, ChargeOrder chargeOrder,
        BigDecimal amountAfter) {
        AccountChange change = new AccountChange();
        change.setUserId(userId);
        change.setOrderNo(getOrderNo(changeEnum));
        change.setType(changeEnum.getType());
        change.setAmount(chargeOrder.getChargeAmount());
        change.setAmountBefore(amountAfter.subtract(chargeOrder.getChargeAmount()));
        change.setAmountAfter(amountAfter);
        change.setFirstProxy(chargeOrder.getFirstProxy());
        change.setSecondProxy(chargeOrder.getSecondProxy());
        change.setThirdProxy(chargeOrder.getThirdProxy());
        accountChangeService.save(change);
    }

    private void saveSystemAccountChang(String orderNo, AccountChangeEnum changeEnum, Long userId,
        ChargeOrder chargeOrder, BigDecimal amountAfter) {
        AccountChange change = new AccountChange();
        change.setUserId(userId);
        change.setOrderNo(orderNo);
        change.setType(changeEnum.getType());
        change.setAmount(chargeOrder.getChargeAmount());
        change.setAmountBefore(amountAfter.subtract(chargeOrder.getChargeAmount()));
        change.setAmountAfter(amountAfter);
        change.setFirstProxy(chargeOrder.getFirstProxy());
        change.setSecondProxy(chargeOrder.getSecondProxy());
        change.setThirdProxy(chargeOrder.getThirdProxy());
        accountChangeService.save(change);
    }

    public String getOrderNo(AccountChangeEnum changeEnum) {
        String orderNo = changeEnum.getCode();
        String today = DateUtil.today("yyyyMMddHHmmssSSS");
        String randNum = CommonUtil.random(3);
        orderNo = orderNo + today + randNum;
        return orderNo;
    }

    private RechargeTurnover getRechargeTurnover(ChargeOrder order, BigDecimal totalCodeNum, BigDecimal codeNum,
        BigDecimal codeTimes) {
        RechargeTurnover rechargeTurnover = new RechargeTurnover();
        rechargeTurnover.setCodeNum(codeNum);
        rechargeTurnover.setCodeNums(totalCodeNum);
        rechargeTurnover.setCodeTimes(codeTimes.floatValue());
        rechargeTurnover.setOrderMoney(order.getChargeAmount());
        rechargeTurnover.setOrderId(order.getId());
        rechargeTurnover.setRemitType(order.getRemitType());
        rechargeTurnover.setUserId(order.getUserId());
        return rechargeTurnover;
    }

    private void sendMessage(Long userId, Integer isFirst, ChargeOrder chargeOrder) {
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
                RabbitMqConstants.INGCHARGEORDER_DIRECT, rechargeRecordVo,
                new CorrelationData(UUID.randomUUID().toString()));
            log.info("充值发送消息成功 userId {} isFirst{} chargeAmount {}", userId, isFirst, chargeOrder.getChargeAmount());
        } catch (Exception ex) {
            log.error("充值发送消息失败 userId {} isFirst{} chargeAmount {} 错误{} ", userId, isFirst,
                chargeOrder.getChargeAmount(), ex);
        }

    }

    @Transactional
    public void cleanUserMud(Long userId) {
        User user = userService.findById(userId);
        if (null == user) {
            throw new BusinessException("账户不存在");
        }
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        if (null == userMoney) {
            throw new BusinessException("用户钱包不存在");
        }
        // 打码量
        BigDecimal mud = userMoney.getCodeNum();
        if (mud.compareTo(BigDecimal.ZERO) < 1) {
            throw new BusinessException("打码量不能低于0");
        }
        // 清空打码量
        userMoney.setCodeNum(BigDecimal.ZERO);
        userMoneyService.save(userMoney);

        // 打码记录
        CodeNumChange change = new CodeNumChange();
        change.setUserId(userId);
        change.setAmountBefore(mud);
        change.setAmount(mud.negate());
        change.setAmountAfter(BigDecimal.ZERO);
        // 5=>总控人工清零
        change.setType(5);
        userMoneyBusiness.subBalanceAdmin(userId, userMoney.getBalance());
        codeNumChangeService.save(change);
    }

}
