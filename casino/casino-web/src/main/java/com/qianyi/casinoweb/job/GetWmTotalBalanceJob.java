package com.qianyi.casinoweb.job;

import com.qianyi.casinocore.CoreConstants;
import com.qianyi.casinocore.model.SysConfig;
import com.qianyi.casinocore.service.SysConfigService;
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
    private SysConfigService sysConfigService;

    @Scheduled(cron = "0 0 * * * ?")
    public void testTasks() {
        try {
            BigDecimal agentBalance = wmApi.getAgentBalance(0);
            BigDecimal balance = agentBalance == null ? BigDecimal.ZERO : agentBalance;
            SysConfig sysConfig = sysConfigService.findBySysGroupAndName(CoreConstants.SysConfigGroup.GROUP_FINANCE, CoreConstants.SysConfigName.WM_TOTAL_BALANCE);
            if (sysConfig == null) {
                sysConfig = new SysConfig();
                sysConfig.setSysGroup(CoreConstants.SysConfigGroup.GROUP_FINANCE);
                sysConfig.setName(CoreConstants.SysConfigName.WM_TOTAL_BALANCE);
                sysConfig.setValue(balance.toString());
                sysConfig.setRemark("平台在WM的总余额");
                sysConfigService.save(sysConfig);
                return;
            }
            sysConfig.setValue(balance.toString());
            sysConfigService.save(sysConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
