package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.business.WithdrawBusiness;
import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
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
        boolean checkNull = CasinoWebUtil.checkNull(money, bankId, withdrawPwd);
        if(checkNull){
            return ResponseUtil.parameterNotNull();
        }
        //判断是否数字
        BigDecimal decMoney = withdrawBusiness.checkMoney(money);
        if(decMoney.compareTo(BigDecimal.valueOf(-1))==0){
            return ResponseUtil.custom("金额类型错误");
        }
        User user = withdrawBusiness.getUserById(CasinoWebUtil.getAuthId());

        if (user != null && ObjectUtils.isEmpty(user.getWithdrawPassword())) {
            return ResponseUtil.emptytWithdrawMoney();
        }
        boolean bcrypt = CasinoWebUtil.checkBcrypt(withdrawPwd, user.getWithdrawPassword());
        if (!bcrypt) {
            return ResponseUtil.custom("交易密码错误");
        }
        BigDecimal withdrawMoney = withdrawBusiness.getWithdrawMoneyByUserId(user.getId());
        //判断是否大于可提金额
        if (decMoney.compareTo(withdrawMoney) >= 0) {
            return ResponseUtil.custom("超过可提金额");
        }
//        String checkResult = withdrawBusiness.checkParams(withdrawPwd,decMoney,user);
//        if(!CasinoWebUtil.checkNull(checkResult)){
//            return ResponseUtil.custom(checkResult);
//        }
        //进行提币
        UserMoney userMoney = withdrawBusiness.processWithdraw(decMoney, bankId, CasinoWebUtil.getAuthId());

        return ResponseUtil.success(userMoney);
    }

    @PostMapping("/updateWithdrawPassword")
    @ApiOperation("当前登录用户修改取款密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "oldWithdrawPassword", value = "原取款密码", required = false),
            @ApiImplicitParam(name = "newWithdrawPassword", value = "新取款密码", required = true),
            @ApiImplicitParam(name = "confirmWithdrawPassword", value = "确认取款密码", required = true)})
    public ResponseEntity updateWithdrawPassword(String oldWithdrawPassword, String newWithdrawPassword, String confirmWithdrawPassword) {
        Long userId = CasinoWebUtil.getAuthId();
        User user = withdrawBusiness.getUserById(userId);
        if (user == null) {
            return ResponseUtil.custom("用户不存在");
        }
        boolean checkNull = CasinoWebUtil.checkNull(newWithdrawPassword, confirmWithdrawPassword);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        if (!newWithdrawPassword.equals(confirmWithdrawPassword)) {
            return ResponseUtil.custom("两次密码输入不一致");
        }
        if (!ObjectUtils.isEmpty(user.getWithdrawPassword())) {
            if (ObjectUtils.isEmpty(oldWithdrawPassword)) {
                return ResponseUtil.custom("原取款密码不允许为空");
            } else {
                boolean checkBcrypt = CasinoWebUtil.checkBcrypt(oldWithdrawPassword, user.getWithdrawPassword());
                if (!checkBcrypt) {
                    return ResponseUtil.custom("原取款密码填写错误");
                }
            }
        }
        String bcryptWithdrawPassword = CasinoWebUtil.bcrypt(newWithdrawPassword);
        user.setWithdrawPassword(bcryptWithdrawPassword);
        withdrawBusiness.save(user);
        return ResponseUtil.success();
    }


    @PostMapping("/updateLoginPassword")
    @ApiOperation("当前登录用户修改登录密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "oldLoginPassword", value = "原登录密码", required = true),
            @ApiImplicitParam(name = "newLoginPassword", value = "新登录密码", required = true),
            @ApiImplicitParam(name = "confirmLoginPassword", value = "确认登录密码", required = true)})
    public ResponseEntity updateLoginPassword(String oldLoginPassword, String newLoginPassword, String confirmLoginPassword) {
        Long userId = CasinoWebUtil.getAuthId();
        User user = withdrawBusiness.getUserById(userId);
        if (user == null) {
            return ResponseUtil.custom("用户不存在");
        }
        boolean checkNull = CasinoWebUtil.checkNull(oldLoginPassword, newLoginPassword, confirmLoginPassword);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        if (!newLoginPassword.equals(confirmLoginPassword)) {
            return ResponseUtil.custom("两次密码输入不一致");
        }
        boolean checkBcrypt = CasinoWebUtil.checkBcrypt(oldLoginPassword, user.getPassword());
        if(!checkBcrypt){
            return ResponseUtil.custom("原登录密码填写错误");
        }
        String bcryptLoginPassword = CasinoWebUtil.bcrypt(newLoginPassword);
        user.setPassword(bcryptLoginPassword);
        withdrawBusiness.save(user);
        return ResponseUtil.success();
    }
}
