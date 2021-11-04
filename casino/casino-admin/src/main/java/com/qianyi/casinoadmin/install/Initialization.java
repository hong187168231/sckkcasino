package com.qianyi.casinoadmin.install;

import com.qianyi.casinoadmin.install.file.BankInfoImg;
import com.qianyi.casinoadmin.install.file.PlatformConfigFile;
import com.qianyi.casinoadmin.install.file.ProxyRebateConfigFile;
import com.qianyi.casinoadmin.install.file.SysPermissionConfigFile;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.BankInfo;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.RebateConfig;
import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.service.BankInfoService;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.service.RebateConfigService;
import com.qianyi.casinocore.service.SysUserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.UploadAndDownloadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class Initialization implements CommandLineRunner {
    @Autowired
    private PlatformConfigFile platformConfigFile;
    @Autowired
    private ProxyRebateConfigFile proxyRebateConfigFile;
    @Autowired
    private PlatformConfigService platformConfigService;
    @Autowired
    private RebateConfigService rebateConfigService;
    @Autowired
    private SysPermissionConfigFile sysPermissionConfigFile;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private BankInfoImg bankInfoImg;
    @Autowired
    private BankInfoService bankInfoService;

    @Override
    public void run(String... args) throws Exception {
       log.info("初始化数据开始============================================》");
       this.saveBankInfo();
       this.runAddSysUser();
       this.runPlatformConfig();
       this.runProxyRebateConfig();
       this.runSysPermissionConfig();
    }
    private void saveBankInfo(){
        List<BankInfo> all = bankInfoService.findAll();
        if (!LoginUtil.checkNull(all) && all.size() > CommonConst.NUMBER_0)
            return;
        Map<String, String> bank = BankInfoImg.bank;
        bank.forEach((key, value) -> {
            MultipartFile file = bankInfoImg.single(key);
            this.saveBankInfo(file,value);
        });
    }
    private void saveBankInfo(MultipartFile file,String bankName){
        BankInfo bankInfo = new BankInfo();
        bankInfo.setBankName(bankName);
        bankInfo.setBankType(CommonConst.NUMBER_0);//默认银行卡
        bankInfo.setDisable(CommonConst.NUMBER_0);//默启用用
        try {
            String fileUrl = null;
            if (file != null){
                fileUrl = UploadAndDownloadUtil.fileUpload(CommonUtil.getLocalPicPath(), file);
            }
            bankInfo.setBankLogo(fileUrl);
        } catch (Exception e) {
            bankInfo.setBankLogo("");
        }
        try {
            bankInfoService.saveBankInfo(bankInfo);
        }catch (Exception e){
            log.error("重复银行名称");
        }
    }

    private void runAddSysUser() {
        SysUser sys = sysUserService.findByUserName("admin");
        if(sys != null){
            return;
        }
        //加密
        String bcryptPassword = LoginUtil.bcrypt("123456");
        SysUser sysUser = new SysUser();
        sysUser.setUserName("admin");
        sysUser.setNickName("admin");
        sysUser.setPassWord(bcryptPassword);
        sysUser.setUserFlag(Constants.open);
        sysUserService.save(sysUser);
    }

    private void runSysPermissionConfig() {
        sysPermissionConfigFile.getPermissionConfig();
    }

    private void runProxyRebateConfig(){
        List<RebateConfig> all = rebateConfigService.findAll();
        if (LoginUtil.checkNull(all) || all.size() == CommonConst.NUMBER_0){
            RebateConfig first = new RebateConfig();
            first.setFirstMoney(proxyRebateConfigFile.getFirstMoney());
            first.setFirstProfit(proxyRebateConfigFile.getFirstProfit());
            first.setSecondMoney(proxyRebateConfigFile.getSecondMoney());
            first.setSecondProfit(proxyRebateConfigFile.getSecondProfit());
            first.setThirdMoney(proxyRebateConfigFile.getThirdMoney());
            first.setThirdProfit(proxyRebateConfigFile.getThirdProfit());
            first.setFourMoney(proxyRebateConfigFile.getFourMoney());
            first.setFourProfit(proxyRebateConfigFile.getFourProfit());
            first.setFiveMoney(proxyRebateConfigFile.getFiveMoney());
            first.setFiveProfit(proxyRebateConfigFile.getFiveProfit());
            first.setSixMoney(proxyRebateConfigFile.getSixMoney());
            first.setSixProfit(proxyRebateConfigFile.getSixProfit());
            first.setSevenMoney(proxyRebateConfigFile.getSevenMoney());
            first.setSevenProfit(proxyRebateConfigFile.getSevenProfit());
            first.setEightMoney(proxyRebateConfigFile.getEightMoney());
            first.setEightProfit(proxyRebateConfigFile.getEightProfit());
            rebateConfigService.save(first);
        }
    }
    private void runPlatformConfig(){
        List<PlatformConfig> all = platformConfigService.findAll();
        if (LoginUtil.checkNull(all) || all.size()== CommonConst.NUMBER_0){
            PlatformConfig platformConfig = new PlatformConfig();
            platformConfig.setClearCodeNum(platformConfigFile.getClearCodeNum());
            platformConfig.setBetRate(platformConfigFile.getBetRate());
            platformConfig.setChargeMinMoney(platformConfigFile.getChargeMinMoney());
            platformConfig.setChargeMaxMoney(platformConfigFile.getChargeMaxMoney());
            platformConfig.setChargeServiceMoney(platformConfigFile.getChargeServiceMoney());
            platformConfig.setChargeRate(platformConfigFile.getChargeRate());
            platformConfig.setWithdrawMinMoney(platformConfigFile.getWithdrawMinMoney());
            platformConfig.setWithdrawMaxMoney(platformConfigFile.getWithdrawMaxMoney());
            platformConfig.setWithdrawServiceMoney(platformConfigFile.getWithdrawServiceMoney());
            platformConfig.setWithdrawRate(platformConfigFile.getWithdrawRate());
            platformConfig.setIpMaxNum(platformConfigFile.getIpMaxNum());
            platformConfig.setWmMoney(platformConfigFile.getWmMoney());
            platformConfig.setWmMoneyWarning(platformConfigFile.getWmMoneyWarning());
            platformConfig.setFirstCommission(platformConfigFile.getFirstCommission());
            platformConfig.setSecondCommission(platformConfigFile.getSecondCommission());
            platformConfig.setThirdCommission(platformConfigFile.getThirdCommission());
            platformConfig.setDomainNameConfiguration(platformConfigFile.getDomainNameConfiguration());
            platformConfig.setRegisterSwitch(platformConfigFile.getRegisterSwitch());
            platformConfig.setProxyConfiguration(platformConfigFile.getProxyConfiguration());
            platformConfig.setSendMessageWarning(platformConfigFile.getSendMessageWarning());
            platformConfigService.save(platformConfig);
        }
    }
}
