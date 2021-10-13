package com.qianyi.casinoweb.runner;

import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.WashCodeConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.service.WashCodeConfigService;
import com.qianyi.casinoweb.job.GetWmTotalBalanceJob;
import com.qianyi.modulecommon.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 第一次启动项目初始化wm余额
 */
@Component
public class GetWmTotalBalanceRunner implements CommandLineRunner {

    @Autowired
    private PlatformConfigService platformConfigService;
    @Autowired
    private GetWmTotalBalanceJob getWmTotalBalanceJob;

    @Override
    public void run(String... args) throws Exception {
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (platformConfig == null || platformConfig.getWmMoney() == null) {
            getWmTotalBalanceJob.tasks();
        }
    }

}
