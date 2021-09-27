package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.CommonConst;
import com.qianyi.casinoadmin.util.CommonUtil;
import com.qianyi.casinoadmin.util.LoginUtil;
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

import java.math.BigDecimal;
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
            @ApiImplicitParam(name = "grade", value = "渠道等级 1A 2B 3C 4D", required = true),
            @ApiImplicitParam(name = "nature", value = "性质 1对公账号 2个人账号", required = true),
            @ApiImplicitParam(name = "attribute", value = "使用属性 1常用卡 2备用卡", required = true),
            @ApiImplicitParam(name = "dayMaxAmount", value = "单日最大收款", required = false),
            @ApiImplicitParam(name = "monthMaxAmount", value = "单月最大收款", required = false),
    })
    @PostMapping("saveBankInfo")
    public ResponseEntity saveBankInfo(String bankNo, String bankId, String accountName,Integer grade,Integer nature,
                                       Integer attribute,String dayMaxAmount,String monthMaxAmount){
        CollectionBankcard byBankNo = collectionBankcardService.findByBankNo(bankNo);
        if (!LoginUtil.checkNull(byBankNo)){
            return ResponseUtil.custom("银行卡已存在");
        }
        CollectionBankcard bankcard = new CollectionBankcard();
        bankcard.setBankNo(bankNo);
        bankcard.setBankId(bankId);
        bankcard.setAccountName(accountName);
        bankcard.setGrade(grade);
        bankcard.setNature(nature);
        bankcard.setAttribute(attribute);
        if (!LoginUtil.checkNull(dayMaxAmount)){
            BigDecimal money = CommonUtil.checkMoney(dayMaxAmount);
            if(money.compareTo(BigDecimal.ZERO)<1){
                return ResponseUtil.custom("金额类型错误");
            }
            bankcard.setDayMaxAmount(money);
        }
        if (!LoginUtil.checkNull(monthMaxAmount)){
            BigDecimal money = CommonUtil.checkMoney(monthMaxAmount);
            if(money.compareTo(BigDecimal.ZERO)<1){
                return ResponseUtil.custom("金额类型错误");
            }
            bankcard.setDayMaxAmount(money);
        }
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
            @ApiImplicitParam(name = "dayMaxAmount", value = "单日最大收款", required = false),
            @ApiImplicitParam(name = "monthMaxAmount", value = "单月最大收款", required = false),
            @ApiImplicitParam(name = "grade", value = "渠道等级 1A 2B 3C 4D", required = false),
            @ApiImplicitParam(name = "attribute", value = "使用属性 1常用卡 2备用卡", required = false),
    })
    @PostMapping("updateBankInfo")
    public ResponseEntity updateBankInfo(Long id, String bankNo, String bankId, String accountName,String dayMaxAmount,
                                         String monthMaxAmount,Integer grade,Integer attribute){
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
        if(!LoginUtil.checkNull(grade)){
            collectionBankcard.setGrade(grade);
        }
        if(!LoginUtil.checkNull(attribute)){
            collectionBankcard.setAttribute(attribute);
        }

        if(!LoginUtil.checkNull(accountName)){
            collectionBankcard.setAccountName(accountName);
        }
        if(!LoginUtil.checkNull(dayMaxAmount)){
            BigDecimal bigDecimal = CommonUtil.checkMoney(dayMaxAmount);
            if(bigDecimal.compareTo(BigDecimal.ZERO)<1){//不能小于等于0
                return ResponseUtil.custom("金额类型错误");
            }
            collectionBankcard.setDayMaxAmount(bigDecimal);
        }
        if(!LoginUtil.checkNull(monthMaxAmount)){
            BigDecimal bigDecimal = CommonUtil.checkMoney(monthMaxAmount);
            if(bigDecimal.compareTo(BigDecimal.ZERO)<1){//不能小于等于0
                return ResponseUtil.custom("金额类型错误");
            }
            collectionBankcard.setMonthMaxAmount(bigDecimal);
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
            if (all.size() >= CommonConst.NUMBER_10){
                return ResponseUtil.custom("收款卡最多上架十张张");
            }
            collectionBankcard.setDisable(Constants.BANK_OPEN);
        }
        collectionBankcardService.save(collectionBankcard);
        return ResponseUtil.success(collectionBankcard);
    }
}
