package com.qianyi.casinoweb.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;

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

    @GetMapping("checkPlatformMaintenanceSwitch")
    @ApiOperation("检查平台维护开关")
    @NoAuthentication
    public ResponseEntity<PlatformMaintenanceSwitch> checkPlatformMaintenanceSwitch() {
        PlatformMaintenanceSwitch vo = new PlatformMaintenanceSwitch();
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (platformConfig == null || platformConfig.getMaintenanceStart() == null || platformConfig.getMaintenanceEnd() == null) {
            vo.setOnOff(false);
            return ResponseUtil.success(vo);
        }
        Integer maintenance = platformConfig.getPlatformMaintenance();
        boolean switchb = maintenance == Constants.open ? true : false;
        //先判断开关是否是维护状态，在判断当前时间是否在维护时区间内
        if (switchb) {
            switchb = DateUtil.isEffectiveDate(new Date(), platformConfig.getMaintenanceStart(), platformConfig.getMaintenanceEnd());
        }
        vo.setOnOff(switchb);
        //最后确定状态
        if (switchb) {
            vo.setStartTime(platformConfig.getMaintenanceStart());
            vo.setEndTime(platformConfig.getMaintenanceEnd());
        }
        return ResponseUtil.success(vo);
    }

    @ApiOperation("增减平台总余额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "0:减 1:加", required = true),
            @ApiImplicitParam(name = "amount", value = "操作金额", required = true),
    })
    @GetMapping("/updateTotalPlatformQuota")
    @Async("asyncExecutor")
    public  synchronized ResponseEntity updateTotalPlatformQuota(Integer type,BigDecimal amount){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (type.equals( CommonConst.NUMBER_0)){
            platformConfig.setTotalPlatformQuota(platformConfig.getTotalPlatformQuota().subtract(amount));
        }else {
            platformConfig.setTotalPlatformQuota(platformConfig.getTotalPlatformQuota().add(amount));
        }
        platformConfigService.save(platformConfig);
        return ResponseUtil.success();
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

    @Data
    @ApiModel("平台维护开关")
    class PlatformMaintenanceSwitch{
        @ApiModelProperty("开关状态，true:维护中，false:正常")
        private Boolean onOff;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date startTime;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date endTime;
    }
}
