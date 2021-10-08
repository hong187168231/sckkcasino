package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinoadmin.vo.BetRatioConfigVo;
import com.qianyi.casinoadmin.vo.DomainNameVo;
import com.qianyi.casinoadmin.vo.RegisterSwitchVo;
import com.qianyi.casinoadmin.vo.UserCommissionVo;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("platformConfig")
@Api(tags = "运营中心")
@Slf4j
public class PlatformConfigController {

    @Autowired
    private PlatformConfigService platformConfigService;

    @ApiOperation("玩家推广返佣配置查询")
    @GetMapping("/findCommission")
    public ResponseEntity<UserCommissionVo> findAll(){
        List<PlatformConfig> platformConfigList = platformConfigService.findAll();
        UserCommissionVo userCommissionVo = null;
        for (PlatformConfig platformConfig : platformConfigList) {
            userCommissionVo = UserCommissionVo.builder()
                    .name("玩家推广返佣配置")
                    .id(platformConfig.getId())
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
        BigDecimal commission = firstCommission.add(secondCommission).add(thirdCommission);
        if(commission.compareTo(BigDecimal.ONE) != 0){
            return ResponseUtil.custom("代理返佣配置总和不等于100%");
        }
        List<PlatformConfig> platformConfigList = platformConfigService.findAll();
        if(platformConfigList != null && platformConfigList.size() >= 1){
            Date commissionUpdate = platformConfigList.get(0).getCommissionUpdate();
            if(commissionUpdate != null){
                long time = new Date().getTime() - commissionUpdate.getTime();
                int oneDay = 60 * 60 * 1000 * 24;//一天时间
                if(oneDay > time){
                    return ResponseUtil.custom("该配置，每24小时只能修改一次");
                }
            }
            platformConfigList.get(0).setFirstCommission(firstCommission);
            platformConfigList.get(0).setSecondCommission(secondCommission);
            platformConfigList.get(0).setThirdCommission(thirdCommission);
            platformConfigList.get(0).setCommissionUpdate(new Date());
            platformConfigService.save(platformConfigList.get(0));
        }else{
            PlatformConfig platformConfig = new PlatformConfig();
            platformConfig.setFirstCommission(firstCommission);
            platformConfig.setSecondCommission(secondCommission);
            platformConfig.setThirdCommission(thirdCommission);
            platformConfig.setCommissionUpdate(new Date());
            platformConfigService.save(platformConfig);
        }
        return new ResponseEntity(ResponseCode.SUCCESS);
    }

    @ApiOperation("查询域名配置")
    @GetMapping("/findDomainName")
    public ResponseEntity<DomainNameVo> findDomainNameVo(){
        PlatformConfig first = platformConfigService.findFirst();
        DomainNameVo domainNameVo = new DomainNameVo();
        if (!LoginUtil.checkNull(first)){
            domainNameVo.setId(first.getId());
            domainNameVo.setName("域名配置");
            domainNameVo.setDomainNameConfiguration(first.getDomainNameConfiguration());
        }
        return new ResponseEntity(ResponseCode.SUCCESS, domainNameVo);
    }

    @ApiOperation("编辑域名配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
            @ApiImplicitParam(name = "domainNameConfiguration", value = "域名配置", required = true),
    })
    @PostMapping("/updateDomainName")
    public ResponseEntity updateDomainName(Long id,String domainNameConfiguration){
        if (LoginUtil.checkNull(id,domainNameConfiguration)){
            return ResponseUtil.custom("参数错误");
        }
        PlatformConfig first = platformConfigService.findFirst();
        if (LoginUtil.checkNull(first)){
            first = new PlatformConfig();
        }
        first.setId(id);
        first.setDomainNameConfiguration(domainNameConfiguration);
        platformConfigService.save(first);
        return ResponseUtil.success();
    }

    @ApiOperation("查询注册开关配置")
    @GetMapping("/findRegisterSwitch")
    public ResponseEntity<RegisterSwitchVo> findRegisterSwitchVo(){
        PlatformConfig first = platformConfigService.findFirst();
        RegisterSwitchVo registerSwitchVo = new RegisterSwitchVo();
        if (!LoginUtil.checkNull(first)){
            registerSwitchVo.setId(first.getId());
            registerSwitchVo.setName("注册开关");
            registerSwitchVo.setRegisterSwitch(first.getRegisterSwitch());
        }
        return new ResponseEntity(ResponseCode.SUCCESS, registerSwitchVo);
    }

    @ApiOperation("编辑注册开关配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
            @ApiImplicitParam(name = "registerSwitch", value = "注册开关配置", required = true),
    })
    @PostMapping("/updateRegisterSwitch")
    public ResponseEntity updateRegisterSwitch(Long id,Integer registerSwitch){
        if (LoginUtil.checkNull(id,registerSwitch)){
            return ResponseUtil.custom("参数错误");
        }
        PlatformConfig first = platformConfigService.findFirst();
        if (LoginUtil.checkNull(first)){
            first = new PlatformConfig();
        }
        first.setId(id);
        first.setRegisterSwitch(registerSwitch);
        platformConfigService.save(first);
        return ResponseUtil.success();
    }
}
