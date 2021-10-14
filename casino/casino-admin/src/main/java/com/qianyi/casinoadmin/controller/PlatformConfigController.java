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
    @PostMapping("/updateCommission")
    public ResponseEntity<UserCommissionVo> update(BigDecimal firstCommission, BigDecimal secondCommission, BigDecimal thirdCommission){
        if (LoginUtil.checkNull(firstCommission,secondCommission,thirdCommission)){
            return ResponseUtil.custom("参数错误");
        }
//        BigDecimal commission = firstCommission.add(secondCommission).add(thirdCommission);
//        if(commission.compareTo(new BigDecimal(0.03)) >= 0){
//            return ResponseUtil.custom("代理返佣配置总和不能大于3%");
//        }
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if(!LoginUtil.checkNull(platformConfig)){
//            Date commissionUpdate = platformConfig.getCommissionUpdate();
//            if(commissionUpdate != null){
//                long time = new Date().getTime() - commissionUpdate.getTime();
//                int oneDay = 60 * 60 * 1000 * 24;//一天时间
//                if(oneDay > time){
//                    return ResponseUtil.custom("该配置，每24小时只能修改一次");
//                }
//            }
            platformConfig.setFirstCommission(firstCommission);
            platformConfig.setSecondCommission(secondCommission);
            platformConfig.setThirdCommission(thirdCommission);
            platformConfig.setCommissionUpdate(new Date());
            platformConfigService.save(platformConfig);
        }else{
            PlatformConfig platform = new PlatformConfig();
            platform.setFirstCommission(firstCommission);
            platform.setSecondCommission(secondCommission);
            platform.setThirdCommission(thirdCommission);
            platform.setCommissionUpdate(new Date());
            platformConfigService.save(platform);
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
            domainNameVo.setProxyConfiguration(first.getProxyConfiguration());
        }
        return new ResponseEntity(ResponseCode.SUCCESS, domainNameVo);
    }

    @ApiOperation("编辑域名配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
            @ApiImplicitParam(name = "domainNameConfiguration", value = "首页域名配置", required = true),
            @ApiImplicitParam(name = "proxyConfiguration", value = "推广注册域名配置", required = true),
    })
    @PostMapping("/updateDomainName")
    public ResponseEntity updateDomainName(Long id,String domainNameConfiguration, String proxyConfiguration){
        if (LoginUtil.checkNull(id,domainNameConfiguration, proxyConfiguration)){
            return ResponseUtil.custom("参数错误");
        }
        PlatformConfig first = platformConfigService.findFirst();
        if (LoginUtil.checkNull(first)){
            first = new PlatformConfig();
        }
        first.setId(id);
        first.setDomainNameConfiguration(domainNameConfiguration);
        first.setProxyConfiguration(proxyConfiguration);
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
