package com.qianyi.casinocore.business;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Lists;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.ExpirationTimeUtil;
import com.qianyi.casinocore.util.RedisKeyUtil;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.liveae.api.PublicAeApi;
import com.qianyi.livedg.api.DgApi;
import com.qianyi.livegoldenf.api.PublicGoldenFApi;
import com.qianyi.livegoldenf.constants.WalletCodeEnum;
import com.qianyi.liveob.api.PublicObdjApi;
import com.qianyi.liveob.api.PublicObtyApi;
import com.qianyi.liveob.api.PublicObzrApi;
import com.qianyi.livewm.api.PublicWMApi;
import com.qianyi.lottery.api.LotteryDmcApi;
import com.qianyi.lottery.api.PublicLotteryApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

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
    private PublicObdjApi obdjApi;
    @Autowired
    private PublicObtyApi obtyApi;
    @Autowired
    private PublicObzrApi obzrApi;
    @Autowired
    private PublicAeApi aeApi;
    @Autowired
    private PublicLotteryApi lotteryApi;
    @Autowired
    private PlatformGameService platformGameService;
    @Autowired
    private AdGamesService adGamesService;
    @Autowired
    private ErrorOrderService errorOrderService;
    @Autowired
    @Qualifier("accountChangeJob")
    private AsyncService asyncService;
    @Autowired
    @Qualifier("asyncExecutor")
    private Executor executor;
    @Value("${project.ipWhite:null}")
    private String ipWhite;

    @Autowired
    private LotteryDmcApi lottoApi;
    @Autowired
    private RedisKeyUtil redisKeyUtil;
    @Autowired
    private DgApi dgApi;

    public ResponseEntity oneKeyRecoverGoldenF(Long userId, String vendorCode) {
        // 适配PG/CQ9
        if (ObjectUtils.isEmpty(vendorCode)) {
            vendorCode = Constants.PLATFORM_PG_CQ9;
        }
        log.info("开始回收{}余额，userId={}", vendorCode, userId);
        if (ObjectUtils.isEmpty(userId)) {
            return ResponseUtil.parameterNotNull();
        }
        if (Constants.PLATFORM_PG_CQ9.equals(vendorCode)) {
            ResponseEntity pgEnable = checkPlatformStatus(Constants.PLATFORM_PG);
            ResponseEntity cq9Enable = checkPlatformStatus(Constants.PLATFORM_CQ9);
            if (pgEnable.getCode() != ResponseCode.SUCCESS.getCode()
                && cq9Enable.getCode() != ResponseCode.SUCCESS.getCode()) {
                log.info("后台开启PG/CQ9维护，禁止回收，pgResponse={},cq9Response={}", pgEnable, cq9Enable);
                return pgEnable;
            }
        }
        if (Constants.PLATFORM_SABASPORT.equals(vendorCode)) {
            ResponseEntity sabaEnable = checkPlatformStatus(Constants.PLATFORM_SABASPORT);
            if (sabaEnable.getCode() != ResponseCode.SUCCESS.getCode()) {
                log.info("后台开启SABASPORT维护，禁止回收，sabaResponse={}", sabaEnable);
                return sabaEnable;
            }
        }
        UserThird third = userThirdService.findByUserId(userId);
        if (third == null || ObjectUtils.isEmpty(third.getGoldenfAccount())) {
            return ResponseUtil.custom(vendorCode + "余额为0");
        }
        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
        try {
            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
            ResponseEntity<BigDecimal> responseEntity =
                getBalanceGoldenF(third.getGoldenfAccount(), userId, vendorCode);
            if (responseEntity.getCode() != ResponseCode.SUCCESS.getCode()) {
                return responseEntity;
            }
            BigDecimal balance = responseEntity.getData();
            if (balance.compareTo(BigDecimal.ONE) == -1) {
                log.info("userId:{},balance={},{}金额小于1，不可回收", userId, balance, vendorCode);
                return ResponseUtil.custom(vendorCode + "余额小于1,不可回收");
            }

            String platformCode = Constants.PLATFORM_SABASPORT;
            // 适配PG/CQ9
            if (ObjectUtils.isEmpty(vendorCode)) {
                platformCode = Constants.PLATFORM_PG_CQ9;
            }
            //重置缓存时间
            ExpirationTimeUtil.resetExpirationTime(platformCode,userId.toString());


            String orderNo = orderService.getOrderNo();
            String goldenfAccount = third.getGoldenfAccount();
            // 调用提值接口扣减余额 存在精度问题，只回收整数部分
            BigDecimal recoverMoney = balance.setScale(0, BigDecimal.ROUND_DOWN);
            String walletCode = WalletCodeEnum.getWalletCodeByVendorCode(vendorCode);
            PublicGoldenFApi.ResponseEntity transferOut =
                goldenFApi.transferOut(goldenfAccount, recoverMoney.doubleValue(), orderNo, walletCode);
            AccountChangeEnum changeEnum = AccountChangeEnum.PG_CQ9_OUT;
            String platform = Constants.PLATFORM_PG_CQ9;
            String remark = "自动转出PG/CQ9";
            if (Constants.PLATFORM_SABASPORT.equals(vendorCode)) {
                changeEnum = AccountChangeEnum.SABASPORT_OUT;
                platform = Constants.PLATFORM_SABASPORT;
                remark = "自动转出SABASPORT";
            }
            if (transferOut == null) {
                User user = userService.findById(userId);
                errorOrderService.syncGoldenFSaveErrorOrder(goldenfAccount, user.getId(), user.getAccount(), orderNo,
                    recoverMoney, changeEnum, platform, walletCode);
                log.error("userId:{},money={},一键回收当前登录用户{}余额失败", userId, recoverMoney, vendorCode);
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
            if (!ObjectUtils.isEmpty(transferOut.getErrorCode())) {
                log.error("{}余额回收失败,userId:{},money={},errorCode={},errorMsg={}", vendorCode, userId, recoverMoney,
                    transferOut.getErrorCode(), transferOut.getErrorMessage());
                return ResponseUtil.custom("回收失败,请联系客服");
            }
            // 三方强烈建议提值/充值后使用 5.9 获取单个玩家的转账记录 进一步确认交易是否成功，避免造成金额损失
            long time = System.currentTimeMillis();
            PublicGoldenFApi.ResponseEntity playerTransactionRecord =
                goldenFApi.getPlayerTransactionRecord(goldenfAccount, time, time, walletCode, orderNo, null);
            if (playerTransactionRecord == null) {
                User user = userService.findById(userId);
                errorOrderService.syncGoldenFSaveErrorOrder(goldenfAccount, user.getId(), user.getAccount(), orderNo,
                    recoverMoney, changeEnum, platform, walletCode);
                log.error("userId:{},money={},{}一键回收余额查询转账记录失败,远程请求异常", userId, recoverMoney, vendorCode);
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
            if (!ObjectUtils.isEmpty(playerTransactionRecord.getErrorCode())) {
                log.error("{}一键回收余额查询转账记录失败,userId:{},errorCode={},errorMsg={}", vendorCode, userId,
                    playerTransactionRecord.getErrorCode(), playerTransactionRecord.getErrorMessage());
                return ResponseUtil.custom("回收失败,请联系客服");
            }
            JSONObject jsonData = JSONObject.parseObject(playerTransactionRecord.getData());
            JSONArray translogs = jsonData.getJSONArray("translogs");
            if (translogs.size() == 0) {
                return ResponseUtil.custom("回收失败,请联系客服");
            }
            // 把额度加回本地
            UserMoney userMoney = userMoneyService.findByUserId(userId);
            if (userMoney == null) {
                userMoney = new UserMoney();
                userMoney.setUserId(userId);
                userMoneyService.save(userMoney);
            }
            userMoneyService.addMoney(userId, recoverMoney);
            // 记录账变
            User user = userService.findById(userId);
            saveAccountChange(platform, userId, recoverMoney, userMoney.getMoney(),
                recoverMoney.add(userMoney.getMoney()), 1, orderNo, changeEnum, remark, user);
            log.info("{}余额回收成功，userId={}", vendorCode, userId);
            return ResponseUtil.success();
        } catch (Exception e) {
            log.error("GF下分出现异常userId{} {}", userId, e.getMessage());
            return ResponseUtil.custom("服务器异常,请重新操作");
        } finally {
            // 释放锁
            RedisKeyUtil.unlock(userMoneyLock);
        }
    }

    public ResponseEntity oneKeyRecoverWm(Long userId) {
        ResponseEntity response = checkPlatformStatus(Constants.PLATFORM_WM_BIG);
        if (response.getCode() != ResponseCode.SUCCESS.getCode()) {
            log.info("后台开启WM维护，禁止回收，response={}", response);
            return response;
        }
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
        // 先退出游戏
        Boolean aBoolean = wmApi.logoutGame(account, lang);
        if (!aBoolean) {
            log.error("userId:{},account={},WM退出游戏失败", userId, user.getAccount());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
        try {
            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
            // 查询用户在wm的余额
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

            //重置缓存时间
            ExpirationTimeUtil.resetExpirationTime(Constants.PLATFORM_WM_BIG,userId.toString());

            // 调用加扣点接口扣减wm余额 存在精度问题，只回收整数部分
            BigDecimal recoverMoney = balance.negate().setScale(0, BigDecimal.ROUND_DOWN);
            String orderNo = orderService.getOrderNo();
            PublicWMApi.ResponseEntity entity = wmApi.changeBalance(account, recoverMoney, orderNo, lang);
            if (entity == null) {
                log.error("WM加扣点失败,远程请求异常,userId:{},account={},money={}", userId, user.getAccount(), recoverMoney);
                // 异步记录错误订单并重试补偿
                errorOrderService.syncSaveErrorOrder(third.getAccount(), user.getId(), user.getAccount(), orderNo,
                    recoverMoney, AccountChangeEnum.RECOVERY, Constants.PLATFORM_WM_BIG);
                return ResponseUtil.custom("回收失败,请联系客服");
            }
            if (entity.getErrorCode() != 0) {
                log.error("WM加扣点失败，userId:{},account={},money={},errorCode={},errorMsg={}", userId, user.getAccount(),
                    recoverMoney, entity.getErrorCode(), entity.getErrorMessage());
                return ResponseUtil.custom("回收失败,请联系客服");
            }
            balance = recoverMoney.abs();
            // 把额度加回本地
            UserMoney userMoney = userMoneyService.findByUserId(userId);
            if (userMoney == null) {
                userMoney = new UserMoney();
                userMoney.setUserId(userId);
                userMoneyService.save(userMoney);
            }
            userMoneyService.addMoney(userId, balance);
            saveAccountChange(Constants.PLATFORM_WM_BIG, userId, balance, userMoney.getMoney(),
                balance.add(userMoney.getMoney()), 1, orderNo, AccountChangeEnum.RECOVERY, "自动转出WM", user);
            log.info("wm余额回收成功，userId={},account={}", userId, user.getAccount());
            return ResponseUtil.success();
        } catch (Exception e) {
            log.error("WM回收余额出现异常userId{} {}", userId, e.getMessage());
            return ResponseUtil.custom("服务器异常,请重新操作");
        } finally {
            // 释放锁
            RedisKeyUtil.unlock(userMoneyLock);
        }
    }

    public ResponseEntity oneKeyRecoverObdj(Long userId) {
        ResponseEntity obdjEnable = checkPlatformAndGameStatus(Constants.PLATFORM_OB, Constants.PLATFORM_OBDJ);
        if (obdjEnable.getCode() != ResponseCode.SUCCESS.getCode()) {
            log.info("后台开启OBDJ维护，禁止回收，response={}", obdjEnable);
            return obdjEnable;
        }
        log.info("开始回收OB电竞余额，userId={}", userId);
        if (userId == null) {
            return ResponseUtil.parameterNotNull();
        }
        UserThird third = userThirdService.findByUserId(userId);
        if (third == null || ObjectUtils.isEmpty(third.getObdjAccount())) {
            return ResponseUtil.custom("OB电竞余额为0");
        }
        User user = userService.findById(userId);
        String account = third.getObdjAccount();

        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
        try {
            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
            PublicObdjApi.ResponseEntity obApiBalance = obdjApi.getBalance(account);
            if (obApiBalance == null) {
                log.error("userId:{},account={},获取用户OB电竞余额失败,远程请求异常", userId, user.getAccount());
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
            if (PublicObdjApi.STATUS_FALSE.equals(obApiBalance.getStatus())) {
                log.error("userId:{},account={},获取用户OB电竞余额失败，msg={}", userId, user.getAccount(),
                    obApiBalance.getData());
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
            if (ObjectUtils.isEmpty(obApiBalance.getData())) {
                log.error("userId:{},account={},获取用户OB电竞余额为null,msg={}", userId, user.getAccount(),
                    obApiBalance.getData());
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
            BigDecimal balance = new BigDecimal(obApiBalance.getData());
            if (balance.compareTo(BigDecimal.ONE) == -1) {
                log.info("userId:{},account={},balance={},OB电竞金额小于1，不可回收", userId, user.getAccount(), balance);
                return ResponseUtil.custom("OB电竞余额小于1,不可回收");
            }

            //重置缓存时间
            ExpirationTimeUtil.resetExpirationTime(Constants.PLATFORM_OBDJ,userId.toString());

            // 调用加扣点接口扣减OB电竞余额 存在精度问题，只回收整数部分
            balance = balance.setScale(0, BigDecimal.ROUND_DOWN);
            String orderNo = orderService.getObdjOrderNo();
            PublicObdjApi.ResponseEntity transfer = obdjApi.transfer(account, 2, balance, orderNo);
            if (transfer == null) {
                log.error("OB电竞扣点失败,远程请求异常,userId:{},account={},money={}", userId, user.getAccount(), balance);
                // 异步记录错误订单
                errorOrderService.syncSaveErrorOrder(third.getObdjAccount(), user.getId(), user.getAccount(), orderNo,
                    balance, AccountChangeEnum.OBDJ_OUT, Constants.PLATFORM_OBDJ);
                return ResponseUtil.custom("回收失败,请联系客服");
            }
            if (PublicObdjApi.STATUS_FALSE.equals(transfer.getStatus())) {
                log.error("OB电竞扣点失败,userId:{},account={},money={},msg={}", userId, user.getAccount(), balance,
                    transfer.getData());
                return ResponseUtil.custom("回收失败,请联系客服");
            }
            // 把额度加回本地
            UserMoney userMoney = userMoneyService.findByUserId(userId);
            if (userMoney == null) {
                userMoney = new UserMoney();
                userMoney.setUserId(userId);
                userMoneyService.save(userMoney);
            }
            userMoneyService.addMoney(userId, balance);
            log.info("OB电竞余额,userMoney加回成功，userId={},balance={}", userId, balance);
            saveAccountChange(Constants.PLATFORM_OBDJ, userId, balance, userMoney.getMoney(),
                balance.add(userMoney.getMoney()), 1, orderNo, AccountChangeEnum.OBDJ_OUT, "自动转出OB电竞", user);
            log.info("OB电竞余额回收成功，userId={},account={},money={}", userId, user.getAccount(), balance);
            return ResponseUtil.success();
        } catch (Exception e) {
            log.error("OBDJ下分出现异常userId{} {}", userId, e.getMessage());
            return ResponseUtil.custom("服务器异常,请重新操作");
        } finally {
            // 释放锁
            RedisKeyUtil.unlock(userMoneyLock);
        }
    }

    public ResponseEntity oneKeyRecoverObty(Long userId) {
        ResponseEntity obtyEnable = checkPlatformAndGameStatus(Constants.PLATFORM_OB, Constants.PLATFORM_OBTY);
        if (obtyEnable.getCode() != ResponseCode.SUCCESS.getCode()) {
            log.info("后台开启OBTY维护，禁止回收，response={}", obtyEnable);
            return obtyEnable;
        }
        log.info("开始回收OB体育余额，userId={}", userId);
        if (userId == null) {
            return ResponseUtil.parameterNotNull();
        }
        UserThird third = userThirdService.findByUserId(userId);
        if (third == null || ObjectUtils.isEmpty(third.getObtyAccount())) {
            return ResponseUtil.custom("OB体育余额为0");
        }
        User user = userService.findById(userId);
        String account = third.getObtyAccount();
        // 先退出
        PublicObtyApi.ResponseEntity logoutResult = obtyApi.kickOutUser(account);
        if (logoutResult == null) {
            log.error("userId:{},account={},OB体育退出失败,远程请求异常", userId, user.getAccount());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (!logoutResult.getStatus()) {
            log.error("userId:{},account={},OB体育退出失败,result={}", userId, logoutResult.toString());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }

        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
        try {
            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
            ResponseEntity<BigDecimal> balanceObty = getBalanceObty(account, userId);
            if (balanceObty.getCode() != ResponseCode.SUCCESS.getCode()) {
                return balanceObty;
            }
            BigDecimal balance = balanceObty.getData();
            if (balance.compareTo(BigDecimal.ONE) == -1) {
                log.info("userId:{},account={},balance={},OB体育金额小于1，不可回收", userId, user.getAccount(), balance);
                return ResponseUtil.custom("OB体育余额小于1,不可回收");
            }

            //重置缓存
            ExpirationTimeUtil.resetExpirationTime(Constants.PLATFORM_OBTY,userId.toString());

            // 调用加扣点接口扣减OB电竞余额 存在精度问题，只回收整数部分
            balance = balance.setScale(0, BigDecimal.ROUND_DOWN);
            String orderNo = orderService.getObtyOrderNo();
            PublicObtyApi.ResponseEntity transfer = obtyApi.transfer(account, 2, balance, orderNo);
            if (transfer == null) {
                log.error("OB体育扣点失败,远程请求异常,userId:{},account={},money={}", userId, user.getAccount(), balance);
                // 异步记录错误订单
                errorOrderService.syncSaveErrorOrder(third.getObtyAccount(), user.getId(), user.getAccount(), orderNo,
                    balance, AccountChangeEnum.OBTY_OUT, Constants.PLATFORM_OBTY);
                return ResponseUtil.custom("回收失败,请联系客服");
            }
            if (!transfer.getStatus()) {
                log.error("OB体育扣点失败,userId:{},account={},money={},msg={}", userId, user.getAccount(), balance,
                    transfer.toString());
                return ResponseUtil.custom("回收失败,请联系客服");
            }
            // 把额度加回本地
            UserMoney userMoney = userMoneyService.findByUserId(userId);
            if (userMoney == null) {
                userMoney = new UserMoney();
                userMoney.setUserId(userId);
                userMoneyService.save(userMoney);
            }
            userMoneyService.addMoney(userId, balance);
            log.info("OB体育余额,userMoney加回成功，userId={},balance={}", userId, balance);
            saveAccountChange(Constants.PLATFORM_OBTY, userId, balance, userMoney.getMoney(),
                balance.add(userMoney.getMoney()), 1, orderNo, AccountChangeEnum.OBTY_OUT, "自动转出OB体育", user);
            log.info("OB体育余额回收成功，userId={},account={},money={}", userId, user.getAccount(), balance);
            return ResponseUtil.success();
        } catch (Exception e) {
            log.error("OB体育下分出现异常userId{} {}", userId, e.getMessage());
            return ResponseUtil.custom("服务器异常,请重新操作");
        } finally {
            // 释放锁
            RedisKeyUtil.unlock(userMoneyLock);
        }
    }


    public ResponseEntity oneKeyRecoverObzr(Long userId) {
        ResponseEntity obtyEnable = checkPlatformAndGameStatus(Constants.PLATFORM_OB, Constants.PLATFORM_OBZR);
        if (obtyEnable.getCode() != ResponseCode.SUCCESS.getCode()) {
            log.info("后台开启OBZR维护，禁止回收，response={}", obtyEnable);
            return obtyEnable;
        }
        log.info("开始回收OB真人余额，userId={}", userId);
        if (userId == null) {
            return ResponseUtil.parameterNotNull();
        }
        UserThird third = userThirdService.findByUserId(userId);
        if (third == null || ObjectUtils.isEmpty(third.getObzrAccount())) {
            return ResponseUtil.custom("OB真人余额为0");
        }
        User user = userService.findById(userId);
        String account = third.getObzrAccount();
        // 先调用离桌
        boolean flag = obzrApi.foreLeaveTable(account);
        if (!flag) {
            log.error("userId:{},account={},OB真人退出失败,远程请求异常", userId, user.getAccount());
//            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
        try {
            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
            ResponseEntity<BigDecimal> balanceObzr = getBalanceObzr(account, userId);
            if (balanceObzr.getCode() != ResponseCode.SUCCESS.getCode()) {
                return balanceObzr;
            }
            BigDecimal balance = balanceObzr.getData();
            if (balance.compareTo(BigDecimal.ONE) == -1) {
                log.info("userId:{},account={},balance={},OB真人金额小于1，不可回收", userId, user.getAccount(), balance);
                return ResponseUtil.custom("OB真人余额小于1,不可回收");
            }

            //重置缓存时间
            ExpirationTimeUtil.resetExpirationTime(Constants.PLATFORM_OBZR,userId.toString());

            // 调用加扣点接口扣减OB电竞余额 存在精度问题，只回收整数部分
            balance = balance.setScale(0, BigDecimal.ROUND_DOWN);
            String orderNo = orderService.getObtyOrderNo();
            PublicObzrApi.ResponseEntity withdraw = obzrApi.withdraw(account,  balance, orderNo);
            if (withdraw == null) {
                log.error("OB真人扣点失败,远程请求异常,userId:{},account={},money={}", userId, user.getAccount(), balance);
                // 异步记录错误订单
                errorOrderService.syncSaveErrorOrder(third.getObtyAccount(), user.getId(), user.getAccount(), orderNo,
                        balance, AccountChangeEnum.OBZR_OUT, Constants.PLATFORM_OBZR);
                return ResponseUtil.custom("回收失败,请联系客服");
            }
            if (!withdraw.getCode().equals("200")) {
                log.error("OB真人扣点失败,userId:{},account={},money={},msg={}", userId, user.getAccount(), balance,
                        withdraw.toString());
                return ResponseUtil.custom("回收失败,请联系客服");
            }
            // 把额度加回本地
            UserMoney userMoney = userMoneyService.findByUserId(userId);
            if (userMoney == null) {
                userMoney = new UserMoney();
                userMoney.setUserId(userId);
                userMoneyService.save(userMoney);
            }
            userMoneyService.addMoney(userId, balance);
            log.info("OB真人余额,userMoney加回成功，userId={},balance={}", userId, balance);
            saveAccountChange(Constants.PLATFORM_OBZR, userId, balance, userMoney.getMoney(),
                    balance.add(userMoney.getMoney()), 1, orderNo, AccountChangeEnum.OBZR_OUT, "自动转出OB真人", user);
            log.info("OB真人余额回收成功，userId={},account={},money={}", userId, user.getAccount(), balance);
            return ResponseUtil.success();
        } catch (Exception e) {
            log.error("OB真人下分出现异常userId{} {}", userId, e.getMessage());
            return ResponseUtil.custom("服务器异常,请重新操作");
        } finally {
            // 释放锁
            RedisKeyUtil.unlock(userMoneyLock);
        }
    }


    public ResponseEntity<BigDecimal> getBalanceGoldenF(String account, Long userId, String vendorCode) {
        String walletCode = WalletCodeEnum.getWalletCodeByVendorCode(vendorCode);
        PublicGoldenFApi.ResponseEntity playerBalance = goldenFApi.getPlayerBalance(account, walletCode);
        if (playerBalance == null) {
            log.error("userId:{},查询GoldenF余额失败", userId);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (!ObjectUtils.isEmpty(playerBalance.getErrorCode())) {
            log.error("查询GoldenF余额失败,userId:{},account={},errorCode={},errorMsg={}", userId, account,
                playerBalance.getErrorCode(), playerBalance.getErrorMessage());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        JSONObject jsonData = JSONObject.parseObject(playerBalance.getData());
        BigDecimal balance = new BigDecimal(jsonData.getDouble("balance"));
        balance = new BigDecimal(balance.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        log.info("查询GF余额walletCode{}",walletCode);
        if (!ObjectUtils.isEmpty(walletCode)){
            ExpirationTimeUtil.resetTripartiteBalance(Constants.PLATFORM_SABASPORT,userId.toString(),balance);
        }else {
            ExpirationTimeUtil.resetTripartiteBalance(Constants.PLATFORM_PG_CQ9,userId.toString(),balance);
        }
        return ResponseUtil.success(balance);
    }

    public ResponseEntity oneKeyRecoverAe(Long userId) {
        ResponseEntity aeEnable = checkPlatformStatus(Constants.PLATFORM_AE);
        if (aeEnable.getCode() != ResponseCode.SUCCESS.getCode()) {
            log.info("后台开启AE维护，禁止回收，response={}", aeEnable);
            return aeEnable;
        }
        log.info("开始回收AE余额，userId={}", userId);
        if (userId == null) {
            return ResponseUtil.parameterNotNull();
        }
        UserThird third = userThirdService.findByUserId(userId);
        String account = third.getAeAccount();
        if (third == null || ObjectUtils.isEmpty(account)) {
            return ResponseUtil.custom("AE余额为0");
        }
        User user = userService.findById(userId);

        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
        try {
            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
            ResponseEntity<BigDecimal> aeBalanceResponse = getAeBalanceByAccount(account);
            if (aeBalanceResponse.getCode() != ResponseCode.SUCCESS.getCode()) {
                return aeBalanceResponse;
            }
            BigDecimal balance = aeBalanceResponse.getData();
            if (balance.compareTo(BigDecimal.ONE) == -1) {
                log.info("userId:{},account={},balance={},AE金额小于1，不可回收", userId, user.getAccount(), balance);
                return ResponseUtil.custom("AE余额小于1,不可回收");
            }
            // 调用加扣点接口扣减OB电竞余额 存在精度问题，只回收整数部分
            balance = balance.setScale(0, BigDecimal.ROUND_DOWN);
            String orderNo = orderService.getObdjOrderNo();
            JSONObject jsonObject = aeApi.withdraw(account, orderNo, 0, balance.toString());
            if (jsonObject == null) {
                log.error("AE扣点失败,远程请求异常,userId:{},account={},result={}", userId, user.getAccount(), jsonObject);
                // 异步记录错误订单
                errorOrderService.syncSaveErrorOrder(third.getAeAccount(), user.getId(), user.getAccount(), orderNo,
                    balance, AccountChangeEnum.AE_OUT, Constants.PLATFORM_AE);
                return ResponseUtil.custom("回收失败,请联系客服");
            }
            String status = jsonObject.getString("status");
            if (!PublicAeApi.SUCCESS_CODE.equals(status)) {
                log.error("AE扣点失败,userId:{},account={},money={},msg={}", userId, user.getAccount(), balance,
                    jsonObject);
                return ResponseUtil.custom("回收失败,请联系客服");
            }
            // 把额度加回本地
            UserMoney userMoney = userMoneyService.findByUserId(userId);
            userMoneyService.addMoney(userId, balance);
            log.info("AE余额,userMoney加回成功，userId={},balance={}", userId, balance);
            saveAccountChange(Constants.PLATFORM_AE, userId, balance, userMoney.getMoney(),
                balance.add(userMoney.getMoney()), 1, orderNo, AccountChangeEnum.AE_OUT, "自动转出AE", user);
            log.info("AE余额回收成功，userId={},account={},money={}", userId, user.getAccount(), balance);
            return ResponseUtil.success();
        } catch (Exception e) {
            log.error("AE下分出现异常userId{} {}", userId, e.getMessage());
            return ResponseUtil.custom("服务器异常,请重新操作");
        } finally {
            // 释放锁
            RedisKeyUtil.unlock(userMoneyLock);
        }
    }

    public ResponseEntity<BigDecimal> getBalanceObdj(String account, Long userId) {
        PublicObdjApi.ResponseEntity balanceResult = obdjApi.getBalance(account);
        if (balanceResult == null) {
            log.error("userId:{},查询OB电竞余额失败,远程请求异常", userId);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (PublicObdjApi.STATUS_FALSE.equals(balanceResult.getStatus())) {
            log.error("userId:{},查询OB电竞余额失败,msg:{}", userId, balanceResult.getData());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        BigDecimal balance = new BigDecimal(balanceResult.getData());
        ExpirationTimeUtil.resetTripartiteBalance(Constants.PLATFORM_OBDJ,userId.toString(),balance);
        return ResponseUtil.success(balance);
    }

    public ResponseEntity<BigDecimal> getBalanceObty(String account, Long userId) {
        PublicObtyApi.ResponseEntity balanceResult = obtyApi.checkBalance(account);
        if (balanceResult == null) {
            log.error("userId:{},查询OB体育余额失败,远程请求异常", userId);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (!balanceResult.getStatus()) {
            log.error("userId:{},查询OB体育余额失败,balanceResult={}", userId, balanceResult.toString());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        JSONObject jsonObject = JSONObject.parseObject(balanceResult.getData());
        BigDecimal balance = new BigDecimal(jsonObject.getString("balance"));
        ExpirationTimeUtil.resetTripartiteBalance(Constants.PLATFORM_OBTY,userId.toString(),balance);
        return ResponseUtil.success(balance);
    }

    public ResponseEntity<BigDecimal> getBalanceObzr(String account, Long userId) {
        PublicObzrApi.ResponseEntity balanceResult = obzrApi.checkBalance(account);
        if (balanceResult == null) {
            log.error("userId:{},查询OB体育余额失败,远程请求异常", userId);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (!balanceResult.getCode().equals("200")) {
            log.error("userId:{},查询OB体育余额失败,balanceResult={}", userId, balanceResult.toString());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        JSONObject jsonObject = JSONObject.parseObject(balanceResult.getData());
        BigDecimal balance = new BigDecimal(jsonObject.getString("balance"));
        ExpirationTimeUtil.resetTripartiteBalance(Constants.PLATFORM_OBZR,userId.toString(),balance);
        return ResponseUtil.success(balance);
    }

    /**
     * 进游戏时一键回收其他游戏的金额(不包含当前游戏)
     * 
     * @param userId
     * @param platform
     * @return
     */
    public ResponseEntity oneKeyRecoverOtherGame(Long userId, String platform) {
        log.info("进入游戏统一回收余额开始==============================================>");
        long startTime = System.currentTimeMillis();
        List<CompletableFuture> completableFutures = new ArrayList<>();
        if (!Constants.PLATFORM_WM_BIG.equals(platform)) {
            CompletableFuture<Void> oneKeyRecoverWm = CompletableFuture.runAsync(() -> {
                oneKeyRecoverWm(userId);
            }, executor);
            completableFutures.add(oneKeyRecoverWm);
        }
        if (!Constants.PLATFORM_PG_CQ9.equals(platform)) {
            CompletableFuture<Void> oneKeyRecoverGoldenF = CompletableFuture.runAsync(() -> {
                oneKeyRecoverGoldenF(userId, Constants.PLATFORM_PG_CQ9);
            }, executor);
            completableFutures.add(oneKeyRecoverGoldenF);
        }
        if (!Constants.PLATFORM_SABASPORT.equals(platform)) {
            CompletableFuture<Void> oneKeyRecoverGoldenF = CompletableFuture.runAsync(() -> {
                oneKeyRecoverGoldenF(userId, Constants.PLATFORM_SABASPORT);
            }, executor);
            completableFutures.add(oneKeyRecoverGoldenF);
        }
        if (!Constants.PLATFORM_OBDJ.equals(platform)) {
            CompletableFuture<Void> oneKeyRecoverObdj = CompletableFuture.runAsync(() -> {
                oneKeyRecoverObdj(userId);
            }, executor);
            completableFutures.add(oneKeyRecoverObdj);
        }
        if (!Constants.PLATFORM_OBTY.equals(platform)) {
            CompletableFuture<Void> oneKeyRecoverObty = CompletableFuture.runAsync(() -> {
                oneKeyRecoverObty(userId);
            }, executor);
            completableFutures.add(oneKeyRecoverObty);
        }
        if (!Constants.PLATFORM_OBZR.equals(platform)) {
            CompletableFuture<Void> oneKeyRecoverObzr = CompletableFuture.runAsync(() -> {
                oneKeyRecoverObzr(userId);
            }, executor);
            completableFutures.add(oneKeyRecoverObzr);
        }
        if (!Constants.PLATFORM_AE.equals(platform)) {
            CompletableFuture<Void> oneKeyRecoverAe = CompletableFuture.runAsync(() -> {
                oneKeyRecoverAe(userId);
            }, executor);
            completableFutures.add(oneKeyRecoverAe);
        }
        if (!Constants.PLATFORM_DMC.equals(platform)) {
            CompletableFuture<Void> oneKeyRecoverDmc = CompletableFuture.runAsync(() -> {
                oneKeyRecoverDMC(userId);
            }, executor);
            completableFutures.add(oneKeyRecoverDmc);
        }
        if (!Constants.PLATFORM_VNC.equals(platform)) {
            CompletableFuture<Void> oneKeyRecoverAe = CompletableFuture.runAsync(() -> {
                oneKeyRecoverVNC(userId);
            }, executor);
            completableFutures.add(oneKeyRecoverAe);
        }
        if (!Constants.PLATFORM_DG.equals(platform)) {
            CompletableFuture<Void> oneKeyRecoverAe = CompletableFuture.runAsync(() -> {
                oneKeyRecoverDG(userId);
            }, executor);
            completableFutures.add(oneKeyRecoverAe);
        }
        // 等待所有子线程计算完成
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()])).join();
        log.info("进入游戏统一回收余额结束耗时{}==============================================>",System.currentTimeMillis()-startTime);
        return ResponseUtil.success();
    }
    
    public ResponseEntity oneKeyRecoverDG(Long userId) {
        ResponseEntity aeEnable = checkPlatformStatus(Constants.PLATFORM_DG);
        if (aeEnable.getCode() != ResponseCode.SUCCESS.getCode()) {
            log.info("后台开启DG维护，禁止回收，response={}", aeEnable);
            return aeEnable;
        }
        log.info("开始回收DG余额，userId={}", userId);
        if (userId == null) {
            return ResponseUtil.parameterNotNull();
        }
        UserThird third = userThirdService.findByUserId(userId);
        String account = third.getDgAccount();
        if (third == null || ObjectUtils.isEmpty(account)) {
            return ResponseUtil.custom("DG账号不存在");
        }
        User user = userService.findById(userId);
        ResponseEntity<BigDecimal> aeBalanceResponse = getDgBalanceByAccount(account);
        if (aeBalanceResponse.getCode() != ResponseCode.SUCCESS.getCode()) {
            return aeBalanceResponse;
        }
        BigDecimal balance = aeBalanceResponse.getData();
        if (balance.compareTo(BigDecimal.ONE) == -1) {
            log.info("userId:{},account={},balance={},DG金额小于1，不可回收", userId, user.getAccount(), balance);
            return ResponseUtil.custom("DG余额小于1,不可回收");
        }

        //重置缓存时间
        ExpirationTimeUtil.resetExpirationTime(Constants.PLATFORM_DG,userId.toString());

        String orderNo = orderService.getDGOrderNo();
        //调用加扣点接口扣减DG电竞余额  存在精度问题，只回收整数部分
        balance = balance.setScale(0, BigDecimal.ROUND_DOWN);
        JSONObject apiResponseData = null;
        try {
            apiResponseData = dgApi.transterWallet(account, balance.negate(),orderNo);//正数存款负数取款
        } catch (Exception e) {
            //异步记录错误订单
            errorOrderService.syncSaveDgErrorOrder(third.getDgAccount(), user.getId(), user.getAccount(), orderNo, balance, AccountChangeEnum.DG_OUT, Constants.PLATFORM_DG);
            return ResponseUtil.custom("回收失败,请联系客服");
        }

        log.info("DG下分返回结果：【{}】, 用户id：【{}】", apiResponseData, userId);
        if (null != apiResponseData && "0".equals(apiResponseData.getString("codeId"))){
            RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
            try {
                userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
                // 把额度加回本地
                UserMoney userMoney = userMoneyService.findByUserId(userId);
                BigDecimal money = userMoney.getMoney();
                userMoneyService.addMoney(userId, balance);
                log.info("DG余额,userMoney加回成功，userId={},balance={}", userId, balance);
                saveAccountChange(Constants.PLATFORM_DG, userId, balance, money, balance.add(money), 1, orderNo,
                    AccountChangeEnum.DG_OUT, "自动转出DG", user);
                log.info("DG余额回收成功，userId={},account={},money={}", userId, user.getAccount(), balance);
                return ResponseUtil.success();
            } catch (Exception e) {
                log.error("DG余额下分出现异常userId{} {}", userId, e.getMessage());
                return ResponseUtil.custom("服务器异常,请重新操作");
            } finally {
                // 释放锁
                RedisKeyUtil.unlock(userMoneyLock);
            }
        }else{
            log.error("DG扣点失败,远程请求异常,userId:{},account={},result={}", userId, user.getAccount(), apiResponseData);
            return dgApi.errorCode(apiResponseData.getIntValue("codeId"), apiResponseData.getString("random"));
        }

    }

    /**
     * 查询DG余额
     *
     * @param account
     * @return
     */
    public ResponseEntity<BigDecimal> getDgBalanceByAccount(String account) {
        JSONObject apiResponseData = null;
        try {
            apiResponseData = dgApi.fetchWalletBalance(account);
        } catch (Exception e) {
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (null != apiResponseData && "0".equals(apiResponseData.getString("codeId"))){
            JSONObject member = apiResponseData.getJSONObject("member");
            BigDecimal amount = member.getBigDecimal("balance");
            return ResponseUtil.success(amount);
        }else{
            return dgApi.errorCode(apiResponseData.getIntValue("codeId"), apiResponseData.getString("random"));
        }

    }

    public ResponseEntity oneKeyRecoverVNC(Long userId) {
        ResponseEntity aeEnable = checkPlatformStatus(Constants.PLATFORM_VNC);
        if (aeEnable.getCode() != ResponseCode.SUCCESS.getCode()) {
            log.info("后台开启越南彩维护，禁止回收，response={}", aeEnable);
            return aeEnable;
        }
        log.info("开始回收越南彩余额，userId={}", userId);
        if (userId == null) {
            return ResponseUtil.parameterNotNull();
        }
        UserThird third = userThirdService.findByUserId(userId);
        String account = third.getVncAccount();
        if (third == null || ObjectUtils.isEmpty(account)) {
            return ResponseUtil.custom("越南彩余额为0");
        }
        User user = userService.findById(userId);

        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
        try {
            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
            ResponseEntity<BigDecimal> aeBalanceResponse = getVncBalanceByAccount(account);
            if (aeBalanceResponse.getCode() != ResponseCode.SUCCESS.getCode()) {
                return aeBalanceResponse;
            }
            BigDecimal balance = aeBalanceResponse.getData();
            if (balance.compareTo(BigDecimal.ONE) == -1) {
                log.info("userId:{},account={},balance={},越南彩金额小于1，不可回收", userId, user.getAccount(), balance);
                return ResponseUtil.custom("VNC余额小于1,不可回收");
            }
            // 调用加扣点接口扣减VNC电竞余额 存在精度问题，只回收整数部分
            balance = balance.setScale(0, BigDecimal.ROUND_DOWN);
            String orderNo = orderService.getVNCOrderNo();
            PublicLotteryApi.ResponseEntity responseEntity = lotteryApi.changeBalance(account, 2, balance, orderNo);
            if (responseEntity == null || StringUtils.isBlank(responseEntity.getData())) {
                log.error("越南彩扣点失败,远程请求异常,userId:{},account={},result={}", userId, user.getAccount(), responseEntity);
                // 异步记录错误订单
                errorOrderService.syncSaveVNCErrorOrder(third.getVncAccount(), user.getId(), user.getAccount(), orderNo,
                    balance, AccountChangeEnum.VNC_OUT, Constants.PLATFORM_VNC);
                return ResponseUtil.custom("回收失败,请联系客服");
            }
            String status = responseEntity.getErrorCode();
            if (!"0".equals(status)) {
                log.error("VNC扣点失败,userId:{},account={},money={},msg={}", userId, user.getAccount(), balance,
                    responseEntity);
                return ResponseUtil.custom("回收失败,请联系客服");
            }
            // 把额度加回本地
            UserMoney userMoney = userMoneyService.findByUserId(userId);
            BigDecimal money = userMoney.getMoney();
            userMoneyService.addMoney(userId, balance);
            log.info("越南彩余额,userMoney加回成功，userId={},balance={}", userId, balance);
            saveAccountChange(Constants.PLATFORM_VNC, userId, balance, money, balance.add(money), 1, orderNo,
                AccountChangeEnum.VNC_OUT, "自动转出VNC", user);
            log.info("VNC余额回收成功，userId={},account={},money={}", userId, user.getAccount(), balance);
            return ResponseUtil.success();
        } catch (Exception e) {
            log.error("越南彩余额下分出现异常userId{} {}", userId, e.getMessage());
            return ResponseUtil.custom("服务器异常,请重新操作");
        } finally {
            // 释放锁
            RedisKeyUtil.unlock(userMoneyLock);
        }
    }

    public ResponseEntity oneKeyRecoverDMC(Long userId) {
        ResponseEntity response = checkPlatformStatus(Constants.PLATFORM_DMC);
        if (response.getCode() != ResponseCode.SUCCESS.getCode()) {
            log.info("后台开启DMC维护，禁止回收，response={}", response);
            return response;
        }
        log.info("开始回收DMC余额，userId={}", userId);
        if (userId == null) {
            return ResponseUtil.parameterNotNull();
        }
        UserThird third = userThirdService.findByUserId(userId);
        if (third == null || ObjectUtils.isEmpty(third.getAccount())) {
            return ResponseUtil.custom("DMC余额为0");
        }
        User user = userService.findById(userId);

        String account = third.getDmcAccount();
        //大马彩没有退出游戏功能
        String token = lottoApi.fetchToken();

        //查询用户在wm的余额
        BigDecimal balance = BigDecimal.ZERO;
        try {
            List<String> idList = Lists.newArrayList(user.getId() + "");
            balance = lottoApi.fetchWalletBalance(idList, token);
            if (balance == null) {
                log.error("userId:{},account={},获取用户DMC余额为null", userId, user.getAccount());
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
        } catch (Exception e) {
            log.error("userIdList:{},获取用户DMC余额失败{}", userId, user.getAccount(), e.getMessage());
            e.printStackTrace();
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (balance.compareTo(BigDecimal.ONE) == -1) {
            log.info("userId:{},account={},balance={},DMC金额小于1，不可回收", userId, user.getAccount(), balance);
            return ResponseUtil.custom("DMC余额小于1,不可回收");
        }
        //调用加扣点接口扣减DMC余额  存在精度问题，只回收整数部分
        BigDecimal recoverMoney = balance.setScale(0, BigDecimal.ROUND_DOWN);
        //订单号三方返回
        String orderNo = orderService.getOrderNo();
        JSONObject jsonObject = lottoApi.transterWallet(user.getId() + "", account, recoverMoney, 2, token,orderNo);
        if (jsonObject == null) {
            log.error("DMC加扣点失败,远程请求异常,userId:{},account={},money={}", userId, user.getAccount(), recoverMoney);
            //异步记录错误订单并重试补偿
//            errorOrderService.syncSaveDMCErrorOrder(third.getAccount(), user.getId(), user.getAccount(), orderNo, recoverMoney, AccountChangeEnum.RECOVERY, Constants.PLATFORM_WM_BIG);
            return ResponseUtil.custom("回收失败,请联系客服");
        }
        if (!lottoApi.getResultCode(jsonObject)) {
            log.error("DMC加扣点失败，userId:{},account={},money={},result={}", userId, user.getAccount(), recoverMoney, jsonObject);
            return ResponseUtil.custom("回收失败,请联系客服");
        }

        //订单号是三方返回，所以
        orderNo = orderNo + "_" + lottoApi.getTransterId(jsonObject);
        balance = recoverMoney.abs();
        //把额度加回本地
        UserMoney userMoney = userMoneyService.findByUserId(userId);
        if (ObjectUtil.isEmpty(userMoney)) {
            userMoney = new UserMoney();
            userMoney.setUserId(userId);
            userMoneyService.save(userMoney);
        }
        userMoneyService.addMoney(userId, balance);
        saveAccountChange(Constants.PLATFORM_DMC, userId, balance, userMoney.getMoney(), balance.add(userMoney.getMoney()), 1, orderNo, AccountChangeEnum.DMC_OUT, "自动转出DMC", user);
        log.info("大马彩余额回收成功，userId={},account={}", userId, user.getAccount());
        return ResponseUtil.success();
    }

    /**
     * 查询越南彩余额
     *
     * @param account
     * @return
     */
    private ResponseEntity<BigDecimal> getVncBalanceByAccount(String account) {
        BigDecimal amount = lotteryApi.getBalance(account);

        return ResponseUtil.success(amount);
    }

    public void saveAccountChange(String gamePlatformName, Long userId, BigDecimal amount, BigDecimal amountBefore,
        BigDecimal amountAfter, Integer type, String orderNo, AccountChangeEnum changeEnum, String remark, User user) {
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

        // 账变中心记录账变
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
            return ResponseUtil.custom("平台不存在");
        }
        if (platformGame.getGameStatus() == 0) {
            return ResponseUtil.custom("平台维护中");
        }
        if (platformGame.getGameStatus() == 2) {
            return ResponseUtil.custom("平台已关闭");
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

    public void inSaveAccountChange(Long userId, BigDecimal amount, BigDecimal amountBefore, BigDecimal amountAfter,
        Integer type, String orderNo, String remark, String platform, AccountChangeEnum changeEnum) {
        User user = userService.findById(userId);
        Order order = new Order();
        order.setMoney(amount);
        order.setUserId(userId);
        order.setRemark(remark);
        order.setType(type);
        order.setState(Constants.order_wait);
        order.setNo(orderNo);
        order.setGamePlatformName(platform);
        order.setFirstProxy(user.getFirstProxy());
        order.setSecondProxy(user.getSecondProxy());
        order.setThirdProxy(user.getThirdProxy());
        orderService.save(order);

        // 账变中心记录账变
        AccountChangeVo vo = new AccountChangeVo();
        vo.setUserId(userId);
        vo.setChangeEnum(changeEnum);
        vo.setAmount(amount.negate());
        vo.setAmountBefore(amountBefore);
        vo.setAmountAfter(amountAfter);
        asyncService.executeAsync(vo);
    }

    public Boolean ipWhiteCheck() {
//        if (ObjectUtils.isEmpty(ipWhite)) {
//            return false;
//        }
//        HttpServletRequest request =
//            ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
//        String ip = IpUtil.getIp(request);
//        String[] ipWhiteArray = ipWhite.split(",");
//        for (String ipw : ipWhiteArray) {
//            if (!ObjectUtils.isEmpty(ipw) && ipw.trim().equals(ip)) {
//                return true;
//            }
//        }
//        return false;
        return true;
    }

    public ResponseEntity<BigDecimal> getAeBalanceByAccount(String aeAccount) {
        JSONObject balanceResult = aeApi.getBalance(aeAccount, 0, 0);
        if (balanceResult == null) {
            log.error("aeAccount:{},查询AE余额失败,远程请求异常", aeAccount);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        String status = balanceResult.getString("status");
        if (!PublicAeApi.SUCCESS_CODE.equals(status)) {
            log.error("aeAccount:{},查询AE余额失败,balanceResult={}", aeAccount, balanceResult);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        JSONArray results = balanceResult.getJSONArray("results");
        if (results.size() == 0) {
            return ResponseUtil.success(BigDecimal.ZERO);
        }
        JSONObject jsonObject = results.getJSONObject(0);
        BigDecimal balance = jsonObject.getBigDecimal("balance");
        return ResponseUtil.success(balance);
    }

    public ResponseEntity<BigDecimal> getAllAeBalance(String aeAccount) {
        Integer alluser = 0;// 查全部
        if (ObjectUtils.isEmpty(aeAccount)) {
            alluser = 1;
        }
        JSONObject balanceResult = aeApi.getBalance(aeAccount, alluser, 0);
        if (balanceResult == null) {
            log.error("aeAccount:{},查询AE余额失败,远程请求异常", aeAccount);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        String status = balanceResult.getString("status");
        if (!PublicAeApi.SUCCESS_CODE.equals(status)) {
            log.error("aeAccount:{},查询AE余额失败,balanceResult={}", aeAccount, balanceResult);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        JSONArray results = balanceResult.getJSONArray("results");
        int size = results.size();
        BigDecimal balance = BigDecimal.ZERO;
        for (int i = 0; i < size; i++) {
            JSONObject jsonObject = results.getJSONObject(i);
            BigDecimal balance1 = jsonObject.getBigDecimal("balance");
            balance = balance.add(balance1);
        }
        return ResponseUtil.success(balance);
    }

    public BigDecimal getDMCBalanceByAccount(String dmcAccount) {
        String token = lottoApi.fetchToken();
        List<String> userIdList = Lists.newArrayList(dmcAccount + "");
        BigDecimal balance = lottoApi.fetchWalletBalance(userIdList, token);
        return balance;
    }

    public ResponseEntity<BigDecimal> getAllDMCBalance() {
        String token = lottoApi.fetchToken();
        BigDecimal balance = lottoApi.fetchTotalBalance(token);
        return ResponseUtil.success(balance);
    }

    public ResponseEntity<BigDecimal> getVNCBalanceByAccount(String vncAccount) {
        BigDecimal balance = lotteryApi.getBalance(vncAccount);
        return ResponseUtil.success(balance);
    }

    public ResponseEntity getAllVNCBalance(String vncAccount) {

        // 查全部
        if (ObjectUtils.isEmpty(vncAccount)) {
            vncAccount = "not Account";
        }
        BigDecimal balance = lotteryApi.getBalance(vncAccount);
        if (balance == null) {
            log.error("aeAccount:{},查询VNC余额失败,远程请求异常", balance);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }

        return ResponseUtil.success(balance);
    }
}