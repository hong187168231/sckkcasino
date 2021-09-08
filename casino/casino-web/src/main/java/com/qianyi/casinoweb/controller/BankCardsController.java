package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.service.BankInfoService;
import com.qianyi.casinocore.service.BankcardsService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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

    @GetMapping("/banklist")
    @ApiOperation("银行列表")
    @ResponseBody
    public ResponseEntity bankList() {
        return ResponseUtil.success(bankInfoService.findAll());
    }

    @GetMapping("/boundList")
    @ApiOperation("用户已绑定银行卡列表")
    @ResponseBody
    public ResponseEntity boundList() {
        Long userId =  CasinoWebUtil.getAuthId();
        List<Bankcards> bankcardsList = bankcardsService.findBankcardsByUserId(userId);
        return ResponseUtil.success(bankcardsList);
    }

    /**
     *  针对绑定银行卡接口的参数合法性校验
     * @param bankId
     * @param bankAccount
     * @param address
     * @return
     */
    public static String checkParamFroBound(String accountName,String bankId, String bankAccount,
                                            String address) {
        if(CasinoWebUtil.checkNull(accountName)){
            return "持卡人不能为空";
        }
        if (bankId == null) {
            return "银行id不能为空！";
        }
        if (CasinoWebUtil.checkNull(address)) {
            return "开户地址不能为空！";
        }
        if (CasinoWebUtil.checkNull(bankAccount)) {
            return "银行账号不能为空！";
        }
        if (bankAccount.length() > 20 || bankAccount.length() < 16) {
            return "长度只能在16~20位！";
        }
        return null;
    }

    @PostMapping("/bound")
    @ApiOperation("用户增加银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true),
            @ApiImplicitParam(name = "bankAccount", value = "银行账号", required = true),
            @ApiImplicitParam(name = "address", value = "开户地址", required = true),
            @ApiImplicitParam(name = "realName", value = "持卡人姓名")})
    public ResponseEntity bound(String bankId, String bankAccount, String address, String realName){
        String checkParamFroBound = this.checkParamFroBound(realName, bankId, bankAccount, address);
        if (CasinoWebUtil.checkNull(checkParamFroBound)) {
            return ResponseUtil.custom(checkParamFroBound);
        }

        if(isGreatThan6()){
            return ResponseUtil.custom("已经超过6张银行卡");
        }

        Bankcards bankcards = boundCard(bankId,bankAccount,address,realName);
        bankcards= bankcardsService.boundCard(bankcards);
        return ResponseUtil.success(bankcards);
    }

    private boolean isGreatThan6(){
        Long userId = CasinoWebUtil.getAuthId();
        int count = bankcardsService.countByUserId(userId);
        return count>=6;
    }

    private Bankcards boundCard(String bankId, String bankAccount, String address, String realName){
        Long userId = CasinoWebUtil.getAuthId();
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
        bankcards.setDisable(0);
        bankcards.setDefaultCard(isFirstCard(firstBankcard));
        return bankcards;
    }

    private String getRealName(Bankcards bankcards, String realName){
        return bankcards==null?realName:bankcards.getRealName();
    }

    private Integer isFirstCard(Bankcards bankcards){
        return bankcards==null?1:0;
    }




}
