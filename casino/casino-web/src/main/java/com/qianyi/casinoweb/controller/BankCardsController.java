package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.BankInfo;
import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.service.BankInfoService;
import com.qianyi.casinocore.service.BankcardsService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/bankcard")
@Api(tags = "用户中心")
public class BankCardsController {

    @Autowired
    private BankInfoService bankInfoService;

    @Autowired
    private BankcardsService bankcardsService;

    @GetMapping("/banklist")
    @ApiOperation("银行列表")
    @ResponseBody
    public ResponseEntity bankList() {
        BankInfo bankInfo=new BankInfo();
        bankInfo.setDisable(0);
        return ResponseUtil.success(bankInfoService.findAll(bankInfo));
    }

//    @GetMapping("/boundList")
//    @ApiOperation("用户已绑定银行卡列表")
//    @ResponseBody
//    public ResponseEntity boundList() {
//        Long userId =  CasinoWebUtil.getAuthId();
//        List<Bankcards> bankcardsList = bankcardsService.findBankcardsByUserId(userId);
//        return ResponseUtil.success(bankcardsList);
//    }

    @PostMapping("/bound")
    @ApiOperation("用户增加银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true),
            @ApiImplicitParam(name = "bankAccount", value = "银行账号", required = true),
            @ApiImplicitParam(name = "address", value = "开户地址", required = true),
            @ApiImplicitParam(name = "realName", value = "持卡人姓名")})
    public ResponseEntity bound(String bankId, String bankAccount, String address, String realName){
        String checkParamFroBound = Bankcards.checkParamFroBound(realName, bankId, bankAccount, address);
        if (StringUtils.hasLength(checkParamFroBound)) {
            return ResponseUtil.custom(checkParamFroBound);
        }

        if(isGreatThan6()){
            return ResponseUtil.custom("已经超过6张银行卡");
        }

        Bankcards bankcards = boundCard(bankId,bankAccount,address,realName);
        bankcards= bankcardsService.boundCard(bankcards);
        return ResponseUtil.success(bankcards);
    }

    /**
     * 根据ID删除银行卡
     * @param id 银行卡id
     * @return
     */
    @GetMapping("/deleteBankCardById")
    @ApiOperation("根据ID删除银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "银行卡id", required = true),
    })
    public ResponseEntity deleteBankCardById(Long id) {
        if (CasinoWebUtil.checkNull(id)) {
            return ResponseUtil.parameterNotNull();
        }
        Bankcards bankcards = bankcardsService.findById(id);
        if (bankcards == null) {
            return ResponseUtil.custom("当前银行卡不存在");
        }
        Long authId = CasinoWebUtil.getAuthId();
        if (!authId.equals(bankcards.getUserId())) {
            return ResponseUtil.custom("当前银行卡不属于登录用户");
        }
        bankcardsService.deleteBankCardById(id);
        //如果删除的是默认银行卡,重新再设置一张卡为默认卡
        if (!ObjectUtils.isEmpty(bankcards.getDefaultCard()) && bankcards.getDefaultCard() == 1) {
            List<Bankcards> bankcardsList = bankcardsService.findBankcardsByUserId(authId);
            if (!CollectionUtils.isEmpty(bankcardsList)) {
                Bankcards bankcards1 = bankcardsList.get(0);
                bankcards1.setDefaultCard(1);
                bankcardsService.updateBankCards(bankcards1);
            }
        }
        return ResponseUtil.success();
    }

    /**
     * 根据ID设置默认银行卡
     * @param id 银行卡id
     * @return
     */
    @PostMapping("/setDefaultBankCardById")
    @ApiOperation("根据ID设置默认银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "银行卡id", required = true),
    })
    public ResponseEntity setDefaultBankCardById(Long id) {
        if (CasinoWebUtil.checkNull(id)) {
            return ResponseUtil.parameterNotNull();
        }
        Bankcards bankcards = bankcardsService.findById(id);
        if (bankcards == null) {
            return ResponseUtil.custom("当前银行卡不存在");
        }
        Long authId = CasinoWebUtil.getAuthId();
        if (!authId.equals(bankcards.getUserId())) {
            return ResponseUtil.custom("当前银行卡不属于登录用户");
        }
        Bankcards defaultBankcards = bankcardsService.findByUserIdAndDefaultCard(authId, 1);
        if (defaultBankcards != null) {
            if (defaultBankcards.getId().equals(bankcards.getId())) {
                return ResponseUtil.success();
            }
            defaultBankcards.setDefaultCard(0);
            bankcardsService.updateBankCards(defaultBankcards);
        }
        bankcards.setDefaultCard(1);
        bankcardsService.updateBankCards(bankcards);
        return ResponseUtil.success();
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
