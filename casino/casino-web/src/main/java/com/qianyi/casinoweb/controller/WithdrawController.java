package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.business.WithdrawBusiness;
import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Api(tags = "用户中心")
@RestController
@RequestMapping("withdraw")
public class WithdrawController {

    @Autowired
    private WithdrawBusiness withdrawBusiness;

    @GetMapping("/money")
    @ApiOperation("获取提币金额")
    @ResponseBody
    public ResponseEntity getWithdrawMoney(){
        Long userId = CasinoWebUtil.getAuthId();
        return ResponseUtil.success(withdrawBusiness.getWithdrawFullMoney(userId));
    }

    @GetMapping("/banklist")
    @ApiOperation("获取用户已绑定银行卡")
    @ResponseBody
    public ResponseEntity getWithdrawBank(){
        Long userId = CasinoWebUtil.getAuthId();
        List<Map<String,Object>> bankcardsList = withdrawBusiness.getWithdrawBankcardsList(userId);
        return ResponseUtil.success(bankcardsList);
    }

    @PostMapping("/submit")
    @ApiOperation("提款提交")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "money", value = "提款金额", required = true),
            @ApiImplicitParam(name = "bankId", value = "银行id", required = true),
            @ApiImplicitParam(name = "withdrawPwd", value = "提币密码", required = true)})
    public ResponseEntity withdraw(String money, String bankId,String withdrawPwd){

        //判断是否数字
        BigDecimal decMoney = withdrawBusiness.checkMoney(money);
        if(decMoney.compareTo(BigDecimal.valueOf(-1))==0){
            return ResponseUtil.custom("金额类型错误");
        }
        User user = withdrawBusiness.getUserById(CasinoWebUtil.getAuthId());

        if (user != null && ObjectUtils.isEmpty(user.getWithdrawMoney())) {
            return ResponseUtil.emptytWithdrawMoney();
        }
        boolean bcrypt = CasinoWebUtil.checkBcrypt(withdrawPwd, user.getWithdrawPassword());
        if (!bcrypt) {
            return ResponseUtil.custom("交易密码错误");
        }
        if (ObjectUtils.isEmpty(user.getWithdrawMoney())) {
            return ResponseUtil.custom("没有可提现额度");
        }
        //判断是否大于可提金额
        if (decMoney.compareTo(user.getWithdrawMoney()) >= 0) {
            return ResponseUtil.custom("超过可提金额");
        }
//        String checkResult = withdrawBusiness.checkParams(withdrawPwd,decMoney,user);
//        if(!CasinoWebUtil.checkNull(checkResult)){
//            return ResponseUtil.custom(checkResult);
//        }
        //进行提币
        user = withdrawBusiness.processWithdraw(decMoney, bankId, CasinoWebUtil.getAuthId());

        return ResponseUtil.success(user);
    }
}
