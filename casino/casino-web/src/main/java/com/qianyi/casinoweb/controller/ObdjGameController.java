package com.qianyi.casinoweb.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.business.ThirdGameBusiness;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.liveob.api.PublicObdjApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.IpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/obGame")
@Api(tags = "OB电竞游戏厅")
@Slf4j
public class ObdjGameController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private UserThirdService userThirdService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private PublicObdjApi obApi;
    @Autowired
    private ThirdGameBusiness thirdGameBusiness;
    @Autowired
    private ErrorOrderService errorOrderService;
    @Autowired
    @Qualifier("accountChangeJob")
    private AsyncService asyncService;
    @Value("${project.ipWhite}")
    private String ipWhite;

    @ApiOperation("开游戏")
    @PostMapping("/openGame")
    public ResponseEntity openGame(HttpServletRequest request) {
        //获取登陆用户
        Long authId = CasinoWebUtil.getAuthId();
        UserThird third = userThirdService.findByUserId(authId);
        //未注册自动注册到第三方
        if (third == null || ObjectUtils.isEmpty(third.getObAccount())) {
            String account = UUID.randomUUID().toString();
            account = account.replaceAll("-", "");
            if (account.length() > 20) {
                account = account.substring(0, 20);
            }
            boolean register = obApi.register(account, account);
            if (!register) {
                log.error("OB电竞注册账号失败");
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
            if (third == null) {
                third = new UserThird();
                third.setUserId(authId);
            }
            third.setObAccount(account);
            try {
                userThirdService.save(third);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("本地注册账号失败,userId:{},{}", authId, e.getMessage());
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
        }
        //回收其他游戏的余额
        thirdGameBusiness.oneKeyRecoverOtherGame(authId,Constants.PLATFORM_OBDJ);
        //TODO 扣款时考虑当前用户余额大于平台在三方的余额最大只能转入平台余额
        UserMoney userMoney = userMoneyService.findByUserId(authId);
        BigDecimal userCenterMoney = BigDecimal.ZERO;
        if (userMoney != null && userMoney.getMoney() != null) {
            userCenterMoney = userMoney.getMoney();
        }
        String obAccount = third.getObAccount();
        User user = userService.findById(authId);
        if (userCenterMoney.compareTo(BigDecimal.ZERO) == 1) {
            //钱转入第三方后本地扣减记录账变  优先扣减本地余额，否则会出现三方加点成功，本地扣减失败的情况
            //先扣本地款
            userMoneyService.subMoney(authId, userCenterMoney);
            String orderNo = orderService.getObOrderNo();
            //加点
            PublicObdjApi.ResponseEntity transfer = obApi.transfer(third.getObAccount(), 1, userCenterMoney, orderNo);
            if (transfer == null) {
                log.error("userId:{},account:{},money:{},进OB电竞游戏加点失败,远程请求异常", third.getUserId(), user.getAccount(), userCenterMoney);
                //异步记录错误订单并重试补偿
                errorOrderService.syncSaveErrorOrder(third.getAccount(), user.getId(), user.getAccount(), orderNo, userCenterMoney, AccountChangeEnum.OBDJ_IN, Constants.PLATFORM_OBDJ);
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
            if (PublicObdjApi.STATUS_FALSE.equals(transfer.getStatus())) {
                log.error("userId:{},进OB电竞游戏加点失败,msg:{}", authId, transfer.getData());
                //三方加扣点失败再把钱加回来
                userMoneyService.addMoney(authId, userCenterMoney);
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
            //记录账变
            saveAccountChange(authId, userCenterMoney, userMoney.getMoney(), userMoney.getMoney().subtract(userCenterMoney), 0, orderNo, "自动转入OB电竞");
        }
        //开游戏
        String ip = IpUtil.getIp(CasinoWebUtil.getRequest());
        if (ObjectUtils.isEmpty(ip)) {
            ip = "127.0.0.1";
        }
        ip = ip.replaceAll("\\.", "");
        PublicObdjApi.ResponseEntity login = obApi.login(obAccount, obAccount, ip);
        if (login == null) {
            log.error("userId:{}，account:{},进OB电竞游戏登录失败,远程请求异常", authId, user.getAccount());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (PublicObdjApi.STATUS_FALSE.equals(login.getStatus())) {
            log.error("userId:{}，account:{},进OB电竞游戏登录失败,msg:{}", authId, user.getAccount(), login.getData());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        JSONObject gameUrl = JSONObject.parseObject(login.getData());
        return ResponseUtil.success(gameUrl);
    }

    public void saveAccountChange(Long userId, BigDecimal amount, BigDecimal amountBefore, BigDecimal amountAfter, Integer type, String orderNo, String remark) {
        User user = userService.findById(userId);
        Order order = new Order();
        order.setMoney(amount);
        order.setUserId(userId);
        order.setRemark(remark);
        order.setType(type);
        order.setState(Constants.order_wait);
        order.setNo(orderNo);
        order.setGamePlatformName(Constants.PLATFORM_OBDJ);
        order.setFirstProxy(user.getFirstProxy());
        order.setSecondProxy(user.getSecondProxy());
        order.setThirdProxy(user.getThirdProxy());
        orderService.save(order);

        //账变中心记录账变
        AccountChangeVo vo = new AccountChangeVo();
        vo.setUserId(userId);
        vo.setChangeEnum(AccountChangeEnum.OBDJ_IN);
        vo.setAmount(amount.negate());
        vo.setAmountBefore(amountBefore);
        vo.setAmountAfter(amountAfter);
        asyncService.executeAsync(vo);
    }

    @ApiOperation("查询当前登录用户OB电竞余额")
    @GetMapping("/getBalance")
    public ResponseEntity<BigDecimal> getBalance() {
        //获取登陆用户
        Long authId = CasinoWebUtil.getAuthId();
        UserThird third = userThirdService.findByUserId(authId);
        if (third == null || ObjectUtils.isEmpty(third.getObAccount())) {
            return ResponseUtil.success(BigDecimal.ZERO);
        }
        BigDecimal balance = BigDecimal.ZERO;
        ResponseEntity<BigDecimal> responseEntity = thirdGameBusiness.getBalanceOb(third.getObAccount(), authId);
        if (responseEntity.getData() != null) {
            balance = responseEntity.getData();
        }
        balance = balance.setScale(2, BigDecimal.ROUND_HALF_UP);
        responseEntity.setData(balance);
        return responseEntity;
    }

    @ApiOperation("查询用户OB电竞余额外部接口")
    @GetMapping("/getBalanceApi")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户ID", required = true)
    })
    public ResponseEntity<BigDecimal> getBalanceApi(Long userId) {
        log.info("开始查询OB电竞余额:userId={}", userId);
        if (!ipWhiteCheck()) {
            return ResponseUtil.custom("ip禁止访问");
        }
        if (CasinoWebUtil.checkNull(userId)) {
            return ResponseUtil.parameterNotNull();
        }
        UserThird third = userThirdService.findByUserId(userId);
        if (third == null || ObjectUtils.isEmpty(third.getObAccount())) {
            return ResponseUtil.custom("当前用户暂未进入过游戏");
        }
        ResponseEntity<BigDecimal> responseEntity = thirdGameBusiness.getBalanceOb(third.getObAccount(), userId);
        return responseEntity;
    }

    @ApiOperation(value = "一键回收当前登录用户OB电竞余额")
    @GetMapping("/oneKeyRecover")
    public ResponseEntity oneKeyRecover() {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        return thirdGameBusiness.oneKeyRecoverOb(userId);
    }

    @ApiOperation(value = "一键回收用户OB电竞余额外部接口")
    @GetMapping("/oneKeyRecoverApi")
    @NoAuthentication
    @ApiImplicitParam(name = "userId", value = "用户ID", required = true)
    public ResponseEntity oneKeyRecoverApi(Long userId) {
        if (!ipWhiteCheck()) {
            return ResponseUtil.custom("ip禁止访问");
        }
        return thirdGameBusiness.oneKeyRecoverOb(userId);
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
    }
}
