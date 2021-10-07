package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.vo.BetRatioConfigVo;
import com.qianyi.casinoadmin.vo.UserCommissionVo;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
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
import java.util.List;

@RestController
@RequestMapping("platformConfig")
@Api(tags = "运营中心")
@Slf4j
public class PlatformConfigController {

    @Autowired
    private PlatformConfigService platformConfigService;

    @ApiOperation("玩家推广返佣配置")
    @GetMapping("/findCommission")
    public ResponseEntity<UserCommissionVo> findAll(){
        List<PlatformConfig> platformConfigList = platformConfigService.findAll();
        UserCommissionVo userCommissionVo = null;
        for (PlatformConfig platformConfig : platformConfigList) {
            userCommissionVo = UserCommissionVo.builder()
                    .name("玩家推广返佣配置")
                    .firstCommission(platformConfig.getFirstCommission())
                    .secondCommission(platformConfig.getSecondCommission())
                    .thirdCommission(platformConfig.getThirdCommission())
                    .build();
        }
        return new ResponseEntity(ResponseCode.SUCCESS, userCommissionVo);
    }

    @ApiOperation("编辑玩家推广返佣配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "firstCommission", value = "一级代理返佣", required = true),
            @ApiImplicitParam(name = "secondCommission", value = "二级代理返佣", required = true),
            @ApiImplicitParam(name = "thirdCommission", value = "三级代理返佣", required = true)
    })
    @GetMapping("/updateCommission")
    public ResponseEntity<UserCommissionVo> update(BigDecimal firstCommission, BigDecimal secondCommission, BigDecimal thirdCommission){
        List<PlatformConfig> platformConfigList = platformConfigService.findAll();
        if(platformConfigList != null && platformConfigList.size() >= 1){
            platformConfigList.get(0).setFirstCommission(firstCommission);
            platformConfigList.get(0).setSecondCommission(secondCommission);
            platformConfigList.get(0).setThirdCommission(thirdCommission);
            platformConfigService.save(platformConfigList.get(0));
        }else{
            PlatformConfig platformConfig = new PlatformConfig();
            platformConfig.setFirstCommission(firstCommission);
            platformConfig.setSecondCommission(secondCommission);
            platformConfig.setThirdCommission(thirdCommission);
            platformConfigService.save(platformConfig);
        }
        return new ResponseEntity(ResponseCode.SUCCESS);
    }
}
