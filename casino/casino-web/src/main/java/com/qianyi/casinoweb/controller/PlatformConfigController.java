package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("platformConfig")
@Api(tags = "平台配置中心")
@Slf4j
public class PlatformConfigController {

    @Autowired
    private PlatformConfigService platformConfigService;

    @GetMapping("getCustomerCode")
    @ApiOperation("获取客服脚本编码")
    @NoAuthentication
    public ResponseEntity<String> getCustomerCode() {
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (platformConfig == null) {
            return ResponseUtil.success();
        }
        String customerCode = platformConfig.getCustomerCode();
        return ResponseUtil.success(customerCode);
    }

    @GetMapping("checkPeopleProxySwitch")
    @ApiOperation("检查人人代开关")
    @NoAuthentication
    public ResponseEntity<Boolean> checkPeopleProxySwitch() {
        PlatformConfig platformConfig = platformConfigService.findFirst();
        boolean proxySwitch = PlatformConfig.checkPeopleProxySwitch(platformConfig);
        return ResponseUtil.success(proxySwitch);
    }

}
