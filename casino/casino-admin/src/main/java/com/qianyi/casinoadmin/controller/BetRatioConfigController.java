package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.vo.BetRatioConfigVo;
import com.qianyi.casinocore.CoreConstants;
import com.qianyi.casinocore.model.BetRatioConfig;
import com.qianyi.casinocore.model.SysConfig;
import com.qianyi.casinocore.service.SysConfigService;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/betRatioConfig")
@Api(tags = "资金中心")
public class BetRatioConfigController {

    @Autowired
    private SysConfigService sysConfigService;

    @ApiOperation("查询所有")
    @GetMapping("/findAll")
    public ResponseEntity<BetRatioConfigVo> findAll(){
        List<SysConfig> configList = sysConfigService.findByGroup(CoreConstants.SysConfigGroup.GROUP_BET);
        BetRatioConfigVo betRatioConfigVo = new BetRatioConfigVo();
        betRatioConfigVo.setName("打码倍率设置");
        for (SysConfig sysConfig : configList) {
            if(sysConfig.getName().equals(CoreConstants.SysConfigName.CAPTCHA_RATE)){
                betRatioConfigVo.setCodeTimes(Float.valueOf(sysConfig.getValue()));
            }else{
                betRatioConfigVo.setMinMoney(new BigDecimal(sysConfig.getValue()));
            }
        }
        return new ResponseEntity(ResponseCode.SUCCESS, betRatioConfigVo);
    }

    @ApiOperation("编辑打码倍率")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "codeTimes", value = "打码倍率", required = true),
            @ApiImplicitParam(name = "minMoney", value = "最低金额重置打码量", required = true)
    })
    @GetMapping("/update")
    public ResponseEntity<BetRatioConfig> update(BigDecimal codeTimes, BigDecimal minMoney){
        List<SysConfig> configList = sysConfigService.findByGroup(CoreConstants.SysConfigGroup.GROUP_BET);
        if(configList == null || configList.size() == 0){
            List<SysConfig> sysConfigList = new ArrayList<>();
            SysConfig sysConfig = new SysConfig();
            sysConfig.setSysGroup(CoreConstants.SysConfigGroup.GROUP_BET);
            sysConfig.setName(CoreConstants.SysConfigName.CAPTCHA_RATE);
            sysConfig.setValue(codeTimes.toString());
            sysConfigList.add(sysConfig);
            SysConfig sys = new SysConfig();
            sys.setSysGroup(CoreConstants.SysConfigGroup.GROUP_BET);
            sys.setName(CoreConstants.SysConfigName.CAPTCHA_MIN);
            sys.setValue(minMoney.toString());
            sysConfigList.add(sys);
            sysConfigList = sysConfigService.saveAll(sysConfigList);
            return new ResponseEntity(ResponseCode.SUCCESS, sysConfigList);
        }
        for (SysConfig sysConfig : configList) {
            if(sysConfig.getName().equals(CoreConstants.SysConfigName.CAPTCHA_RATE)){
                sysConfig.setValue(codeTimes.toString());
            }
            if(sysConfig.getName().equals(CoreConstants.SysConfigName.CAPTCHA_MIN)){
                sysConfig.setValue(minMoney.toString());
            }
        }
        return new ResponseEntity(ResponseCode.SUCCESS, configList);
    }
}
