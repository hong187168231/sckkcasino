package com.qianyi.casinoweb.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.util.DeviceUtil;
import com.qianyi.livegoldenf.api.PublicGoldenFApi;
import com.qianyi.livegoldenf.constants.LanguageEnum;
import com.qianyi.livewm.api.PublicWMApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.annotation.RequestLimit;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.IpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/golednf")
@Api(tags = "GoldenF游戏厅")
@Slf4j
public class GoldenFController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private UserThirdService userThirdService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private PlatformConfigService platformConfigService;
    @Autowired
    private PublicGoldenFApi goldenFApi;
    @Autowired
    @Qualifier("accountChangeJob")
    private AsyncService asyncService;
    @Value("${project.goldenf.currency:null}")
    private String currency;

    @ApiOperation("开游戏")
    @RequestLimit(limit = 1, timeout = 5)
    @Transactional
    @PostMapping("/openGame")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "gameCode", value = "游戏代码", required = true),
    })
    public ResponseEntity openGame(String gameCode, HttpServletRequest request) {
        boolean checkNull = CommonUtil.checkNull(gameCode);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        //获取登陆用户
        Long authId = CasinoWebUtil.getAuthId();
        UserThird third = userThirdService.findByUserId(authId);
        //未注册自动注册到第三方
        if (third == null || ObjectUtils.isEmpty(third.getGoldenfAccount())) {
            String account = UUID.randomUUID().toString();
            account = account.replaceAll("-", "");
            if (account.length() > 20) {
                account = account.substring(0, 20);
            }
            boolean register = goldenFApi.playerCreate(account, currency);
            if (!register) {
                log.error("GoldenF注册账号失败");
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
            if (third == null) {
                third = new UserThird();
                third.setUserId(authId);
            }
            third.setGoldenfAccount(account);
            try {
                userThirdService.save(third);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("本地注册账号失败,userId:{},{}", authId, e.getMessage());
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
        }
        User user = userService.findById(authId);
        //TODO 扣款时考虑当前用户余额大于平台在三方的余额最大只能转入平台余额
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(authId);
        BigDecimal userCenterMoney = BigDecimal.ZERO;
        if (userMoney != null && userMoney.getMoney() != null) {
            userCenterMoney = userMoney.getMoney();
        }

//        if (platformConfig != null && platformConfig.getWmMoney() != null) {
//            BigDecimal wmMoney = platformConfig.getWmMoney();
//            if (wmMoney != null && wmMoney.compareTo(BigDecimal.ZERO) == 1) {
//                if (wmMoney.compareTo(userCenterMoney) == -1) {
//                    userCenterMoney = wmMoney;
//                }
//            }
//        }
        String goldenfAccount = third.getGoldenfAccount();
        if (userCenterMoney.compareTo(BigDecimal.ZERO) == 1) {
            String orderNo = orderService.getOrderNo();
            //加点
            ResponseEntity responseEntity = transferIn(goldenfAccount, authId, userCenterMoney.doubleValue(), orderNo);
            if (responseEntity!=null){
                return responseEntity;
            }
            //钱转入第三方后本地扣减记录账变，扣款
            userMoneyService.subMoney(authId, userCenterMoney);
            Order order = new Order();
            order.setMoney(userCenterMoney);
            order.setUserId(authId);
            order.setRemark("自动转入PG/CQ9");
            order.setType(0);
            order.setState(Constants.order_wait);
            order.setNo(orderNo);
            order.setFirstProxy(user.getFirstProxy());
            order.setSecondProxy(user.getSecondProxy());
            order.setThirdProxy(user.getThirdProxy());
            orderService.save(order);

            //账变中心记录账变
            AccountChangeVo vo = new AccountChangeVo();
            vo.setUserId(authId);
            vo.setChangeEnum(AccountChangeEnum.PG_CQ9_IN);
            vo.setAmount(userCenterMoney.negate());
            vo.setAmountBefore(userMoney.getMoney());
            vo.setAmountAfter(userMoney.getMoney().subtract(userCenterMoney));
            asyncService.executeAsync(vo);
        }

        //开游戏
        String language = request.getHeader(Constants.LANGUAGE);
        String languageCode = LanguageEnum.getLanguageCode(language);
        PublicGoldenFApi.ResponseEntity entity = goldenFApi.startGame(goldenfAccount, gameCode, languageCode, null);
        if (entity == null) {
            log.error("userId:{},进游戏获取gameUrl失败", third.getUserId());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (!ObjectUtils.isEmpty(entity.getErrorCode())) {
            log.error("userId:{},errorCode={},errorMsg={}", third.getUserId(), entity.getErrorCode(), entity.getErrorMessage());
            return ResponseUtil.custom("进入游戏失败,请联系客服");
        }
        JSONObject jsonData = JSONObject.parseObject(entity.getData());
        String gameUrl = jsonData.getString("game_url");
        return ResponseUtil.success(gameUrl);
    }

    public ResponseEntity transferIn(String playerName,Long userId,double amonut,String orderNo){
        PublicGoldenFApi.ResponseEntity entity = goldenFApi.transferIn(playerName, amonut, orderNo, null);
        if (entity == null) {
            log.error("userId:{},进游戏加扣点失败", userId);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (!ObjectUtils.isEmpty(entity.getErrorCode())) {
            log.error("userId:{},errorCode={},errorMsg={}", userId, entity.getErrorCode(), entity.getErrorMessage());
            return ResponseUtil.custom("加点失败,请联系客服");
        }
        //三方强烈建议提值/充值后使用 5.9 获取单个玩家的转账记录 进一步确认交易是否成功，避免造成金额损失
        long time = System.currentTimeMillis();
        PublicGoldenFApi.ResponseEntity playerTransactionRecord = goldenFApi.getPlayerTransactionRecord(playerName, time, time, null, orderNo, null);
        if (playerTransactionRecord == null) {
            log.error("userId:{},进游戏查询转账记录失败", userId);
            return ResponseUtil.custom("加点失败,请联系客服");
        }
        if (!ObjectUtils.isEmpty(playerTransactionRecord.getErrorCode())) {
            log.error("userId:{},errorCode={},errorMsg={}", userId, playerTransactionRecord.getErrorCode(), playerTransactionRecord.getErrorMessage());
            return ResponseUtil.custom("加点失败,请联系客服");
        }
        JSONObject jsonData = JSONObject.parseObject(playerTransactionRecord.getData());
        JSONArray translogs = jsonData.getJSONArray("translogs");
        if (translogs.size() == 0) {
            return ResponseUtil.custom("加点失败,请联系客服");
        }
        return null;
    }

   /* @ApiOperation("查询当前登录用户WM余额")
    @GetMapping("getWmBalance")
    public ResponseEntity<BigDecimal> getWmBalance() {
        //获取登陆用户
        Long authId = CasinoWebUtil.getAuthId();
        UserThird third = userThirdService.findByUserId(authId);
        if (third == null) {
            return ResponseUtil.success(BigDecimal.ZERO);
        }
        User user = userService.findById(authId);
        Integer lang = user.getLanguage();
        if (lang == null) {
            lang = 0;
        }
        try {
            BigDecimal balance = wmApi.getBalance(third.getAccount(), lang);
            if (balance == null) {
                log.error("userId:{},获取用户WM余额为null", authId);
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
            return ResponseUtil.success(balance.setScale(2, BigDecimal.ROUND_HALF_UP));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("userId:{},获取用户WM余额失败{}", authId, e.getMessage());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
    }

    @ApiOperation("查询用户WM余额外部接口")
//    @RequestLimit(limit = 1, timeout = 5)
    @GetMapping("getWmBalanceApi")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "第三方账号", required = true),
            @ApiImplicitParam(name = "lang", value = "语言", required = true),
    })
    public ResponseEntity<BigDecimal> getWmBalanceApi(String account, Integer lang) {
        log.info("开始查询WM余额:account={},lang={}", account, lang);
        if (!ipWhiteCheck()) {
            return ResponseUtil.custom("ip禁止访问");
        }
        if (CasinoWebUtil.checkNull(account, lang)) {
            return ResponseUtil.parameterNotNull();
        }
        try {
            BigDecimal balance = wmApi.getBalance(account, lang);
            if (balance == null) {
                log.error("account:{},获取用户WM余额为null", account);
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
            log.info("WM余额查询成功:account={},lang={},balance", account, lang, balance);
            return ResponseUtil.success(balance);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("account:{},获取用户WM余额失败{}", account, e.getMessage());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
    }

    @ApiOperation("一键回收当前登录用户WM余额")
    @Transactional
    @GetMapping("oneKeyRecover")
    public ResponseEntity oneKeyRecover() {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        return oneKeyRecoverCommon(userId);
    }

    @ApiOperation("一键回收用户WM余额外部接口")
    @Transactional
    @GetMapping("oneKeyRecoverApi")
    @NoAuthentication
    @ApiImplicitParam(name = "userId", value = "用户ID", required = true)
    public ResponseEntity oneKeyRecoverApi(Long userId) {
        if (!ipWhiteCheck()) {
            return ResponseUtil.custom("ip禁止访问");
        }
        return oneKeyRecoverCommon(userId);
    }

    public ResponseEntity oneKeyRecoverCommon(Long userId) {
        log.info("开始回收wm余额，userId={}", userId);
        if (CasinoWebUtil.checkNull(userId)) {
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
        Order order = new Order();
        order.setMoney(balance);
        order.setUserId(userId);
        order.setRemark("自动转出WM");
        order.setType(1);
        order.setState(Constants.order_wait);
        String orderNo = orderService.getOrderNo();
        order.setNo(orderNo);
        order.setFirstProxy(user.getFirstProxy());
        order.setSecondProxy(user.getSecondProxy());
        order.setThirdProxy(user.getThirdProxy());
        orderService.save(order);
        //账变中心记录账变
        AccountChangeVo vo = new AccountChangeVo();
        vo.setUserId(userId);
        vo.setChangeEnum(AccountChangeEnum.RECOVERY);
        vo.setAmount(balance);
        vo.setAmountBefore(userMoney.getMoney());
        vo.setAmountAfter(userMoney.getMoney().add(balance));
        asyncService.executeAsync(vo);
        log.info("wm余额回收成功，userId={}", userId);
        return ResponseUtil.success();
    }

    private Boolean ipWhiteCheck() {
        if (ObjectUtils.isEmpty(ipWhite)) {
            return false;
        }
        String ip = IpUtil.getIp(CasinoWebUtil.getRequest());
        String[] ipWhiteArray = ipWhite.split(",");
        for (String ipw : ipWhiteArray) {
            if (!ObjectUtils.isEmpty(ipw) && ipw.trim().equals(ip)) {
                return true;
            }
        }
        return false;
    }*/
}
