package com.qianyi.casinoweb.controller;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.business.ThirdGameBusiness;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.ExpirationTimeUtil;
import com.qianyi.casinocore.util.RedisKeyUtil;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.util.DeviceUtil;
import com.qianyi.casinoweb.vo.ObdjGameUrlVo;
import com.qianyi.liveob.api.PublicObdjApi;
import com.qianyi.liveob.api.PublicObtyApi;
import com.qianyi.liveob.api.PublicObzrApi;
import com.qianyi.liveob.constants.LanguageEnum;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.IpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/obzrGame")
@Api(tags = "OB真人游戏厅")
@Slf4j
public class ObzrGameController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private UserThirdService userThirdService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private PublicObzrApi obApi;
    @Autowired
    private ThirdGameBusiness thirdGameBusiness;
    @Autowired
    private ErrorOrderService errorOrderService;
    @Autowired
    private RedisKeyUtil redisKeyUtil;

    @ApiOperation("开游戏")
    @PostMapping("/openGame")
    @ApiImplicitParams({@ApiImplicitParam(name = "deviceType", value = "设备类型 1-网页 2-手机网页 3-App iOS或 h5 iOS 4-App Android 或h5 Android", required = true)
           })
    public ResponseEntity<ObdjGameUrlVo> openGame(HttpServletRequest request,Integer deviceType) {
        if (ObjectUtil.isNull(deviceType)) {
            return ResponseUtil.parameterNotNull();
        }
        //判断平台和游戏状态
        ResponseEntity response = thirdGameBusiness.checkPlatformAndGameStatus(Constants.PLATFORM_OB, Constants.PLATFORM_OBZR);
        if (response.getCode() != ResponseCode.SUCCESS.getCode()) {
            return response;
        }
        //获取登陆用户
        Long authId = CasinoWebUtil.getAuthId();
        UserThird third = null;
        synchronized (authId) {
            third = userThirdService.findByUserId(authId);
            User user = userService.findById(authId);
            //未注册自动注册到第三方
            if (third == null || ObjectUtils.isEmpty(third.getObzrAccount())) {
                String obzrAccount = obApi.getMerchantCode().toLowerCase()+"_"+user.getAccount();
                boolean register = obApi.create(obzrAccount, obzrAccount);
                if (!register) {
                    log.error("OB真人注册账号失败");
                    return ResponseUtil.custom("服务器异常,请重新操作");
                }
                if (third == null) {
                    third = new UserThird();
                    third.setUserId(authId);
                }
                third.setObzrAccount(obzrAccount);
                try {
                    userThirdService.save(third);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("OB真人本地注册账号失败,userId:{},{}", authId, e.getMessage());
                    return ResponseUtil.custom("服务器异常,请重新操作");
                }
            }
        }
        String obAccount = third.getObzrAccount();
        User user = userService.findById(authId);

        //重置缓存时间
        ExpirationTimeUtil.resetExpirationTime(Constants.PLATFORM_OBZR,authId.toString());

        //回收其他游戏的余额
        thirdGameBusiness.oneKeyRecoverOtherGame(authId, Constants.PLATFORM_OBZR);
        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(authId.toString());
        try {
            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
            UserMoney userMoney = userMoneyService.findByUserId(authId);
            BigDecimal userCenterMoney = BigDecimal.ZERO;
            if (userMoney != null && userMoney.getMoney() != null) {
                userCenterMoney = userMoney.getMoney();
            }
            if (userCenterMoney.compareTo(BigDecimal.ZERO) == 1) {
                //钱转入第三方后本地扣减记录账变  优先扣减本地余额，否则会出现三方加点成功，本地扣减失败的情况
                //先扣本地款
                userMoneyService.subMoney(authId, userCenterMoney,userMoney);
                String orderNo = orderService.getObdjOrderNo();
                //加点
                PublicObzrApi.ResponseEntity transfer = obApi.deposit(third.getObzrAccount(),  userCenterMoney, orderNo);
                if (transfer == null) {
                    log.error("userId:{},account:{},money:{},进OB真人游戏加点失败,远程请求异常", third.getUserId(), user.getAccount(), userCenterMoney);
                    //异步记录错误订单并重试补偿
                    errorOrderService.syncSaveErrorOrder(third.getObzrAccount(), user.getId(), user.getAccount(), orderNo, userCenterMoney, AccountChangeEnum.OBZR_IN, Constants.PLATFORM_OBZR);
                    return ResponseUtil.custom("服务器异常,请重新操作");
                }
                if (!PublicObzrApi.SUCCESS_CODE.equals(transfer.getCode())) {
                    log.error("userId:{},进OB真人游戏加点失败,msg:{}", authId, transfer.getData());
                    //三方加扣点失败再把钱加回来
                    userMoneyService.addMoney(authId, userCenterMoney);
                    return ResponseUtil.custom("服务器异常,请重新操作");
                }
                //记录账变
                thirdGameBusiness.inSaveAccountChange(authId, userCenterMoney, userMoney.getMoney(), userMoney.getMoney().subtract(userCenterMoney), 0, orderNo, "自动转入OB真人",Constants.PLATFORM_OBZR,AccountChangeEnum.OBZR_IN);
            }
        } catch (Exception e) {
            log.error("OBDJ上分出现异常userId{} {}", authId, e.getMessage());
            return ResponseUtil.custom("服务器异常,请重新操作");
        } finally {
            // 释放锁
            RedisKeyUtil.unlock(userMoneyLock);
        }

        // 开游戏
        PublicObzrApi.ResponseEntity login = obApi.forwardGame(obAccount, deviceType);
        if (login == null) {
            log.error("userId:{}，account:{},进OB真人游戏登录失败,远程请求异常", authId, user.getAccount());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (!login.getCode().equals("200")) {
            log.error("userId:{}，account:{},进OB真人游戏登录失败,msg:{}", authId, user.getAccount(), login.toString());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        JSONObject data = JSONObject.parseObject(login.getData());
        if (ObjectUtils.isEmpty(data)) {
            return ResponseUtil.success(data);
        }
        String loginUrl = data.getString("url");
        return ResponseUtil.success(loginUrl);
    }

    @ApiOperation("查询当前登录用户OB真人余额")
    @GetMapping("/getBalance")
    public ResponseEntity<BigDecimal> getBalance() {
        //获取登陆用户
        Long authId = CasinoWebUtil.getAuthId();
        UserThird third = userThirdService.findByUserId(authId);
        if (third == null || ObjectUtils.isEmpty(third.getObzrAccount())) {
            return ResponseUtil.success(BigDecimal.ZERO);
        }
        BigDecimal balance = BigDecimal.ZERO;
        ResponseEntity<BigDecimal> responseEntity = thirdGameBusiness.getBalanceObzr(third.getObzrAccount(), authId);
        if (responseEntity.getData() != null) {
            balance = responseEntity.getData();
        }
        balance = balance.setScale(2, BigDecimal.ROUND_HALF_UP);
        responseEntity.setData(balance);
        return responseEntity;
    }

    @ApiOperation("查询用户OB真人余额外部接口")
    @GetMapping("/getBalanceApi")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户ID", required = true)
    })
    public ResponseEntity<BigDecimal> getBalanceApi(Long userId) {
        log.info("开始查询OB真人余额:userId={}", userId);
        Boolean ipWhiteCheck = thirdGameBusiness.ipWhiteCheck();
        if (!ipWhiteCheck) {
            return ResponseUtil.custom("ip禁止访问");
        }
        if (CasinoWebUtil.checkNull(userId)) {
            return ResponseUtil.parameterNotNull();
        }
        UserThird third = userThirdService.findByUserId(userId);
        if (third == null || ObjectUtils.isEmpty(third.getObzrAccount())) {
            return ResponseUtil.custom("当前用户暂未进入过游戏");
        }
        ResponseEntity<BigDecimal> responseEntity = thirdGameBusiness.getBalanceObzr(third.getObzrAccount(), userId);
        return responseEntity;
    }

    @ApiOperation(value = "一键回收当前登录用户OB真人余额")
    @GetMapping("/oneKeyRecover")
    public ResponseEntity oneKeyRecover() {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        return thirdGameBusiness.oneKeyRecoverObzr(userId);
    }

    @ApiOperation(value = "一键回收用户OB真人余额外部接口")
    @GetMapping("/oneKeyRecoverApi")
    @NoAuthentication
    @ApiImplicitParam(name = "userId", value = "用户ID", required = true)
    public ResponseEntity oneKeyRecoverApi(Long userId) {
        Boolean ipWhiteCheck = thirdGameBusiness.ipWhiteCheck();
        if (!ipWhiteCheck) {
            return ResponseUtil.custom("ip禁止访问");
        }
        return thirdGameBusiness.oneKeyRecoverObzr(userId);
    }


}