package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.math.BigDecimal;

@RestController
@RequestMapping("shareProfit")
@Api(tags = "代理中心")
public class ShareProfitController {

    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    @Qualifier("accountChangeJob")
    AsyncService asyncService;

    @ApiOperation("用户领取分润金额")
    @GetMapping("/receiveShareProfit")
    @Transactional
    public ResponseEntity receiveWashCode() {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        if (userMoney == null) {
            return ResponseUtil.custom("用户钱包不存在");
        }
        if (userMoney.getShareProfit() == null) {
            userMoney.setShareProfit(BigDecimal.ZERO);
        }
        if (userMoney.getWashCode().compareTo(BigDecimal.ONE) == -1) {
            return ResponseUtil.custom("金额小于1,不能领取");
        }
        userMoneyService.addMoney(userId, userMoney.getShareProfit());
        userMoneyService.subShareProfit(userId, userMoney.getShareProfit());

        AccountChangeVo vo=new AccountChangeVo();
        vo.setUserId(userId);
        vo.setChangeEnum(AccountChangeEnum.SHARE_PROFIT);
        vo.setAmount(userMoney.getShareProfit());
        vo.setAmountBefore(userMoney.getMoney());
        vo.setAmountAfter(userMoney.getMoney().add(userMoney.getShareProfit()));
        asyncService.executeAsync(vo);
        return ResponseUtil.success();
    }
}
