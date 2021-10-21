package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.ProxyRebateConfig;
import com.qianyi.casinocore.service.ProxyRebateConfigService;
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

@RestController
@RequestMapping("/proxyRebate")
@Api(tags = "运营中心")
public class ProxyRebateConfigController {

    @Autowired
    private ProxyRebateConfigService proxyRebateConfigService;

    @ApiOperation("查询代理返佣等级配置")
    @GetMapping("/findAll")
    public ResponseEntity<ProxyRebateConfig> findRegisterSwitchVo(){
        ProxyRebateConfig first = proxyRebateConfigService.findFirst();
        return new ResponseEntity(ResponseCode.SUCCESS, first);
    }

    @ApiOperation("编辑代理返佣等级配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "level", value = "返佣等级", required = true),
            @ApiImplicitParam(name = "money", value = "业绩额度(整数)", required = true),
            @ApiImplicitParam(name = "profit", value = "返佣比例", required = true),
    })
    @PostMapping("/updateProxyRebate")
    public ResponseEntity updateRegisterSwitch(Integer level, Integer money, Double profit){
        if (DateUtil.verifyTime()){
            return ResponseUtil.custom("0点到1点不能修改该配置");
        }
        if(level < 1 || level > 5 || money < 0 || profit < 0){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyRebateConfig proxyRebateConfig = proxyRebateConfigService.findFirst();
        if (LoginUtil.checkNull(proxyRebateConfig)){
            proxyRebateConfig = new ProxyRebateConfig();
        }
        if(level == 1){
            proxyRebateConfig.setFirstMoney(money);
            proxyRebateConfig.setFirstProfit(profit);
        }
        if(level == 2){
            proxyRebateConfig.setSecondMoney(money);
            proxyRebateConfig.setSecondProfit(profit);
        }
        if(level == 3){
            proxyRebateConfig.setThirdMoney(money);
            proxyRebateConfig.setThirdProfit(profit);
        }
        if(level == 4){
            proxyRebateConfig.setFourMoney(money);
            proxyRebateConfig.setFourProfit(profit);
        }
        if(level == 5){
            proxyRebateConfig.setFiveMoney(money);
            proxyRebateConfig.setFiveProfit(profit);
        }
        proxyRebateConfigService.save(proxyRebateConfig);
        return ResponseUtil.success();
    }
}
