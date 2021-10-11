package com.qianyi.casinoweb.controller;

import java.math.BigDecimal;
import java.util.UUID;

import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.annotation.RequestLimit;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.util.DateUtil;
import com.qianyi.modulecommon.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.model.Order;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.service.OrderService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.service.UserThirdService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.livewm.api.PublicWMApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.Transactional;

@RestController
@RequestMapping("wm")
@Api(tags = "WM游戏厅")
@Slf4j
public class WMController {
    @Autowired
    UserService userService;
    @Autowired
    UserMoneyService userMoneyService;
    @Autowired
    UserThirdService userThirdService;
    @Autowired
    OrderService orderService;
    @Autowired
    PublicWMApi wmApi;
    @Autowired
    @Qualifier("accountChangeJob")
    AsyncService asyncService;

    @Value("${project.signature}")
    String signature;
    @Value("${project.ipWhite}")
    String ipWhite;

    @ApiOperation("开游戏")
    @RequestLimit(limit = 1,timeout = 5)
    @Transactional
    @PostMapping("openGame")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "gameType", value = "默认：大厅。1.百家乐。2.龙虎 3. 轮盘 4. 骰宝 " +
                    "5. 牛牛  6. 三公  7. 番摊  8. 色碟 9. 鱼虾蟹 10. 炸金花 11. 牌九 12. 二八杠", required = false),
    })
    public ResponseEntity openGame(Integer gameType) {
        //获取登陆用户
        Long authId = CasinoWebUtil.getAuthId();
        UserThird third = userThirdService.findByUserId(authId);
        //未注册自动注册到第三方
        if (third == null || third.getUserId() == null) {
            String account = UUID.randomUUID().toString();
            account = account.replaceAll("-", "");

            if (account.length() > 30) {
                account = account.substring(0, 30);
            }
            String password = authId + "qianyi";

            boolean register = wmApi.register(account, password, account, null, null, "", null);
            if (!register) {
                return ResponseUtil.custom("服务器异常,请重新操作");
            }

            third = new UserThird();
            third.setAccount(account);
            third.setPassword(password);
            third.setUserId(authId);
            try {
                userThirdService.save(third);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseUtil.custom("服务器异常,请重新操作");
            }

        }

        User user = userService.findById(authId);

        Integer lang = user.getLanguage();
        if (lang == null) {
            lang = 0;
        }
        //开游戏
        String model = getModel(gameType);

        String url = wmApi.openGame(third.getAccount(), third.getPassword(), lang, null, 4, model);
        if (CommonUtil.checkNull(url)) {
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        //自动转帐,子线程处理
//        new Thread(new OrderBetJob()).start();
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(authId);
        if (userMoney != null && BigDecimal.ZERO.compareTo(userMoney.getMoney()) == -1) {
            BigDecimal money = userMoney.getMoney();
            String orderNo = orderService.getOrderNo();
            boolean isSucc = wmApi.changeBalance(third.getAccount(), money, orderNo, lang);
            //钱转入第三方后本地扣减记录账变
            if (isSucc) {
                //扣款
                //TODO 扣款时考虑当前用户余额不能大于平台在三方的余额
                userMoneyService.subMoney(authId, money);

                Order order = new Order();
                order.setMoney(money);
                order.setUserId(authId);
                order.setRemark("自动转入WM");
                order.setType(0);
                order.setState(Constants.order_wait);
                order.setNo(orderNo);
                order.setFirstProxy(user.getFirstProxy());
                order.setSecondProxy(user.getSecondProxy());
                order.setThirdProxy(user.getThirdProxy());
                orderService.save(order);

                //账变中心记录账变
                AccountChangeVo vo=new AccountChangeVo();
                vo.setUserId(authId);
                vo.setChangeEnum(AccountChangeEnum.WM_IN);
                vo.setAmount(money.negate());
                vo.setAmountBefore(userMoney.getMoney());
                vo.setAmountAfter(userMoney.getMoney().subtract(money));
                asyncService.executeAsync(vo);
            }
        }
        return ResponseUtil.success(url);
    }

    private String getModel(Integer gameType) {
        if (gameType == null) {
            return null;
        }
        String model = "";
// 默认：大厅。1.百家乐。2.龙虎 3. 轮盘 4. 骰宝 5. 牛牛  6. 三公  7. 番摊  8. 色碟 9. 鱼虾蟹 10. 炸金花 11. 牌九 12. 二八杠",
        switch (gameType) {
            case 1:
                model = "onlybac";
                break;
            case 2:
                model = "onlydgtg";
                break;
            case 3:
                model = "onlyrou";
                break;
            case 4:
                model = "onlysicbo";
                break;
            case 5:
                model = "onlyniuniu";
                break;
            case 6:
                model = "onlysamgong";
                break;
            case 7:
                model = "onlyfantan";
                break;
            case 8:
                model = "onlysedie";
                break;
            case 9:
                model = "onlyfishshrimpcrab";
                break;
            case 10:
                model = "onlygoldenflower";
                break;
            case 11:
                model = "onlypaigow";
                break;
            case 12:
                model = "onlythisbar";
                break;
            default:

        }

        return model;
    }

//    /**
//     * 取余额
//     *
//     * @return
//     */
//    @RequestMapping("callBalance")
//    public RespEntity callBalance(String cmd, String signature, String user, String requestDate) {
//        log.info(this.getClass().getSimpleName() + "==>callBalance:cmd{},signature:{},user:{},requestDate:{}", cmd, signature, user, requestDate);
//        RespEntity entity = new RespEntity();
//        if (!"CallBalance".equals(cmd)) {
//            entity.setErrorCode(1);
//            entity.setErrorMessage("参数错误");
//            return entity;
//        }
//
//        if (!this.signature.equals(signature)) {
//            entity.setErrorCode(2);
//            entity.setErrorMessage("参数错误");
//            return entity;
//        }
//
//        UserThird third = userThirdService.findByAccount(user);
//        if (third == null || CommonUtil.checkNull(third.getAccount())) {
//            entity.setErrorCode(3);
//            entity.setErrorMessage("参数错误");
//            return entity;
//        }
//
//        User weUser = userService.findById(third.getUserId());
//        BigDecimal money = weUser.getMoney();
//        if (money == null) {
//            money = BigDecimal.ZERO;
//        }
//
//        JSONObject json = new JSONObject();
//        json.put("user", user);
//        json.put("money", String.valueOf(money));
//        json.put("responseDate", DateUtil.today("yyyy-MM-dd HH:mm:ss"));
//
//        entity.setErrorCode(0);
//        entity.setErrorMessage("success");
//        entity.setResult(json);
//
//        return entity;
//    }

    @ApiOperation("查询当前登录用户WM余额")
    @RequestLimit(limit = 1,timeout = 5)
    @GetMapping("getWmBalance")
    public ResponseEntity getWmBalance() {
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
        BigDecimal balance = BigDecimal.ZERO;
        try {
            balance = wmApi.getBalance(third.getAccount(), lang);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        return ResponseUtil.success(balance);
    }

    @ApiOperation("查询用户WM余额外部接口")
    @RequestLimit(limit = 1,timeout = 5)
    @GetMapping("getWmBalanceApi")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "第三方账号", required = true),
            @ApiImplicitParam(name = "lang", value = "语言", required = true),
            })
    public ResponseEntity getWmBalanceApi(String account, Integer lang) {
        if (CasinoWebUtil.checkNull(account, lang)) {
            return ResponseUtil.parameterNotNull();
        }
        if (!ipWhiteCheck()){
            return ResponseUtil.custom("ip禁止访问");
        }
        BigDecimal balance = BigDecimal.ZERO;
        try {
            balance = wmApi.getBalance(account, lang);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        return ResponseUtil.success(balance);
    }

    @ApiOperation("一键回收当前登录用户WM余额")
    @Transactional
    @RequestLimit(limit = 1,timeout = 5)
    @GetMapping("oneKeyRecover")
    public ResponseEntity oneKeyRecover() {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        UserThird third = userThirdService.findByUserId(userId);
        if (third == null) {
            return ResponseUtil.custom("当前用户未在第三方注册,请注册后再重试");
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
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        //查询用户在wm的余额
        BigDecimal balance = BigDecimal.ZERO;
        try {
            balance = wmApi.getBalance(account, lang);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (BigDecimal.ZERO.compareTo(balance) == 0) {
            return ResponseUtil.success();
        }
        //调用加扣点接口扣减wm余额
        Boolean changeBalance = wmApi.changeBalance(account, balance.negate(), null, lang);
        if (!changeBalance) {
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        //把额度加回本地
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        if (userMoney == null) {
            return ResponseUtil.custom("用户钱包不存在");
        }
        //wm余额大于0
        if (BigDecimal.ZERO.compareTo(balance) == -1) {
            userMoneyService.addMoney(userId, balance);
        } else if (BigDecimal.ZERO.compareTo(balance) == 1) {//wm余额小于0
            userMoneyService.subMoney(userId, balance.abs());
        }
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
        AccountChangeVo vo=new AccountChangeVo();
        vo.setUserId(userId);
        vo.setChangeEnum(AccountChangeEnum.WM_OUT);
        vo.setAmount(balance);
        vo.setAmountBefore(userMoney.getMoney());
        vo.setAmountAfter(userMoney.getMoney().add(balance));
        asyncService.executeAsync(vo);
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
    }

    @Data
    class RespEntity {
        private Integer errorCode;
        private String errorMessage;
        private Object result;
    }
}
