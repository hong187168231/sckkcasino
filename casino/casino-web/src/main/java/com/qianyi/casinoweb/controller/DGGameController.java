package com.qianyi.casinoweb.controller;

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
import com.qianyi.livedg.api.DgApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.config.LocaleConfig;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/dgGame")
@Api(tags = "DG游戏")
@Slf4j
public class DGGameController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private UserThirdService userThirdService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ThirdGameBusiness thirdGameBusiness;
    @Autowired
    private ErrorOrderService errorOrderService;

    @Autowired
    private DgApi dgApi;
    @Autowired
    private RedisKeyUtil redisKeyUtil;

    @ApiOperation("获取token，前端自行开启游戏")
    @PostMapping("/openGame")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "platform", value = "DG：DG", required = true),
            @ApiImplicitParam(name = "loginType", value = "1：PC浏览器  0：手机浏览器进入游戏", required = true)
    })
    public ResponseEntity openGame(String platform, HttpServletRequest request,String loginType) {
        boolean checkNull = CommonUtil.checkNull(platform);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        //判断平台和游戏状态
        ResponseEntity response = thirdGameBusiness.checkPlatformAndGameStatus(Constants.PLATFORM_DG, platform);
        if (response.getCode() != ResponseCode.SUCCESS.getCode()) {
            return response;
        }
        //获取登陆用户
        Long authId = CasinoWebUtil.getAuthId();
        User user = userService.findById(authId);
        String account = user.getAccount();
        UserThird third = null;
        synchronized (authId) {
            third = userThirdService.findByUserId(authId);
            //未注册自动注册到第三方
            if (third == null || ObjectUtils.isEmpty(third.getDgAccount())) {
                if (third == null) {
                    third = new UserThird();
                    third.setUserId(authId);
                }
                third.setDgAccount(account);
                try {
                    //在DG平台注册用户，密码默认为账号
                    JSONObject apiResponseData = dgApi.createDgMemberGame(account, DigestUtils.md5Hex(account));
                    if ((null != apiResponseData && "0".equals(apiResponseData.getString("codeId"))) ||
                            (null != apiResponseData && "103".equals(apiResponseData.getString("codeId"))) ||//此账号被占用
                            (null != apiResponseData && "116".equals(apiResponseData.getString("codeId")))//账号已占用
                    ){
                        userThirdService.save(third);
                    }else {//返回DG平台错误信息
                        return dgApi.errorCode(apiResponseData.getIntValue("codeId"), apiResponseData.getString("random"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("DG本地注册账号失败,userId:{},{}", authId, e.getMessage());
                    return ResponseUtil.custom("服务器异常,请重新操作");
                }
            }
        }

        //重置缓存时间
        ExpirationTimeUtil.resetExpirationTime(Constants.PLATFORM_DG,authId.toString());

        //回收其他游戏的余额
        thirdGameBusiness.oneKeyRecoverOtherGame(authId, Constants.PLATFORM_DG);
        UserMoney userMoney = userMoneyService.findByUserId(authId);
        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(authId.toString());
        try {
            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
            BigDecimal userCenterMoney = BigDecimal.ZERO;
            if (userMoney != null && userMoney.getMoney() != null) {
                userCenterMoney = userMoney.getMoney();
            }
            if (userCenterMoney.compareTo(BigDecimal.ZERO) == 1) {
                //钱转入第三方后本地扣减记录账变  优先扣减本地余额，否则会出现三方加点成功，本地扣减失败的情况
                //先扣本地款
                userMoneyService.subMoney(authId, userCenterMoney,userMoney);
                String orderNo = orderService.getOrderNo();
                JSONObject deposit = null;
                try {
                    //加点
                    deposit = dgApi.transterWallet( third.getDgAccount(), userCenterMoney,orderNo);
                    if (null != deposit && "0".equals(deposit.getString("codeId"))){
                        //记录账变
                        thirdGameBusiness.inSaveAccountChange(authId, userCenterMoney, userMoney.getMoney(), userMoney.getMoney().subtract(userCenterMoney), 0, orderNo, "自动转入DG", Constants.PLATFORM_DG, AccountChangeEnum.DG_IN);

                    }else if(null == deposit) {
                        log.error("userId:{},DG游戏加点失败,result:{}", authId, deposit);
                        //三方加扣点失败再把钱加回来
                        userMoneyService.addMoney(authId, userCenterMoney);
                        return ResponseUtil.custom("服务器异常,请重新操作");
                    }else {
                        log.error("userId:{},DG游戏加点失败,result:{}", authId, deposit);
                        //三方加扣点失败再把钱加回来
                        userMoneyService.addMoney(authId, userCenterMoney);
                        return ResponseUtil.custom("服务器异常,请重新操作");
                    }

                } catch (Exception e) {
                    log.error("userId:{},account:{},money:{},DG游戏加点失败,远程请求异常", third.getUserId(), user.getAccount(), userCenterMoney);
                    //三方加扣点失败再把钱加回来
                    userMoneyService.addMoney(authId, userCenterMoney);
                    //异步记录错误订单并重试补偿
                    errorOrderService.syncSaveDgErrorOrder(third.getDgAccount(), user.getId(), user.getAccount(), orderNo, userCenterMoney, AccountChangeEnum.DG_IN, Constants.PLATFORM_DG);
                    return dgApi.errorCode(deposit.getIntValue("codeId"), deposit.getString("random"));
                }

            }
            log.info("DG登录请求参数 id，{} 用户名,{} 客户id{}", user.getAccount(), authId);
            JSONObject dgApiResponseData = null;
            try {
                //有些异步请求提示会获取不到request
                HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                String language = httpServletRequest.getHeader(Constants.LANGUAGE);
                String lang = "";
                if (LocaleConfig.zh_CN.toString().equals(language)) {
                    lang = "cn";
                } else if (LocaleConfig.km_KH.toString().equals(language)) {
                    lang = "en";
                } else if (LocaleConfig.as_MY.toString().equals(language)) {
                    lang = "en";
                } else if (LocaleConfig.th_TH.toString().equals(language)) {
                    lang = "th";
                }else {
                    lang = "en";
                }
                //            dgApiResponseData = dgApi.loginDgGameFree(user.getAccount(), lang);//测试环境会员试玩登入
                dgApiResponseData = dgApi.loginDgGame(user.getAccount(),  lang);//正式环境会员登入
                if (null != dgApiResponseData && "0".equals(dgApiResponseData.getString("codeId"))) {
                    StringBuilder builder = new StringBuilder();
                    //                1：PC浏览器  0：手机浏览器进入游戏
                    String urlStr = "";
                    if("1".equals(loginType)){
                        urlStr = (String) dgApiResponseData.getJSONArray("list").get(0);
                    }else {
                        urlStr = (String) dgApiResponseData.getJSONArray("list").get(1);
                    }
                    builder.append(urlStr).append(dgApiResponseData.getString("token"));
                    builder.append("&language=").append(lang);
                    log.error("DG登录完整请求地址,url:{}", builder.toString());
                    //登录
                    return ResponseUtil.success(builder.toString());
                }else {
                    return dgApi.errorCode(dgApiResponseData.getIntValue("codeId"), dgApiResponseData.getString("random"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("DG本地注册账号失败,userId:{},{}", authId, e.getMessage());
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
        } catch (Exception e) {
            log.error("DG上分出现异常userId{} {}", authId, e.getMessage());
            return ResponseUtil.custom("服务器异常,请重新操作");
        } finally {
            // 释放锁
            RedisKeyUtil.unlock(userMoneyLock);
        }
    }


    @ApiOperation("查询当前登录用户DG余额")
    @GetMapping("/getBalance")
    public ResponseEntity<BigDecimal> getBalance() {
        //获取登陆用户
        Long authId = CasinoWebUtil.getAuthId();
        UserThird third = userThirdService.findByUserId(authId);
        if (third == null || ObjectUtils.isEmpty(third.getDgAccount())) {
            return ResponseUtil.success(BigDecimal.ZERO);
        }
        BigDecimal balance = BigDecimal.ZERO;
        ResponseEntity<BigDecimal> responseEntity = thirdGameBusiness.getDgBalanceByAccount(third.getDgAccount());
        if (responseEntity.getData() != null) {
            balance = responseEntity.getData();
        }
        balance = balance.setScale(2, BigDecimal.ROUND_HALF_UP);
        ExpirationTimeUtil.resetTripartiteBalance(Constants.PLATFORM_DG,authId.toString(),balance);
        return ResponseUtil.success(balance);
    }



    @ApiOperation("查询用户DG余额外部接口")
    @GetMapping("/getBalanceApi")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户ID,为空时查全部", required = false)
    })
    public ResponseEntity<BigDecimal> getBalanceApi(Long userId) {
        Boolean ipWhiteCheck = thirdGameBusiness.ipWhiteCheck();
        if (!ipWhiteCheck) {
            return ResponseUtil.custom("ip禁止访问");
        }
        String DGAccount = "null";
        if (userId != null) {
            UserThird third = userThirdService.findByUserId(userId);
            if (third == null || ObjectUtils.isEmpty(third.getDgAccount())) {
                return ResponseUtil.custom("当前用户暂未进入过游戏");
            }
            DGAccount = third.getDgAccount();
        }
        List<String> idList = new LinkedList<>();
        idList.add(userId + "");
        BigDecimal balance = BigDecimal.ZERO;
        ResponseEntity<BigDecimal> responseEntity = thirdGameBusiness.getDgBalanceByAccount(DGAccount);
        if (responseEntity.getData() != null) {
            balance = responseEntity.getData();
        }
        balance = balance.setScale(2, BigDecimal.ROUND_HALF_UP);
        ExpirationTimeUtil.resetTripartiteBalance(Constants.PLATFORM_DG,userId.toString(),balance);
        return ResponseUtil.success(balance);
    }

    @ApiOperation(value = "一键回收当前登录用户DG余额")
    @GetMapping("/oneKeyRecover")
    public ResponseEntity oneKeyRecover() {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        return thirdGameBusiness.oneKeyRecoverDG(userId);
    }

    @ApiOperation(value = "一键回收用户DG余额外部接口")
    @GetMapping("/oneKeyRecoverApi")
    @NoAuthentication
    @ApiImplicitParam(name = "userId", value = "用户ID", required = true)
    public ResponseEntity oneKeyRecoverApi(Long userId) {
        Boolean ipWhiteCheck = thirdGameBusiness.ipWhiteCheck();
        if (!ipWhiteCheck) {
            return ResponseUtil.custom("ip禁止访问");
        }
        return thirdGameBusiness.oneKeyRecoverDG(userId);
    }

}
