package com.qianyi.casinoweb.inteceptor;

import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.inteceptor.AbstractPlatformMaintainInteceptor;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Slf4j
public class PlatformMaintainInteceptor extends AbstractPlatformMaintainInteceptor {

    @Autowired
    private PlatformConfigService platformConfigService;

    @Override
    protected PlatformMaintenanceSwitch platformMaintainCheck() {
        PlatformMaintenanceSwitch vo = new PlatformMaintenanceSwitch();
        try {
            PlatformConfig platformConfig = platformConfigService.findFirst();
            if (platformConfig == null || platformConfig.getMaintenanceStart() == null || platformConfig.getMaintenanceEnd() == null) {
                vo.setOnOff(false);
                return vo;
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
                SimpleDateFormat sd = DateUtil.getSimpleDateFormat();
                if (platformConfig.getMaintenanceStart() != null) {
                    vo.setStartTime(sd.format(platformConfig.getMaintenanceStart()));
                }
                if (platformConfig.getMaintenanceEnd() != null) {
                    vo.setEndTime(sd.format(platformConfig.getMaintenanceEnd()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("检查KK平台维护时报错，msg={}", e.getMessage());
        }
        return vo;
    }
}
