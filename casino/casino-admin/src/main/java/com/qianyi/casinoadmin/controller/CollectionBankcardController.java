package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.BankInfo;
import com.qianyi.casinocore.model.CollectionBankcard;
import com.qianyi.casinocore.service.CollectionBankcardService;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户谷歌验证登录
 */
@Api(tags = "资金中心")
@RestController
@RequestMapping("collection")
public class CollectionBankcardController {

    @Autowired
    private CollectionBankcardService collectionBankcardService;

    @GetMapping("bankList")
    @ApiOperation("收款银行卡列表")
    @ResponseBody
    public ResponseEntity bankList() {
        return ResponseUtil.success(collectionBankcardService.getCollectionBandcards());
    }

    @ApiOperation("新增收款银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bankNo", value = "银行账号", required = true),
            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true),
            @ApiImplicitParam(name = "accountName", value = "开户名", required = true),
            @ApiImplicitParam(name = "disable", value = "0:未禁用 1：禁用", required = true)
    })
    @PostMapping("saveBankInfo")
    public ResponseEntity saveBankInfo(String bankNo, String bankId, String accountName, Integer disable){
        if(disable < Constants.BANK_OPEN || disable > Constants.BANK_CLOSE){
            return ResponseUtil.custom("参数不合法");
        }
        CollectionBankcard collectionBankcard = collectionBankcardService.findByBankNo(bankNo);
        if(collectionBankcard != null){
            return ResponseUtil.custom("银行卡已存在");
        }
        CollectionBankcard bankcard = new CollectionBankcard();
        bankcard.setBankNo(bankNo);
        bankcard.setBankId(bankId);
        bankcard.setAccountName(accountName);
        bankcard.setDisable(disable);
        collectionBankcardService.save(bankcard);
        return ResponseUtil.success(bankcard);
    }

    @ApiOperation("修改收款银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
            @ApiImplicitParam(name = "bankNo", value = "银行账号", required = false),
            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = false),
            @ApiImplicitParam(name = "accountName", value = "开户名", required = false),
            @ApiImplicitParam(name = "disable", value = "0:未禁用 1：禁用", required = false)
    })
    @PostMapping("updateBankInfo")
    public ResponseEntity updateBankInfo(Long id, String bankNo, String bankId, String accountName, Integer disable){
        if(disable != null && (disable < Constants.BANK_OPEN || disable > Constants.BANK_CLOSE)){
            return ResponseUtil.custom("参数不合法");
        }
        CollectionBankcard collectionBankcard = collectionBankcardService.findById(id);
        if(collectionBankcard == null){
            return ResponseUtil.custom("银行卡不存在");
        }

        if(LoginUtil.checkNull(bankNo)){
           collectionBankcard.setBankNo(bankNo);
        }
        if(LoginUtil.checkNull(bankId)){
           collectionBankcard.setBankId(bankId);
        }
        if(LoginUtil.checkNull(accountName)){
           collectionBankcard.setAccountName(accountName);
        }
        if(disable != null){
           collectionBankcard.setDisable(disable);
        }
        collectionBankcardService.save(collectionBankcard);
        return ResponseUtil.success(collectionBankcard);
    }

    @ApiOperation("修改银行卡状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true)
    })
    @PostMapping("updateDisable")
    public ResponseEntity updateDisable(Long id){
        CollectionBankcard collectionBankcard = collectionBankcardService.findById(id);
        if(collectionBankcard == null){
            return ResponseUtil.custom("银行卡不存在");
        }
        if(collectionBankcard.getDisable() == Constants.BANK_OPEN){
            collectionBankcard.setDisable(Constants.BANK_CLOSE);
        }else{
            collectionBankcard.setDisable(Constants.BANK_OPEN);
        }
        collectionBankcardService.save(collectionBankcard);
        return ResponseUtil.success(collectionBankcard);
    }


}
