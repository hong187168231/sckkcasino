package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.RebateConfig;
import com.qianyi.casinocore.service.RebateConfigService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/proxyRebate")
@Api(tags = "运营中心")
public class RebateConfigController {

    @Autowired
    private RebateConfigService rebateConfigService;

    @ApiOperation("查询全局代理返佣等级配置")
    @GetMapping("/findAll")
    public ResponseEntity<RebateConfig> findRegisterSwitchVo(){
        RebateConfig first = rebateConfigService.findFirst();
        return new ResponseEntity(ResponseCode.SUCCESS, first);
    }

    @ApiOperation("编辑全局代理返佣等级配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "level", value = "返佣等级", required = true),
            @ApiImplicitParam(name = "money", value = "业绩额度(整数)", required = true),
            @ApiImplicitParam(name = "profit", value = "返佣比例", required = true),
    })
    @PostMapping("/updateProxyRebate")
    public ResponseEntity updateRegisterSwitch(Integer level, Integer money, BigDecimal profit){
        if (DateUtil.verifyTime()){
            return ResponseUtil.custom("0点到1点不能修改该配置");
        }
        if (LoginUtil.checkNull(level,money,profit)){
            return ResponseUtil.custom("参数必填");
        }
        if(level < 1 || level > 8 || money < 0 || profit.compareTo(BigDecimal.ZERO) < 0 || profit.compareTo(BigDecimal.valueOf(30l)) > 0){
            return ResponseUtil.custom("参数不合法");
        }
        RebateConfig rebateConfig = rebateConfigService.findFirst();
        if (LoginUtil.checkNull(rebateConfig)){
            rebateConfig = new RebateConfig();
        }
        if(level == CommonConst.NUMBER_1){
            rebateConfig.setFirstMoney(money);
            rebateConfig.setFirstProfit(profit);
        }
        if(level == CommonConst.NUMBER_2){
            rebateConfig.setSecondMoney(money);
            rebateConfig.setSecondProfit(profit);
        }
        if(level == CommonConst.NUMBER_3){
            rebateConfig.setThirdMoney(money);
            rebateConfig.setThirdProfit(profit);
        }
        if(level == CommonConst.NUMBER_4){
            rebateConfig.setFourMoney(money);
            rebateConfig.setFourProfit(profit);
        }
        if(level == CommonConst.NUMBER_5){
            rebateConfig.setFiveMoney(money);
            rebateConfig.setFiveProfit(profit);
        }
        if(level == CommonConst.NUMBER_6){
            rebateConfig.setSixMoney(money);
            rebateConfig.setSixProfit(profit);
        }
        if(level == CommonConst.NUMBER_7){
            rebateConfig.setSevenMoney(money);
            rebateConfig.setSevenProfit(profit);
        }
        if(level == CommonConst.NUMBER_8){
            rebateConfig.setEightMoney(money);
            rebateConfig.setEightProfit(profit);
        }
        if (this.verify(rebateConfig)){
            return ResponseUtil.custom("返佣不能大于30块");
        }
        if (this.verifySize(rebateConfig)){
            return ResponseUtil.custom("低级别值不能大于高级别");
        }
        rebateConfigService.save(rebateConfig);
        return ResponseUtil.success();
    }
    private Boolean verify(RebateConfig rebateConfig){
        if (!LoginUtil.checkNull(rebateConfig.getFirstProfit()) && rebateConfig.getFirstProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(rebateConfig.getSecondProfit()) && rebateConfig.getSecondProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(rebateConfig.getThirdProfit()) && rebateConfig.getThirdProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(rebateConfig.getFourProfit()) && rebateConfig.getFourProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(rebateConfig.getFiveProfit()) && rebateConfig.getFiveProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(rebateConfig.getSixProfit()) && rebateConfig.getSixProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(rebateConfig.getSevenProfit()) && rebateConfig.getSevenProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(rebateConfig.getEightProfit()) && rebateConfig.getEightProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        return false;
    }
    private Boolean verifySize(RebateConfig rebateConfig){
        if (rebateConfig.getFirstProfit().compareTo(rebateConfig.getSecondProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getFirstMoney() >= rebateConfig.getSecondMoney() ){
            return true;
        }
        if (rebateConfig.getSecondProfit().compareTo(rebateConfig.getThirdProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getSecondMoney() >= rebateConfig.getThirdMoney() ){
            return true;
        }
        if (rebateConfig.getThirdProfit().compareTo(rebateConfig.getFourProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getThirdMoney() >= rebateConfig.getFourMoney() ){
            return true;
        }
        if (rebateConfig.getFourProfit().compareTo(rebateConfig.getFiveProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getFourMoney() >= rebateConfig.getFiveMoney() ){
            return true;
        }
        if (rebateConfig.getFiveProfit().compareTo(rebateConfig.getSixProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getFiveMoney() >= rebateConfig.getSixMoney() ){
            return true;
        }
        if (rebateConfig.getSixProfit().compareTo(rebateConfig.getSevenProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getSixMoney() >= rebateConfig.getSevenMoney() ){
            return true;
        }
        if (rebateConfig.getSevenProfit().compareTo(rebateConfig.getEightProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getSevenMoney() >= rebateConfig.getEightMoney() ){
            return true;
        }
        return false;
    }
}
