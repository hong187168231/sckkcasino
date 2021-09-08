package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.BankInfoService;
import com.qianyi.casinocore.service.BankcardsService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/bankcard")
@Api(tags = "银行卡")
public class BankCardsController {

    @Autowired
    private BankInfoService bankInfoService;

    @Autowired
    private BankcardsService bankcardsService;

    @Autowired
    private UserService userService;

    @GetMapping("/banklist")
    @ApiOperation("银行列表")
    @ResponseBody
    public ResponseEntity bankList() {
        return ResponseUtil.success(bankInfoService.findAll());
    }

    @GetMapping("/boundList")
    @ApiOperation("用户已绑定银行卡列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", required = true),
    })
    public ResponseEntity boundList(Long userId) {
        List<Bankcards> bankcardsList = bankcardsService.findBankcardsByUserId(userId);
        return ResponseUtil.success(bankcardsList);
    }

    /**
     * 可以灵活添加参数，满足后续添加的需求
     *
     * @param bankAccount
     * @param realName
     * @return
     */
    @GetMapping("/peggBankCard")
    @ApiOperation("银行卡/开户名反查")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bankAccount", value = "银行卡号", required = false),
            @ApiImplicitParam(name = "realName", value = "开户名", required = false),
    })
    public ResponseEntity peggBankCard(String bankAccount, String realName){
        if(LoginUtil.checkNull(bankAccount, realName)){
            return ResponseUtil.parameterNotNull();
        }
        Bankcards bankcards = new Bankcards();
        bankcards.setBankAccount(bankAccount);
        bankcards.setRealName(realName);
        List<Bankcards> bankcardsList = bankcardsService.findUserBank(bankcards);
        return ResponseUtil.success(bankcardsList);
    }

    @PostMapping("/bound")
    @ApiOperation("用户增加银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", required = true),
            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true),
            @ApiImplicitParam(name = "bankAccount", value = "银行账号", required = true),
            @ApiImplicitParam(name = "address", value = "开户地址", required = true),
            @ApiImplicitParam(name = "realName", value = "持卡人姓名")})
    public ResponseEntity bound(Long userId, String bankId, String bankAccount, String address, String realName){
        String checkParamFroBound = Bankcards.checkParamFroBound(realName, bankId, bankAccount, address);
        if (StringUtils.isNotEmpty(checkParamFroBound)) {
            return ResponseUtil.custom(checkParamFroBound);
        }

        //判断是否存在该用户
        User user = userService.findById(userId);
        if(user == null){
            return ResponseUtil.custom("不存在该会员");
        }

        if(isGreatThan6(userId)){
            return ResponseUtil.custom("已经超过6张银行卡");
        }

        Bankcards bankcards = boundCard(userId, bankId,bankAccount,address,realName);
        boolean isSuccess= bankcardsService.boundCard(bankcards)==null?true:false;
        return ResponseUtil.success(isSuccess);
    }

    private boolean isGreatThan6(Long userId) {
        int count = bankcardsService.countByUserId(userId);
        return count>=Constants.MAX_BANK_NUM;
    }

    @PostMapping("/disable")
    @ApiOperation("禁用/启用")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", required = true),
            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true),

    })
    public ResponseEntity disable(Long userId, String bankId){
        Bankcards bankcards = new Bankcards();
        bankcards.setUserId(userId);
        bankcards.setBankId(bankId);
        //查询银行卡
        List<Bankcards> bankcardsList = bankcardsService.findUserBank(bankcards);
        if(bankcardsList == null || bankcardsList.size()<= 0){
            return ResponseUtil.custom("用户未绑定银行卡");
        }
        Bankcards bank = bankcardsList.get(0);
        if(bank.getDisable() == Constants.BANK_CLOSE){
            bank.setDisable(Constants.BANK_OPEN);
        }else{
            bank.setDisable(Constants.BANK_CLOSE);
        }
        boolean isSuccess= bankcardsService.boundCard(bank)==null?true:false;
        return ResponseUtil.success(isSuccess);
    }


    private Bankcards boundCard(Long userId, String bankId, String bankAccount, String address, String realName){
        Bankcards firstBankcard = bankcardsService.findBankCardsInByUserId(userId);
        Date now = new Date();
        Bankcards bankcards = new Bankcards();
        bankcards.setUserId(userId);
        bankcards.setBankId(bankId);
        bankcards.setBankAccount(bankAccount);
        bankcards.setAddress(address);
        bankcards.setRealName(getRealName(firstBankcard,realName));
        bankcards.setUpdateTime(now);
        bankcards.setCreateTime(now);
        bankcards.setDisable(Constants.BANK_OPEN);
        bankcards.setDefaultCard(isFirstCard(firstBankcard));
        return bankcards;
    }

    private String getRealName(Bankcards bankcards, String realName){
        return bankcards==null?realName:bankcards.getRealName();
    }

    private Integer isFirstCard(Bankcards bankcards){
        return bankcards==null?0:1;
    }




}
