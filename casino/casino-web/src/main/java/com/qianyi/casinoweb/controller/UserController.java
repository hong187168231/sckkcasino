package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.vo.UserVo;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("user")
@Api(tags = "用户中心")
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    UserMoneyService userMoneyService;

    @GetMapping("info")
    @ApiOperation("获取当前用户的基本信息")
    public ResponseEntity<UserVo> info() {
        Long authId = CasinoWebUtil.getAuthId();
        User user = userService.findById(authId);
        UserVo vo = new UserVo();
        vo.setUserId(user.getId());
        vo.setAccount(user.getAccount());
        vo.setName(user.getName());
        vo.setHeadImg(user.getHeadImg());
        //TODO 查询可提金额，未完成流水(打码量)
        UserMoney userMoney = userMoneyService.findByUserId(user.getId());
        BigDecimal defaultVal = BigDecimal.ZERO.setScale(2);
        if (userMoney == null) {
            vo.setUnfinshTurnover(defaultVal);
            vo.setDrawMoney(defaultVal);
            return new ResponseEntity(ResponseCode.SUCCESS, vo);
        }
        if (userMoney.getCodeNum() == null) {
            vo.setUnfinshTurnover(defaultVal);
            vo.setDrawMoney(defaultVal);
        }
        BigDecimal codeNum = userMoney.getCodeNum();
        vo.setUnfinshTurnover(codeNum.setScale(2, BigDecimal.ROUND_HALF_UP));
        //打码量为0时才有可提现金额
        if (codeNum.compareTo(BigDecimal.ZERO) < 1) {
            BigDecimal money = userMoney.getMoney() == null ? defaultVal : userMoney.getMoney();
            vo.setDrawMoney(money.setScale(2, BigDecimal.ROUND_HALF_UP));
        } else {
            vo.setDrawMoney(defaultVal);
        }
//        return ResponseUtil.success(vo);
        return new ResponseEntity(ResponseCode.SUCCESS, vo);
    }
}
