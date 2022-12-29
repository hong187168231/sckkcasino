package com.qianyi.casinoweb.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.business.ThirdGameBusiness;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.lottery.api.LotteryDmcApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

//@RestController
//@RequestMapping("/dmcGame")
//@Api(tags = "大马彩游戏")
//@Slf4j
//public class DMCGameController {
//
//    @Autowired
//    private UserService userService;
//    @Autowired
//    private UserMoneyService userMoneyService;
//    @Autowired
//    private UserThirdService userThirdService;
//    @Autowired
//    private OrderService orderService;
//    @Autowired
//    private ThirdGameBusiness thirdGameBusiness;
//    @Autowired
//    private ErrorOrderService errorOrderService;
//
//    @Autowired
//    private LotteryDmcApi lotteryDmcApi;
//
//    @ApiOperation("获取token，前端自行开启游戏")
//    @PostMapping("/openGame")
//    @ApiImplicitParam(name = "platform", value = "大马彩：DMC", required = true)
//    public ResponseEntity<String> openGame(String platform, HttpServletRequest request) {
//        boolean checkNull = CommonUtil.checkNull(platform);
//        if (checkNull) {
//            return ResponseUtil.parameterNotNull();
//        }
//        //判断平台和游戏状态
//        ResponseEntity response = thirdGameBusiness.checkPlatformAndGameStatus(Constants.PLATFORM_DMC, platform);
//        if (response.getCode() != ResponseCode.SUCCESS.getCode()) {
//            return response;
//        }
//        //获取登陆用户
//        Long authId = CasinoWebUtil.getAuthId();
//        User user = userService.findById(authId);
//        String account = user.getAccount();
//        UserThird third = null;
//        synchronized (authId) {
//            third = userThirdService.findByUserId(authId);
//            //未注册自动注册到第三方
//            if (third == null || ObjectUtils.isEmpty(third.getDmcAccount())) {
//                if (third == null) {
//                    third = new UserThird();
//                    third.setUserId(authId);
//                }
//                third.setDmcAccount(account);
//                try {
//                    userThirdService.save(third);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    log.error("AE本地注册账号失败,userId:{},{}", authId, e.getMessage());
//                    return ResponseUtil.custom("服务器异常,请重新操作");
//                }
//            }
//        }
//        //回收其他游戏的余额
//        thirdGameBusiness.oneKeyRecoverOtherGame(authId, Constants.PLATFORM_DMC);
//        UserMoney userMoney = userMoneyService.findByUserId(authId);
//        BigDecimal userCenterMoney = BigDecimal.ZERO;
//        if (userMoney != null && userMoney.getMoney() != null) {
//            userCenterMoney = userMoney.getMoney();
//        }
//        String token = lotteryDmcApi.fetchToken();
//        if (userCenterMoney.compareTo(BigDecimal.ZERO) == 1) {
//            //钱转入第三方后本地扣减记录账变  优先扣减本地余额，否则会出现三方加点成功，本地扣减失败的情况
//            //先扣本地款
//            userMoneyService.subMoney(authId, userCenterMoney,userMoney);
//            String orderNo = orderService.getOrderNo();
//            //加点
//            JSONObject deposit = lotteryDmcApi.transterWallet(userMoney.getUserId() + "", third.getDmcAccount(), userCenterMoney, 1, token,orderNo);
//            if (ObjectUtils.isEmpty(deposit)) {
//                log.error("userId:{},account:{},money:{},DMC游戏加点失败,远程请求异常", third.getUserId(), user.getAccount(), userCenterMoney);
//                //三方加扣点失败再把钱加回来
//                userMoneyService.addMoney(authId, userCenterMoney);
//                //异步记录错误订单并重试补偿
//                errorOrderService.syncSaveDMCErrorOrder(third.getAeAccount(), user.getId(), user.getAccount(), orderNo, userCenterMoney, AccountChangeEnum.DMC_IN, Constants.PLATFORM_DMC);
//                return ResponseUtil.custom("服务器异常,请重新操作");
//            }
//            boolean resultCode = lotteryDmcApi.getResultCode(deposit);
//            if (!resultCode) {
//                log.error("userId:{},大马彩游戏加点失败,result:{}", authId, deposit);
//                //三方加扣点失败再把钱加回来
//                userMoneyService.addMoney(authId, userCenterMoney);
//                return ResponseUtil.custom("服务器异常,请重新操作");
//            }
//            //记录账变
//            thirdGameBusiness.inSaveAccountChange(authId, userCenterMoney, userMoney.getMoney(), userMoney.getMoney().subtract(userCenterMoney), 0, orderNo, "自动转入大马彩", Constants.PLATFORM_DMC, AccountChangeEnum.DMC_IN);
//        }
//        log.info("大马彩登录请求参数 id，{} 用户名,{} 客户id{}", user.getAccount(), authId);
//        String result = lotteryDmcApi.loginDmcGame(third.getDmcAccount(), authId.toString(),token);
//        return ResponseUtil.success(result);
//    }
//
//
//    @ApiOperation("查询当前登录用户DMC余额")
//    @GetMapping("/getBalance")
//    public ResponseEntity<BigDecimal> getBalance() {
//        //获取登陆用户
//        Long authId = CasinoWebUtil.getAuthId();
//        UserThird third = userThirdService.findByUserId(authId);
//        if (third == null || ObjectUtils.isEmpty(third.getDmcAccount())) {
//            return ResponseUtil.success(BigDecimal.ZERO);
//        }
//        BigDecimal balance = thirdGameBusiness.getDMCBalanceByAccount(third.getDmcAccount());
//        balance = balance == null ? BigDecimal.ZERO : balance;
//        balance = balance.setScale(2, BigDecimal.ROUND_HALF_UP);
//        return ResponseUtil.success(balance);
//    }
//
//
//
//    @ApiOperation("查询用户DMC余额外部接口")
//    @GetMapping("/getBalanceApi")
//    @NoAuthentication
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "userId", value = "用户ID,为空时查全部", required = false)
//    })
//    public ResponseEntity<BigDecimal> getBalanceApi(Long userId) {
//        Boolean ipWhiteCheck = thirdGameBusiness.ipWhiteCheck();
//        if (!ipWhiteCheck) {
//            return ResponseUtil.custom("ip禁止访问");
//        }
//        String dmcAccount = "null";
//        if (userId != null) {
//            UserThird third = userThirdService.findByUserId(userId);
//            if (third == null || ObjectUtils.isEmpty(third.getDmcAccount())) {
//                return ResponseUtil.custom("当前用户暂未进入过游戏");
//            }
//            dmcAccount = third.getDmcAccount();
//        }
//        List<String> idList = new LinkedList<>();
//        idList.add(userId + "");
//        BigDecimal balance= thirdGameBusiness.getDMCBalanceByAccount(dmcAccount);
//        return ResponseUtil.success(balance);
//    }
//
//    @ApiOperation(value = "一键回收当前登录用户DMC余额")
//    @GetMapping("/oneKeyRecover")
//    public ResponseEntity oneKeyRecover() {
//        //获取登陆用户
//        Long userId = CasinoWebUtil.getAuthId();
//        return thirdGameBusiness.oneKeyRecoverDMC(userId);
//    }
//
//    @ApiOperation(value = "一键回收用户DMC余额外部接口")
//    @GetMapping("/oneKeyRecoverApi")
//    @NoAuthentication
//    @ApiImplicitParam(name = "userId", value = "用户ID", required = true)
//    public ResponseEntity oneKeyRecoverApi(Long userId) {
//        Boolean ipWhiteCheck = thirdGameBusiness.ipWhiteCheck();
//        if (!ipWhiteCheck) {
//            return ResponseUtil.custom("ip禁止访问");
//        }
//        return thirdGameBusiness.oneKeyRecoverDMC(userId);
//    }
//
//}
