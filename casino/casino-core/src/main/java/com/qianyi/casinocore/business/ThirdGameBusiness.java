package com.qianyi.casinocore.business;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.Order;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.service.OrderService;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.service.UserThirdService;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.livegoldenf.api.PublicGoldenFApi;
import com.qianyi.livewm.api.PublicWMApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;

@Slf4j
@Service
public class ThirdGameBusiness {

    @Autowired
    private UserThirdService userThirdService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private UserService userService;
    @Autowired
    private PublicGoldenFApi goldenFApi;
    @Autowired
    private PublicWMApi wmApi;
    @Autowired
    @Qualifier("accountChangeJob")
    private AsyncService asyncService;

    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public ResponseEntity oneKeyRecoverGoldenF(Long userId) {
        log.info("开始回收PG/CQ9余额，userId={}", userId);
        if (userId == null) {
            return ResponseUtil.parameterNotNull();
        }
        UserThird third = userThirdService.findByUserId(userId);
        if (third == null || ObjectUtils.isEmpty(third.getGoldenfAccount())) {
            return ResponseUtil.custom("当前用户暂未进入过游戏");
        }
        ResponseEntity<BigDecimal> responseEntity = getBalanceGoldenF(third.getGoldenfAccount(), userId);
        if (responseEntity.getCode() != ResponseCode.SUCCESS.getCode()) {
            return responseEntity;
        }
        BigDecimal balance = new BigDecimal(responseEntity.getData().doubleValue());
        if (balance.compareTo(BigDecimal.ONE) == -1) {
            log.error("userId:{},balance={},PG/CQ9金额小于1，不可回收", userId, balance);
            return ResponseUtil.custom("PG/CQ9余额小于1,不可回收");
        }
        String orderNo = orderService.getOrderNo();
        String goldenfAccount = third.getGoldenfAccount();
        //调用提值接口扣减余额  存在精度问题，只回收整数部分
        BigDecimal recoverMoney = balance.setScale(0, BigDecimal.ROUND_DOWN);
        PublicGoldenFApi.ResponseEntity transferOut = goldenFApi.transferOut(goldenfAccount, recoverMoney.doubleValue(), orderNo, null);
        if (transferOut == null) {
            log.error("userId:{},键回收当前登录用户PG/CQ9余额失败", userId);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (!ObjectUtils.isEmpty(transferOut.getErrorCode())) {
            log.error("userId:{},errorCode={},errorMsg={}", userId, transferOut.getErrorCode(), transferOut.getErrorMessage());
            return ResponseUtil.custom("回收失败,请联系客服");
        }
        //三方强烈建议提值/充值后使用 5.9 获取单个玩家的转账记录 进一步确认交易是否成功，避免造成金额损失
        long time = System.currentTimeMillis();
        PublicGoldenFApi.ResponseEntity playerTransactionRecord = goldenFApi.getPlayerTransactionRecord(goldenfAccount, time, time, null, orderNo, null);
        if (playerTransactionRecord == null) {
            log.error("userId:{},进游戏查询转账记录失败", userId);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (!ObjectUtils.isEmpty(playerTransactionRecord.getErrorCode())) {
            log.error("userId:{},errorCode={},errorMsg={}", userId, playerTransactionRecord.getErrorCode(), playerTransactionRecord.getErrorMessage());
            return ResponseUtil.custom("回收失败,请联系客服");
        }
        JSONObject jsonData = JSONObject.parseObject(playerTransactionRecord.getData());
        JSONArray translogs = jsonData.getJSONArray("translogs");
        if (translogs.size() == 0) {
            return ResponseUtil.custom("回收失败,请联系客服");
        }
        //把额度加回本地
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        if (userMoney == null) {
            userMoney = new UserMoney();
            userMoney.setUserId(userId);
            userMoneyService.save(userMoney);
        }
        userMoneyService.addMoney(userId, recoverMoney);
        //记录账变
        User user = userService.findById(userId);
        saveAccountChange(userId, recoverMoney, userMoney.getMoney(), recoverMoney.add(userMoney.getMoney()), 1, orderNo, AccountChangeEnum.PG_CQ9_OUT,"自动转出PG/CQ9", user);
        log.info("PG/CQ9余额回收成功，userId={}", userId);
        return ResponseUtil.success();
    }

    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public ResponseEntity oneKeyRecoverWm(Long userId) {
        log.info("开始回收wm余额，userId={}", userId);
        if (userId == null) {
            return ResponseUtil.parameterNotNull();
        }
        UserThird third = userThirdService.findByUserId(userId);
        if (third == null || ObjectUtils.isEmpty(third.getAccount())) {
            return ResponseUtil.custom("WM余额为0");
        }
        User user = userService.findById(userId);
        Integer lang = user.getLanguage();
        if (lang == null) {
            lang = 0;
        }
        String account = third.getAccount();
        //先退出游戏
        Boolean aBoolean = wmApi.logoutGame(account, lang);
        if (!aBoolean) {
            log.error("userId:{},退出游戏失败", userId);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        //查询用户在wm的余额
        BigDecimal balance = BigDecimal.ZERO;
        try {
            balance = wmApi.getBalance(account, lang);
            if (balance == null) {
                log.error("userId:{},获取用户WM余额为null", userId);
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
        } catch (Exception e) {
            log.error("userId:{},获取用户WM余额失败{}", userId, e.getMessage());
            e.printStackTrace();
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (balance.compareTo(BigDecimal.ONE) == -1) {
            log.error("userId:{},balance={},金额小于1，不可回收", userId, balance);
            return ResponseUtil.custom("WM余额小于1,不可回收");
        }
        //调用加扣点接口扣减wm余额  存在精度问题，只回收整数部分
        BigDecimal recoverMoney = balance.negate().setScale(0, BigDecimal.ROUND_DOWN);
        PublicWMApi.ResponseEntity entity = wmApi.changeBalance(account, recoverMoney, null, lang);
        if (entity.getErrorCode() != 0) {
            log.error("userId:{},errorCode={},errorMsg={}", userId, entity.getErrorCode(), entity.getErrorMessage());
            return ResponseUtil.custom("回收失败,请联系客服");
        }
        balance = recoverMoney.abs();
        //把额度加回本地
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        if (userMoney == null) {
            userMoney = new UserMoney();
            userMoney.setUserId(userId);
            userMoneyService.save(userMoney);
        }
        userMoneyService.addMoney(userId, balance);
        String orderNo = orderService.getOrderNo();
        saveAccountChange(userId, balance, userMoney.getMoney(), balance.add(userMoney.getMoney()), 1, orderNo, AccountChangeEnum.RECOVERY,"自动转出WM", user);
        log.info("wm余额回收成功，userId={}", userId);
        return ResponseUtil.success();
    }

    public ResponseEntity<BigDecimal> getBalanceGoldenF(String account, Long userId) {
        PublicGoldenFApi.ResponseEntity playerBalance = goldenFApi.getPlayerBalance(account, null);
        if (playerBalance == null) {
            log.error("userId:{},查询余额失败", userId);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (!ObjectUtils.isEmpty(playerBalance.getErrorCode())) {
            log.error("userId:{},errorCode={},errorMsg={}", userId, playerBalance.getErrorCode(), playerBalance.getErrorMessage());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        JSONObject jsonData = JSONObject.parseObject(playerBalance.getData());
        BigDecimal balance = new BigDecimal(jsonData.getDouble("balance"));
        return ResponseUtil.success(balance.setScale(2, BigDecimal.ROUND_HALF_UP));
    }

    public void saveAccountChange(Long userId, BigDecimal amount, BigDecimal amountBefore, BigDecimal amountAfter, Integer type, String orderNo,AccountChangeEnum changeEnum, String remark, User user) {
        Order order = new Order();
        order.setMoney(amount);
        order.setUserId(userId);
        order.setRemark(remark);
        order.setType(type);
        order.setState(Constants.order_wait);
        order.setNo(orderNo);
        order.setFirstProxy(user.getFirstProxy());
        order.setSecondProxy(user.getSecondProxy());
        order.setThirdProxy(user.getThirdProxy());
        orderService.save(order);

        //账变中心记录账变
        AccountChangeVo vo = new AccountChangeVo();
        vo.setUserId(userId);
        vo.setChangeEnum(changeEnum);
        vo.setAmount(amount.negate());
        vo.setAmountBefore(amountBefore);
        vo.setAmountAfter(amountAfter);
        asyncService.executeAsync(vo);
    }
}
