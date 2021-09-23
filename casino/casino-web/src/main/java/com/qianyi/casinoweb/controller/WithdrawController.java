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
        if(decMoney.compareTo(BigDecimal.ZERO)<1){//不能小于等于0
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
//        String checkResult = withdrawBusiness.checkParams(withdrawPwd,decMoney,user);
//        if(!CasinoWebUtil.checkNull(checkResult)){
//            return ResponseUtil.custom(checkResult);
//        }
        //进行提币
        ResponseEntity responseEntity = withdrawBusiness.processWithdraw(decMoney, bankId, CasinoWebUtil.getAuthId());

        return responseEntity;
    }

    @PostMapping("/updateWithdrawPassword")
    @ApiOperation("当前登录用户修改取款密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "oldWithdrawPassword", value = "原取款密码", required = false),
            @ApiImplicitParam(name = "newsWithdrawPassword", value = "新取款密码", required = true),
            @ApiImplicitParam(name = "confirmWithdrawPassword", value = "确认取款密码", required = true)})
    public ResponseEntity updateWithdrawPassword(String oldWithdrawPassword, String newsWithdrawPassword, String confirmWithdrawPassword) {
        Long userId = CasinoWebUtil.getAuthId();
        User user = withdrawBusiness.getUserById(userId);
        if (user == null) {
            return ResponseUtil.custom("用户不存在");
        }
        boolean checkNull = CasinoWebUtil.checkNull(newsWithdrawPassword, confirmWithdrawPassword);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        if (!newsWithdrawPassword.equals(confirmWithdrawPassword)) {
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
        String bcryptWithdrawPassword = CasinoWebUtil.bcrypt(newsWithdrawPassword);
        user.setWithdrawPassword(bcryptWithdrawPassword);
        withdrawBusiness.save(user);
        return ResponseUtil.success();
    }


    @PostMapping("/updateLoginPassword")
    @ApiOperation("当前登录用户修改登录密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "oldLoginPassword", value = "原登录密码", required = true),
            @ApiImplicitParam(name = "newsLoginPassword", value = "新登录密码", required = true),
            @ApiImplicitParam(name = "confirmLoginPassword", value = "确认登录密码", required = true)})
    public ResponseEntity updateLoginPassword(String oldLoginPassword, String newsLoginPassword, String confirmLoginPassword) {
        Long userId = CasinoWebUtil.getAuthId();
        User user = withdrawBusiness.getUserById(userId);
        if (user == null) {
            return ResponseUtil.custom("用户不存在");
        }
        boolean checkNull = CasinoWebUtil.checkNull(oldLoginPassword, newsLoginPassword, confirmLoginPassword);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        if (!newsLoginPassword.equals(confirmLoginPassword)) {
            return ResponseUtil.custom("两次密码输入不一致");
        }
        boolean checkBcrypt = CasinoWebUtil.checkBcrypt(oldLoginPassword, user.getPassword());
        if(!checkBcrypt){
            return ResponseUtil.custom("原登录密码填写错误");
        }
        String bcryptLoginPassword = CasinoWebUtil.bcrypt(newsLoginPassword);
        user.setPassword(bcryptLoginPassword);
        withdrawBusiness.save(user);
        return ResponseUtil.success();
    }
}
