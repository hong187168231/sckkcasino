package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.business.ThirdGameBusiness;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.liveae.constants.LanguageEnum;
import com.qianyi.lottery.api.PublicLotteryApi;
import com.qianyi.lottery.util.LanguageVNCEnum;
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

@RestController
@RequestMapping("/vncGame")
@Api(tags = "VNC游戏")
@Slf4j
public class GameVNDController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private UserThirdService userThirdService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private PublicLotteryApi lotteryApi;
    @Autowired
    private ThirdGameBusiness thirdGameBusiness;
    @Autowired
    private ErrorOrderService errorOrderService;
    @Autowired
    private PlatformConfigService platformConfigService;

    @ApiOperation("启动游戏")
    @PostMapping("/openGame")
    @ApiImplicitParam(name = "platform", value = "VNC", required = true)
    public ResponseEntity<String> openGame(String platform, HttpServletRequest request) {
        boolean checkNull = CommonUtil.checkNull(platform);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }

        //判断平台和游戏状态
        ResponseEntity response = thirdGameBusiness.checkPlatformAndGameStatus(Constants.PLATFORM_VNC, platform);
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
            if (third == null || ObjectUtils.isEmpty(third.getVncAccount())) {
                //三方大写都会转成小写
                account = createMember(account);
                if (ObjectUtils.isEmpty(account)) {
                    return ResponseUtil.custom("服务器异常,请重新操作");
                }
                if (third == null) {
                    third = new UserThird();
                    third.setUserId(authId);
                }
                third.setVncAccount(account);
                try {
                    userThirdService.save(third);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("AE本地注册账号失败,userId:{},{}", authId, e.getMessage());
                    return ResponseUtil.custom("服务器异常,请重新操作");
                }
            }
        }
        //回收其他游戏的余额
        thirdGameBusiness.oneKeyRecoverOtherGame(authId, Constants.PLATFORM_VNC);
        //TODO 扣款时考虑当前用户余额大于平台在三方的余额最大只能转入平台余额
        UserMoney userMoney = userMoneyService.findByUserId(authId);
        BigDecimal userCenterMoney = BigDecimal.ZERO;
        if (userMoney != null && userMoney.getMoney() != null) {
            userCenterMoney = userMoney.getMoney().divideToIntegralValue(BigDecimal.ONE);
        }
        if (userCenterMoney.compareTo(BigDecimal.ZERO) == 1) {
            //钱转入第三方后本地扣减记录账变  优先扣减本地余额，否则会出现三方加点成功，本地扣减失败的情况
            //先扣本地款
            userMoneyService.subMoney(authId, userCenterMoney);
            String orderNo = orderService.getOrderNo();
            //加点
            PublicLotteryApi.ResponseEntity responseEntity = lotteryApi.changeBalance(account, 1, userCenterMoney, orderNo);
            if (responseEntity == null) {
                log.error("userId:{},account:{},money:{},进VNC游戏加点失败,远程请求异常", third.getUserId(), user.getAccount(), userCenterMoney);
                //异步记录错误订单并重试补偿
                errorOrderService.syncSaveVNCErrorOrder(third.getVncAccount(), user.getId(), user.getAccount(), orderNo, userCenterMoney, AccountChangeEnum.VNC_IN, Constants.PLATFORM_VNC);
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
            String status = responseEntity.getErrorCode();
            if (!"0".equals(status)) {
                log.error("userId:{},进VNC游戏加点失败,result:{}", authId, responseEntity);
                //三方加扣点失败再把钱加回来
                userMoneyService.addMoney(authId, userCenterMoney);
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
            //记录账变
            thirdGameBusiness.inSaveAccountChange(authId, userCenterMoney, userMoney.getMoney(), userMoney.getMoney().subtract(userCenterMoney), 0, orderNo, "自动转入VNC", Constants.PLATFORM_VNC, AccountChangeEnum.VNC_IN);
        }
        //开游戏
        String language = request.getHeader(Constants.LANGUAGE);
        String languageCode = LanguageVNCEnum.getLanguageCode(language);
        String gameCode = "VNC";

        String url = lotteryApi.lanuchGame(account, languageCode, "1", gameCode);
        if (ObjectUtils.isEmpty(url)) {
            log.error("userId:{}，account:{},进VNC游戏登录失败,远程请求异常", authId, user.getAccount());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        return ResponseUtil.success(url);

    }

    @ApiOperation("查询当前登录用户VNC余额")
    @GetMapping("/getBalance")
    public ResponseEntity<BigDecimal> getBalance() {
        //获取登陆用户
        Long authId = CasinoWebUtil.getAuthId();
        UserThird third = userThirdService.findByUserId(authId);
        if (third == null || ObjectUtils.isEmpty(third.getVncAccount())) {
            return ResponseUtil.success(BigDecimal.ZERO);
        }
        BigDecimal balance = BigDecimal.ZERO;
        ResponseEntity<BigDecimal> responseEntity = thirdGameBusiness.getVNCBalanceByAccount(third.getVncAccount());
        if (responseEntity.getData() != null) {
            balance = responseEntity.getData();
        }
        balance = balance.setScale(2, BigDecimal.ROUND_HALF_UP);
        responseEntity.setData(balance);
        return responseEntity;
    }

    @ApiOperation("查询用户VNC余额外部接口")
    @GetMapping("/getBalanceApi")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户ID,为空时查全部", required = false)
    })
    public ResponseEntity<BigDecimal> getBalanceApi(Long userId) {
        log.info("开始查询VNC余额:userId={}", userId);
        Boolean ipWhiteCheck = thirdGameBusiness.ipWhiteCheck();
        if (!ipWhiteCheck) {
            return ResponseUtil.custom("ip禁止访问");
        }
        String aeAccount = "null";
        if (userId != null) {
            UserThird third = userThirdService.findByUserId(userId);
            if (third == null || ObjectUtils.isEmpty(third.getVncAccount())) {
                return ResponseUtil.custom("当前用户暂未进入过游戏");
            }
            aeAccount = third.getVncAccount();
        }
        ResponseEntity responseEntity = thirdGameBusiness.getAllVNCBalance(aeAccount);
        return responseEntity;
    }

    @ApiOperation(value = "一键回收当前登录用户VNC余额")
    @GetMapping("/oneKeyRecover")
    public ResponseEntity oneKeyRecover() {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        return thirdGameBusiness.oneKeyRecoverVNC(userId);
    }

    @ApiOperation(value = "一键回收用户VNC余额外部接口")
    @GetMapping("/oneKeyRecoverApi")
    @NoAuthentication
    @ApiImplicitParam(name = "userId", value = "用户ID", required = true)
    public ResponseEntity oneKeyRecoverApi(Long userId) {
        Boolean ipWhiteCheck = thirdGameBusiness.ipWhiteCheck();
        if (!ipWhiteCheck) {
            return ResponseUtil.custom("ip禁止访问");
        }
        return thirdGameBusiness.oneKeyRecoverVNC(userId);
    }

    private String createMember(String account) {
        account = account.toLowerCase();
        boolean createFlag = lotteryApi.createMember(account);
        if (!createFlag) {
            log.error("越南彩注册账号失败,远程请求异常");
            return null;
        }
        return account;
    }
}
