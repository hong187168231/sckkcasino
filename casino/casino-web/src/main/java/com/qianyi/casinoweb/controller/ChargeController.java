package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.business.ChargeBusiness;
import com.qianyi.casinocore.model.ChargeOrder;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/charge")
@Api(tags = "银行卡线下充值")
public class ChargeController {

    @Autowired
    private ChargeBusiness chargeBusiness;

    @GetMapping("/collect_bankcards")
    @ApiOperation("收款银行卡列表")
    @ResponseBody
    public ResponseEntity bankList(){
        return ResponseUtil.success(chargeBusiness.getCollectionBankcards());
    }

    @PostMapping("/submit")
    @ApiOperation("提交充值")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "chargeAmount", value = "充值金额", required = true),
            @ApiImplicitParam(name = "remitType", value = "汇款方式，银行卡1，支付宝2，微信3", required = true),
            @ApiImplicitParam(name = "remitterName", value = "汇款人", required = true),
    })
    public ResponseEntity submitCharge(String chargeAmount,Integer remitType,String remitterName){
        if (CasinoWebUtil.checkNull(chargeAmount,remitType,remitterName)) {
            return ResponseUtil.parameterNotNull();
        }
        ResponseEntity responseEntity = chargeBusiness.submitOrder(chargeAmount, remitType, remitterName, CasinoWebUtil.getAuthId());
        return responseEntity;
    }
}
