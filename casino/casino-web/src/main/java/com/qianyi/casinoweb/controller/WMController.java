package com.qianyi.casinoweb.controller;

import java.math.BigDecimal;
import java.util.UUID;

import com.qianyi.modulecommon.annotation.RequestLimit;
import com.qianyi.modulecommon.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@RestController
@RequestMapping("wm")
@Api(tags = "WM游戏厅")
@Slf4j
public class WMController {
    @Autowired
    UserService userService;
    @Autowired
    UserThirdService userThirdService;
    @Autowired
    OrderService orderService;
    @Autowired
    PublicWMApi wmApi;

    @Value("${project.signature}")
    String signature;

    @ApiOperation("开游戏")
    @RequestLimit(limit = 1,timeout = 5)
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
        BigDecimal money = user.getMoney();
        if (money != null && money.compareTo(BigDecimal.ZERO) == 1) {
            //扣款
            userService.subMoney(authId, money);

            Order order = new Order();
            order.setMoney(money);
            order.setUserId(authId);
            order.setRemark("WM 自动转帐");
            order.setState(Constants.order_wait);

            String orderNo = orderService.getOrderNo();
            order.setNo(orderNo);
            orderService.save(order);

            boolean isSucc = wmApi.changeBalance(third.getAccount(), money, orderNo, lang);

            //加款
            if (!isSucc) {
                userService.addMoney(authId, money);
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

    /**
     * 取余额
     *
     * @return
     */
    @RequestMapping("callBalance")
    public RespEntity callBalance(String cmd, String signature, String user, String requestDate) {
        log.info(this.getClass().getSimpleName() + "==>callBalance:cmd{},signature:{},user:{},requestDate:{}", cmd, signature, user, requestDate);
        RespEntity entity = new RespEntity();
        if (!"CallBalance".equals(cmd)) {
            entity.setErrorCode(1);
            entity.setErrorMessage("参数错误");
            return entity;
        }

        if (!this.signature.equals(signature)) {
            entity.setErrorCode(2);
            entity.setErrorMessage("参数错误");
            return entity;
        }

        UserThird third = userThirdService.findByAccount(user);
        if (third == null || CommonUtil.checkNull(third.getAccount())) {
            entity.setErrorCode(3);
            entity.setErrorMessage("参数错误");
            return entity;
        }

        User weUser = userService.findById(third.getUserId());
        BigDecimal money = weUser.getMoney();
        if (money == null) {
            money = BigDecimal.ZERO;
        }

        JSONObject json = new JSONObject();
        json.put("user", user);
        json.put("money", String.valueOf(money));
        json.put("responseDate", DateUtil.today("yyyy-MM-dd HH:mm:ss"));

        entity.setErrorCode(0);
        entity.setErrorMessage("success");
        entity.setResult(json);

        return entity;
    }


    @Data
    class RespEntity {
        private Integer errorCode;
        private String errorMessage;
        private Object result;
    }
}
