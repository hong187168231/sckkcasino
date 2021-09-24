package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.CommonConst;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "disable", value = "0:未禁用 1：禁用", required = false),
            @ApiImplicitParam(name = "bankId", value = "银行类型", required = false),
    })
    public ResponseEntity bankList(Integer pageSize, Integer pageCode,Integer disable,String bankId) {
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        CollectionBankcard collectionBankcard = new CollectionBankcard();
        collectionBankcard.setDisable(disable);
        collectionBankcard.setBankId(bankId);
        Page<CollectionBankcard> collectionBandPage = collectionBankcardService.getCollectionBandPage(collectionBankcard, pageable);
        return ResponseUtil.success(collectionBandPage);
    }

    @ApiOperation("新增收款银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bankNo", value = "银行账号", required = true),
            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true),
            @ApiImplicitParam(name = "accountName", value = "开户名", required = true),
    })
    @PostMapping("saveBankInfo")
    public ResponseEntity saveBankInfo(String bankNo, String bankId, String accountName){
        CollectionBankcard byBankNo = collectionBankcardService.findByBankNo(bankNo);
        if (!LoginUtil.checkNull(byBankNo)){
            return ResponseUtil.custom("银行卡已存在");
        }
        CollectionBankcard bankcard = new CollectionBankcard();
        bankcard.setBankNo(bankNo);
        bankcard.setBankId(bankId);
        bankcard.setAccountName(accountName);
        bankcard.setDisable(CommonConst.NUMBER_1);//新增默认禁用
        collectionBankcardService.save(bankcard);
        return ResponseUtil.success(bankcard);
    }

    @ApiOperation("修改收款银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
            @ApiImplicitParam(name = "bankNo", value = "银行账号", required = false),
            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = false),
            @ApiImplicitParam(name = "accountName", value = "开户名", required = false),
    })
    @PostMapping("updateBankInfo")
    public ResponseEntity updateBankInfo(Long id, String bankNo, String bankId, String accountName){
        CollectionBankcard collectionBankcard = collectionBankcardService.findById(id);
        if(collectionBankcard == null){
            return ResponseUtil.custom("银行卡不存在");
        }
        if(!LoginUtil.checkNull(bankNo)){
            CollectionBankcard byBankNo = collectionBankcardService.findByBankNo(bankNo);
            if (!LoginUtil.checkNull(byBankNo)){
                return ResponseUtil.custom("银行卡已存在");
            }
           collectionBankcard.setBankNo(bankNo);
        }
        if(!LoginUtil.checkNull(bankId)){
           collectionBankcard.setBankId(bankId);
        }
        if(!LoginUtil.checkNull(accountName)){
           collectionBankcard.setAccountName(accountName);
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
        if(LoginUtil.checkNull(collectionBankcard)){
            return ResponseUtil.custom("银行卡不存在");
        }
        return this.checkNumber(collectionBankcard);
    }

    public synchronized ResponseEntity checkNumber( CollectionBankcard collectionBankcard){
        if(collectionBankcard.getDisable() == Constants.BANK_OPEN){
            collectionBankcard.setDisable(Constants.BANK_CLOSE);
        }else{
            CollectionBankcard collection = new CollectionBankcard();
            collection.setDisable(Constants.BANK_OPEN);
            List<CollectionBankcard> all = collectionBankcardService.findAll(collection);
            if (all.size() >= CommonConst.NUMBER_2){
                return ResponseUtil.custom("收款卡最多上架两张");
            }
            collectionBankcard.setDisable(Constants.BANK_OPEN);
        }
        collectionBankcardService.save(collectionBankcard);
        return ResponseUtil.success(collectionBankcard);
    }
}
