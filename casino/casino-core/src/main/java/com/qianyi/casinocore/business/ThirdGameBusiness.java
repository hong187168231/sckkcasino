package com.qianyi.casinocore.business;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.livegoldenf.api.PublicGoldenFApi;
import com.qianyi.liveob.api.PublicObApi;
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
    private PublicObApi obApi;
    @Autowired
    private PlatformGameService platformGameService;
    @Autowired
    private AdGamesService adGamesService;
    @Autowired
    private ErrorOrderService errorOrderService;
    @Autowired
    @Qualifier("accountChangeJob")
    private AsyncService asyncService;

    public ResponseEntity oneKeyRecoverGoldenF(Long userId) {
        log.info("开始回收PG/CQ9余额，userId={}", userId);
        if (userId == null) {
            return ResponseUtil.parameterNotNull();
        }
        UserThird third = userThirdService.findByUserId(userId);
        if (third == null || ObjectUtils.isEmpty(third.getGoldenfAccount())) {
            return ResponseUtil.custom("PG/CQ9余额为0");
        }
        ResponseEntity<BigDecimal> responseEntity = getBalanceGoldenF(third.getGoldenfAccount(), userId);
        if (responseEntity.getCode() != ResponseCode.SUCCESS.getCode()) {
            return responseEntity;
        }
        BigDecimal balance = responseEntity.getData();
        if (balance.compareTo(BigDecimal.ONE) == -1) {
            log.info("userId:{},balance={},PG/CQ9金额小于1，不可回收", userId, balance);
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
        UserMoney userMoney = userMoneyService.findByUserId(userId);
        if (userMoney == null) {
            userMoney = new UserMoney();
            userMoney.setUserId(userId);
            userMoneyService.save(userMoney);
        }
        userMoneyService.addMoney(userId, recoverMoney);
        //记录账变
        User user = userService.findById(userId);
        saveAccountChange(Constants.PLATFORM_PG_CQ9,userId, recoverMoney, userMoney.getMoney(), recoverMoney.add(userMoney.getMoney()), 1, orderNo, AccountChangeEnum.PG_CQ9_OUT,"自动转出PG/CQ9", user);
        log.info("PG/CQ9余额回收成功，userId={}", userId);
        return ResponseUtil.success();
    }

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
            log.error("userId:{},account={},退出游戏失败", userId, user.getAccount());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        //查询用户在wm的余额
        BigDecimal balance = BigDecimal.ZERO;
        try {
            balance = wmApi.getBalance(account, lang);
            if (balance == null) {
                log.error("userId:{},account={},获取用户WM余额为null", userId, user.getAccount());
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
        } catch (Exception e) {
            log.error("userId:{},account={},获取用户WM余额失败{}", userId, user.getAccount(), e.getMessage());
            e.printStackTrace();
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (balance.compareTo(BigDecimal.ONE) == -1) {
            log.info("userId:{},account={},balance={},WM金额小于1，不可回收", userId, user.getAccount(), balance);
            return ResponseUtil.custom("WM余额小于1,不可回收");
        }
        //调用加扣点接口扣减wm余额  存在精度问题，只回收整数部分
        BigDecimal recoverMoney = balance.negate().setScale(0, BigDecimal.ROUND_DOWN);
        String orderNo = orderService.getOrderNo();
        PublicWMApi.ResponseEntity entity = wmApi.changeBalance(account, recoverMoney, orderNo, lang);
        if (entity == null) {
            log.error("加扣点失败,远程请求异常,userId:{},account={},money={}", userId, user.getAccount(), recoverMoney);
            //异步记录错误订单并重试补偿
            errorOrderService.syncSaveErrorOrder(third.getAccount(), user.getId(), user.getAccount(), orderNo, recoverMoney, AccountChangeEnum.RECOVERY, Constants.PLATFORM_WM_BIG);
            return ResponseUtil.custom("回收失败,请联系客服");
        }
        if (entity.getErrorCode() != 0) {
            log.error("加扣点失败，userId:{},account={},money={},errorCode={},errorMsg={}", userId, user.getAccount(), recoverMoney, entity.getErrorCode(), entity.getErrorMessage());
            return ResponseUtil.custom("回收失败,请联系客服");
        }
        balance = recoverMoney.abs();
        //把额度加回本地
        UserMoney userMoney = userMoneyService.findByUserId(userId);
        if (userMoney == null) {
            userMoney = new UserMoney();
            userMoney.setUserId(userId);
            userMoneyService.save(userMoney);
        }
        userMoneyService.addMoney(userId, balance);
        saveAccountChange(Constants.PLATFORM_WM_BIG, userId, balance, userMoney.getMoney(), balance.add(userMoney.getMoney()), 1, orderNo, AccountChangeEnum.RECOVERY, "自动转出WM", user);
        log.info("wm余额回收成功，userId={},account={}", userId, user.getAccount());
        return ResponseUtil.success();
    }

    public ResponseEntity oneKeyRecoverOb(Long userId) {
        log.info("开始回收OB电竞余额，userId={}", userId);
        if (userId == null) {
            return ResponseUtil.parameterNotNull();
        }
        UserThird third = userThirdService.findByUserId(userId);
        if (third == null || ObjectUtils.isEmpty(third.getObAccount())) {
            return ResponseUtil.custom("OB电竞余额为0");
        }
        User user = userService.findById(userId);
        String account = third.getObAccount();
        PublicObApi.ResponseEntity obApiBalance = obApi.getBalance(account);
        if (obApiBalance == null) {
            log.error("userId:{},account={},获取用户OB电竞余额为失败,远程请求异常", userId, user.getAccount());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (PublicObApi.STATUS_FALSE.equals(obApiBalance.getStatus())) {
            log.error("userId:{},account={},获取用户OB电竞余额为失败，msg={}", userId, user.getAccount(), obApiBalance.getData());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (ObjectUtils.isEmpty(obApiBalance.getData())) {
            log.error("userId:{},account={},获取用户OB电竞余额为null,msg={}", userId, user.getAccount(), obApiBalance.getData());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        BigDecimal balance = new BigDecimal(obApiBalance.getData());
        if (balance.compareTo(BigDecimal.ONE) == -1) {
            log.info("userId:{},account={},balance={},OB电竞金额小于1，不可回收", userId, user.getAccount(), balance);
            return ResponseUtil.custom("OB余额小于1,不可回收");
        }
        //调用加扣点接口扣减wm余额  存在精度问题，只回收整数部分
        balance = balance.setScale(0, BigDecimal.ROUND_DOWN);
        String orderNo = orderService.getObOrderNo();
        PublicObApi.ResponseEntity transfer = obApi.transfer(account, 2, balance, orderNo);
        if (transfer == null) {
            log.error("OB电竞扣点失败,远程请求异常,userId:{},account={},money={}", userId, user.getAccount(), balance);
            //异步记录错误订单
            errorOrderService.syncSaveErrorOrder(third.getAccount(), user.getId(), user.getAccount(), orderNo, balance, AccountChangeEnum.OBDJ_OUT, Constants.PLATFORM_OBDJ);
            return ResponseUtil.custom("回收失败,请联系客服");
        }
        if (PublicObApi.STATUS_FALSE.equals(transfer.getStatus())) {
            log.error("OB电竞扣点失败,userId:{},account={},money={},msg={}", userId, user.getAccount(), balance, transfer.getData());
            return ResponseUtil.custom("回收失败,请联系客服");
        }
        //把额度加回本地
        UserMoney userMoney = userMoneyService.findByUserId(userId);
        if (userMoney == null) {
            userMoney = new UserMoney();
            userMoney.setUserId(userId);
            userMoneyService.save(userMoney);
        }
        userMoneyService.addMoney(userId, balance);
        log.info("userMoney加回成功，userId={},balance={}", userId, balance);
        saveAccountChange(Constants.PLATFORM_OBDJ, userId, balance, userMoney.getMoney(), balance.add(userMoney.getMoney()), 1, orderNo, AccountChangeEnum.OBDJ_OUT, "自动转出OB电竞", user);
        log.info("OB电竞余额回收成功，userId={},account={},money={}", userId, user.getAccount(), balance);
        return ResponseUtil.success();
    }

    public ResponseEntity<BigDecimal> getBalanceGoldenF(String account, Long userId) {
        PublicGoldenFApi.ResponseEntity playerBalance = goldenFApi.getPlayerBalance(account, null);
        if (playerBalance == null) {
            log.error("userId:{},查询余额失败", userId);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (!ObjectUtils.isEmpty(playerBalance.getErrorCode())) {
            log.error("userId:{},account={},errorCode={},errorMsg={}", userId, account, playerBalance.getErrorCode(), playerBalance.getErrorMessage());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        JSONObject jsonData = JSONObject.parseObject(playerBalance.getData());
        BigDecimal balance = new BigDecimal(jsonData.getDouble("balance"));
        return ResponseUtil.success(balance);
    }

    public ResponseEntity<BigDecimal> getBalanceOb(String account, Long userId) {
        PublicObApi.ResponseEntity balanceResult = obApi.getBalance(account);
        if (balanceResult == null) {
            log.error("userId:{},查询OB余额失败,远程请求异常", userId);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (PublicObApi.STATUS_FALSE.equals(balanceResult.getStatus())) {
            log.error("userId:{},查询OB余额失败,msg:{}", userId, balanceResult.getData());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        BigDecimal balance = new BigDecimal(balanceResult.getData());
        return ResponseUtil.success(balance);
    }

    public void saveAccountChange(String gamePlatformName, Long userId, BigDecimal amount, BigDecimal amountBefore, BigDecimal amountAfter, Integer type, String orderNo, AccountChangeEnum changeEnum, String remark, User user) {
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
        order.setGamePlatformName(gamePlatformName);
        orderService.save(order);
        log.info("order表记录保存成功，order={}", order.toString());

        //账变中心记录账变
        AccountChangeVo vo = new AccountChangeVo();
        vo.setUserId(userId);
        vo.setChangeEnum(changeEnum);
        vo.setAmount(amount);
        vo.setAmountBefore(amountBefore);
        vo.setAmountAfter(amountAfter);
        asyncService.executeAsync(vo);
    }


    public ResponseEntity checkPlatformStatus(String vendorCode) {
        if (ObjectUtils.isEmpty(vendorCode)) {
            return ResponseUtil.custom("平台不存在");
        }
        PlatformGame platformGame = platformGameService.findByGamePlatformName(vendorCode);
        if (platformGame == null) {
            return ResponseUtil.customBefore(vendorCode, "平台不存在");
        }
        if (platformGame.getGameStatus() == 0) {
            return ResponseUtil.customBefore(vendorCode, "平台维护中");
        }
        if (platformGame.getGameStatus() == 2) {
            return ResponseUtil.customBefore(vendorCode, "平台已关闭");
        }
        return ResponseUtil.success();
    }

    public ResponseEntity checkGameStatus(String vendorCode, String gameCode) {
        if (ObjectUtils.isEmpty(vendorCode)) {
            return ResponseUtil.custom("平台不存在");
        }
        if (ObjectUtils.isEmpty(gameCode)) {
            return ResponseUtil.custom("游戏不存在");
        }
        AdGame adGame = adGamesService.findByGamePlatformNameAndGameCode(vendorCode, gameCode);
        if (adGame == null) {
            return ResponseUtil.custom("游戏不存在");
        }
        if (adGame.getGamesStatus() == 0) {
            return ResponseUtil.custom("游戏维护中");
        }
        if (adGame.getGamesStatus() == 2) {
            return ResponseUtil.custom("游戏已关闭");
        }
        return ResponseUtil.success();
    }

    public ResponseEntity checkPlatformAndGameStatus(String vendorCode, String gameCode) {
        ResponseEntity presponse = checkPlatformStatus(vendorCode);
        if (presponse.getCode() != ResponseCode.SUCCESS.getCode()) {
            return presponse;
        }
        ResponseEntity gresponse = checkGameStatus(vendorCode, gameCode);
        return gresponse;
    }
}
