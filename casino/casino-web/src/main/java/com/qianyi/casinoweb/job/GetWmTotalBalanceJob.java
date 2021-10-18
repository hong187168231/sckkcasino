package com.qianyi.casinoweb.job;

import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.livewm.api.PublicWMApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 整点查询平台在WM的总余额，更新到本地
 */
@Component
public class GetWmTotalBalanceJob {

    @Autowired
    private PublicWMApi wmApi;
    @Autowired
    private PlatformConfigService platformConfigService;

    @Scheduled(cron = "0 0 * * * ?")
    public void tasks() {
        try {
            BigDecimal agentBalance = wmApi.getAgentBalance(0);
            if (agentBalance == null) {
                return;
            }
            PlatformConfig platformConfig = platformConfigService.findFirst();
            if (platformConfig == null) {
                platformConfig = new PlatformConfig();
                platformConfig.setWmMoney(agentBalance);
                platformConfigService.save(platformConfig);
                return;
            }
            platformConfig.setWmMoney(agentBalance);
            platformConfigService.save(platformConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
