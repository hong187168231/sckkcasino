package com.qianyi.casinoweb.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.business.ThirdGameBusiness;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.livegoldenf.api.PublicGoldenFApi;
import com.qianyi.livegoldenf.constants.LanguageEnum;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
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
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
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
    private PublicGoldenFApi goldenFApi;
    @Autowired
    private ThirdGameBusiness gameBusiness;
    @Autowired
    private PlatformGameService platformGameService;
    @Autowired
    private AdGamesService adGamesService;
    @Autowired
    @Qualifier("accountChangeJob")
    private AsyncService asyncService;
    @Value("${project.goldenf.currency:null}")
    private String currency;
    @Value("${project.ipWhite}")
    private String ipWhite;

    @ApiOperation("开游戏")
//    @RequestLimit(limit = 1, timeout = 5)
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
        //转出WM游戏的余额
        gameBusiness.oneKeyRecoverWm(authId);
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
            if (responseEntity != null) {
                return responseEntity;
            }
            //钱转入第三方后本地扣减，扣款
            userMoneyService.subMoney(authId, userCenterMoney);
            //记录账变
            saveAccountChange(authId, userCenterMoney, userMoney.getMoney(), userMoney.getMoney().subtract(userCenterMoney), 0, orderNo, "自动转入PG/CQ9");
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

    public void saveAccountChange(Long userId, BigDecimal amount, BigDecimal amountBefore, BigDecimal amountAfter, Integer type, String orderNo, String remark) {
        User user = userService.findById(userId);
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
        orderService.save(order);

        //账变中心记录账变
        AccountChangeVo vo = new AccountChangeVo();
        vo.setUserId(userId);
        vo.setChangeEnum(AccountChangeEnum.PG_CQ9_IN);
        vo.setAmount(amount.negate());
        vo.setAmountBefore(amountBefore);
        vo.setAmountAfter(amountAfter);
        asyncService.executeAsync(vo);
    }

    public ResponseEntity transferIn(String playerName, Long userId, double amonut, String orderNo) {
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
            return ResponseUtil.custom("服务器异常,请重新操作");
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

    @ApiOperation("查询当前登录用户PG/CQ9余额")
    @GetMapping("/getBalance")
    public ResponseEntity<BigDecimal> getBalance() {
        //获取登陆用户
        Long authId = CasinoWebUtil.getAuthId();
        UserThird third = userThirdService.findByUserId(authId);
        if (third == null || ObjectUtils.isEmpty(third.getGoldenfAccount())) {
            return ResponseUtil.success(BigDecimal.ZERO);
        }
        ResponseEntity<BigDecimal> responseEntity = gameBusiness.getBalanceGoldenF(third.getGoldenfAccount(), authId);
        return responseEntity;
    }

    @ApiOperation("查询用户PG/CQ9余额外部接口")
    @GetMapping("/getBalanceApi")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户ID", required = true)
    })
    public ResponseEntity<BigDecimal> getBalanceApi(Long userId) {
        log.info("开始查询PG/CQ9余额:userId={}", userId);
        if (!ipWhiteCheck()) {
            return ResponseUtil.custom("ip禁止访问");
        }
        if (CasinoWebUtil.checkNull(userId)) {
            return ResponseUtil.parameterNotNull();
        }
        UserThird third = userThirdService.findByUserId(userId);
        if (third == null || ObjectUtils.isEmpty(third.getGoldenfAccount())) {
            return ResponseUtil.custom("当前用户暂未进入过游戏");
        }
        ResponseEntity<BigDecimal> responseEntity = gameBusiness.getBalanceGoldenF(third.getGoldenfAccount(), userId);
        return responseEntity;
    }

    @ApiOperation("一键回收当前登录用户PG/CQ9余额")
    @Transactional
    @GetMapping("/oneKeyRecover")
    public ResponseEntity oneKeyRecover() {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        return gameBusiness.oneKeyRecoverGoldenF(userId);
    }

    @ApiOperation("一键回收用户PG/CQ9余额外部接口")
    @Transactional
    @GetMapping("/oneKeyRecoverApi")
    @NoAuthentication
    @ApiImplicitParam(name = "userId", value = "用户ID", required = true)
    public ResponseEntity oneKeyRecoverApi(Long userId) {
        if (!ipWhiteCheck()) {
            return ResponseUtil.custom("ip禁止访问");
        }
        return gameBusiness.oneKeyRecoverGoldenF(userId);
    }

    @ApiOperation("PG/CQ9游戏列表")
    @GetMapping("/gameList")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "vendorCode", value = "产品代码:PG/CQ9", required = true),
            @ApiImplicitParam(name = "gameName", value = "游戏名称", required = false),
    })
    @NoAuthentication
    public ResponseEntity<List<AdGame>> gameList(String vendorCode, String gameName) {
        if (CasinoWebUtil.checkNull(vendorCode)) {
            return ResponseUtil.parameterNotNull();
        }
        PlatformGame platformGame = platformGameService.findByGamePlatformName(vendorCode);
        if (platformGame == null) {
            return ResponseUtil.custom("当前产品不存在");
        }
        if (platformGame.getGameStatus() != Constants.open) {
            return ResponseUtil.custom("当前产品已下架");
        }
        if (ObjectUtils.isEmpty(gameName)) {
            List<AdGame> gameList = adGamesService.findByGamePlatformIdAndGamesStatusIsTrue(platformGame.getGamePlatformId());
            return ResponseUtil.success(gameList);
        }
        List<AdGame> gameList = adGamesService.findByGamePlatformIdAndGameNameAndGamesStatusIsTrue(platformGame.getGamePlatformId(), gameName);
        return ResponseUtil.success(gameList);
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
