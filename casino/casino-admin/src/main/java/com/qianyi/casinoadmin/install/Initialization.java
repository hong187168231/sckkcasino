package com.qianyi.casinoadmin.install;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.util.CommonConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class Initialization implements CommandLineRunner {
    @Autowired
    private PlatformConfigBean platformConfigBean;
    @Autowired
    private PlatformConfigService platformConfigService;
    @Override
    public void run(String... args) throws Exception {
        log.info("初始化数据开始============================================》");
        List<PlatformConfig> all = platformConfigService.findAll();
        if (LoginUtil.checkNull(all) || all.size()== CommonConst.NUMBER_0){
            PlatformConfig platformConfig = new PlatformConfig();
            platformConfig.setClearCodeNum(platformConfigBean.getClearCodeNum());
            platformConfig.setBetRate(platformConfigBean.getBetRate());
            platformConfig.setChargeMinMoney(platformConfigBean.getChargeMinMoney());
            platformConfig.setChargeMaxMoney(platformConfigBean.getChargeMaxMoney());
            platformConfig.setChargeServiceMoney(platformConfigBean.getChargeServiceMoney());
            platformConfig.setChargeRate(platformConfigBean.getChargeRate());
            platformConfig.setWithdrawMinMoney(platformConfigBean.getWithdrawMinMoney());
            platformConfig.setWithdrawMaxMoney(platformConfigBean.getWithdrawMaxMoney());
            platformConfig.setWithdrawServiceMoney(platformConfigBean.getWithdrawServiceMoney());
            platformConfig.setWithdrawRate(platformConfigBean.getWithdrawRate());
            platformConfig.setIpMaxNum(platformConfigBean.getIpMaxNum());
            platformConfig.setWmMoney(platformConfigBean.getWmMoney());
            platformConfig.setWmMoneyWarning(platformConfigBean.getWmMoneyWarning());
            platformConfig.setFirstCommission(platformConfigBean.getFirstCommission());
            platformConfig.setSecondCommission(platformConfigBean.getSecondCommission());
            platformConfig.setThirdCommission(platformConfigBean.getThirdCommission());
            platformConfig.setDomainNameConfiguration(platformConfigBean.getDomainNameConfiguration());
            platformConfig.setRegisterSwitch(platformConfigBean.getRegisterSwitch());
            platformConfigService.save(platformConfig);
        }
    }
}
