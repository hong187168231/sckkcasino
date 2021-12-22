package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

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

    @GetMapping("getPeopleProportion")
    @ApiOperation("获取人人代三级分佣比例")
    @NoAuthentication
    public ResponseEntity<PeopleProportion> getPeopleProportion() {
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (platformConfig == null) {
            return ResponseUtil.success();
        }
        PeopleProportion peopleProportion = new PeopleProportion();
        BeanUtils.copyProperties(platformConfig, peopleProportion);
        return ResponseUtil.success(peopleProportion);
    }

    @Data
    @ApiModel("人人代三级分佣比例")
    class PeopleProportion{

        @ApiModelProperty("一级玩家返佣")
        private BigDecimal firstCommission;

        @ApiModelProperty("二级玩家返佣")
        private BigDecimal secondCommission;

        @ApiModelProperty("三级玩家返佣")
        private BigDecimal thirdCommission;
    }
}
