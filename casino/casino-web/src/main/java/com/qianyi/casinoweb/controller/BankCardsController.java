//package com.qianyi.casinoweb.controller;
//
//import com.qianyi.casinocore.model.BankcardsCustomer;
//import com.qianyi.casinocore.service.BankInfoService;
//import com.qianyi.casinocore.service.BankcardsService;
//import com.qianyi.casinoweb.util.CasinoWebUtil;
//import com.qianyi.modulecommon.reponse.ResponseEntity;
//import com.qianyi.modulecommon.reponse.ResponseUtil;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiImplicitParam;
//import io.swagger.annotations.ApiImplicitParams;
//import io.swagger.annotations.ApiOperation;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Date;
//import java.util.List;
//
//@RestController
//@RequestMapping("/bankcard")
//@Api(tags = "银行卡")
//public class BankCardsController {
//
//    @Autowired
//    private BankInfoService bankInfoService;
//
//    @Autowired
//    private BankcardsService bankcardsService;
//
//    @GetMapping("/banklist")
//    @ApiOperation("银行列表")
//    @ResponseBody
//    public ResponseEntity bankList() {
//        return ResponseUtil.success(bankInfoService.findAll());
//    }
//
//    @GetMapping("/boundList")
//    @ApiOperation("用户已绑定银行卡列表")
//    @ResponseBody
//    public ResponseEntity boundList() {
//        Long userId =  CasinoWebUtil.getAuthId();
//        List<Bankcards> bankcardsList = bankcardsService.findBankcardsByUserId(userId);
//        return ResponseUtil.success(bankcardsList);
//    }
//
//    @PostMapping("/bound")
//    @ApiOperation("用户增加银行卡")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true),
//            @ApiImplicitParam(name = "bankAccount", value = "银行账号", required = true),
//            @ApiImplicitParam(name = "address", value = "开户地址", required = true),
//            @ApiImplicitParam(name = "realName", value = "持卡人姓名")})
//    public ResponseEntity bound(Long bankId, String bankAccount, String address, String realName){
//        String checkParamFroBound = Bankcards.checkParamFroBound(realName, bankId, bankAccount, address);
//        if (StringUtils.isNotEmpty(checkParamFroBound)) {
//            return ResponseUtil.custom(checkParamFroBound);
//        }
//
//        if(isGreatThan6()){
//            return ResponseUtil.custom("已经超过6张银行卡");
//        }
//
//        Bankcards bankcards = boundCard(bankId,bankAccount,address,realName);
//        bankcards= bankcardsService.boundCard(bankcards);
//        return ResponseUtil.success(bankcards);
//    }
//
//    private boolean isGreatThan6(){
//        Long userId = CasinoWebUtil.getAuthId();
//        int count = bankcardsService.countByUserId(userId);
//        return count>=6;
//    }
//
//    private Bankcards boundCard(Long bankId, String bankAccount, String address, String realName){
//        Long userId = CasinoWebUtil.getAuthId();
//        Bankcards firstBankcard = bankcardsService.findBankCardsInByUserId(userId);
//        Date now = new Date();
//        Bankcards bankcards = new Bankcards();
//        bankcards.setUserId(userId);
//        bankcards.setBankId(bankId);
//        bankcards.setBankAccount(bankAccount);
//        bankcards.setAddress(address);
//        bankcards.setRealName(getRealName(firstBankcard,realName));
//        bankcards.setUpdateTime(now);
//        bankcards.setCreateTime(now);
//        bankcards.setDisable(0);
//        bankcards.setDefaultCard(isFirstCard(firstBankcard));
//        return bankcards;
//    }
//
//    private String getRealName(Bankcards bankcards, String realName){
//        return bankcards==null?realName:bankcards.getRealName();
//    }
//
//    private Integer isFirstCard(Bankcards bankcards){
//        return bankcards==null?1:0;
//    }
//
//
//
//
//}
