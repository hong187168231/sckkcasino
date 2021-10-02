package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.vo.BetRatioConfigVo;
import com.qianyi.casinocore.CoreConstants;
import com.qianyi.casinocore.model.BetRatioConfig;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.SysConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
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
    private PlatformConfigService platformConfigService;

    @ApiOperation("打码设置查询")
    @GetMapping("/findAll")
    public ResponseEntity<BetRatioConfigVo> findAll(){
        List<PlatformConfig> platformConfigList = platformConfigService.findAll();
        BetRatioConfigVo betRatioConfigVo = new BetRatioConfigVo();
        betRatioConfigVo.setName("打码倍率设置");
        for (PlatformConfig platformConfig : platformConfigList) {
            if(platformConfig.getBetRate() != null){
                betRatioConfigVo.setCodeTimes(platformConfig.getBetRate());
            }
            if(platformConfig.getClearCodeNum() != null){
                betRatioConfigVo.setMinMoney(platformConfig.getClearCodeNum());
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
    public ResponseEntity<BetRatioConfigVo> update(BigDecimal codeTimes, BigDecimal minMoney){
        List<PlatformConfig> platformConfigList = platformConfigService.findAll();
        if(platformConfigList != null && platformConfigList.size() >= 1){
            platformConfigList.get(0).setClearCodeNum(minMoney);
            platformConfigList.get(0).setBetRate(codeTimes);
            platformConfigService.save(platformConfigList.get(0));
        }else{
            PlatformConfig platformConfig = new PlatformConfig();
            platformConfig.setBetRate(codeTimes);
            platformConfig.setClearCodeNum(minMoney);
            platformConfigService.save(platformConfig);
        }
        return new ResponseEntity(ResponseCode.SUCCESS);
    }
}
