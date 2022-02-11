package com.qianyi.casinoadmin.install;

import com.qianyi.casinoadmin.install.file.*;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.repository.CustomerConfigureRepository;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.UploadAndDownloadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
@Order(1)
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
    private PictureInitialize pictureInitialize;
    @Autowired
    private BankInfoService bankInfoService;

    @Autowired
    private CustomerConfigureService customerConfigureService;
    @Autowired
    private PictureService pictureService;
    @Autowired
    private InitializationSuperRole initializationSuperRole;
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysUserRoleService sysUserRoleService;
    @Value("${project.username:admin}")
    private String userNames;

    @Value("${project.password:123456}")
    private String password;

    @Autowired
    NewPermissions newPermissions;

    @Autowired
    private GameRecordEndIndexService gameRecordEndIndexService;



    @Override
    public void run(String... args) throws Exception {
       log.info("初始化数据开始============================================》");
//       this.saveBanner();
       this.runPlatformConfig();
       this.saveBankInfo();
       this.runProxyRebateConfig();
       this.runSysPermissionConfig();
       this.addPermissionConfig();
       this.runAddSysUser();
       //客户中心配置初始化
        this.saveCustomerConfigureInfo();
        this.saveGameRecordEndIndex();
    }

    private void saveGameRecordEndIndex() {
        GameRecordEndIndex first = gameRecordEndIndexService.findFirst();
        if (LoginUtil.checkNull(first)){
            first = new GameRecordEndIndex();
            first.setGameRecordId(0L);
            gameRecordEndIndexService.save(first);
        }else {
            first.setGameRecordId(first.getGameRecordId()==null?0L:first.getGameRecordId());
            first.setGameRecordGoldenFId(first.getGameRecordGoldenFId()==null?0L:first.getGameRecordGoldenFId());
        }
    }

    /**
     * 添加新的权限脚本
     */
    private void addPermissionConfig() {
        sysPermissionConfigFile.addPermissionConfig();
        newPermissions.addNewPermission();//新增加权限在此方法里面写
    }

    private void saveBanner(){
        List<LunboPic> PCLunboPics = pictureService.findByTheShowEnd(CommonConst.NUMBER_1);
        if (LoginUtil.checkNull(PCLunboPics) || PCLunboPics.size() == CommonConst.NUMBER_0){
            Map<String, Integer> PCMap = pictureInitialize.PCbanner;
            PCMap.forEach((key, value) -> {
                MultipartFile file = pictureInitialize.single(key);
                this.saveBanner(file,value,CommonConst.NUMBER_1);
            });
        }
        List<LunboPic> APPLunboPics = pictureService.findByTheShowEnd(CommonConst.NUMBER_2);
        if (LoginUtil.checkNull(APPLunboPics) || APPLunboPics.size() == CommonConst.NUMBER_0){
            Map<String, Integer> APPMap = pictureInitialize.APPbanner;
            APPMap.forEach((key, value) -> {
                MultipartFile file = pictureInitialize.single(key);
                this.saveBanner(file,value,CommonConst.NUMBER_2);
            });
        }
    }

    private void saveBanner(MultipartFile file,Integer no,Integer theShowEnd){
        LunboPic lunboPic = new LunboPic();
        lunboPic.setTheShowEnd(theShowEnd);
        lunboPic.setNo(no);
        try {
            if(file == null){
                lunboPic.setUrl(null);
            }else{
                PlatformConfig platformConfig= platformConfigService.findFirst();
                String uploadUrl = platformConfig.getUploadUrl();
                if(uploadUrl!=null) {
                    String fileUrl = UploadAndDownloadUtil.fileUpload(file, uploadUrl);
                    lunboPic.setUrl(fileUrl);
                }
            }
        } catch (Exception e) {
            log.error("初始化轮播图出错");
        }
        pictureService.save(lunboPic);
    }
    public void saveBankInfo(){
        List<BankInfo> all = bankInfoService.findAll();
        if (!LoginUtil.checkNull(all) && all.size() > CommonConst.NUMBER_0)
            return;
        Map<String, String> bank = pictureInitialize.bank;
        bank.forEach((key, value) -> {
            MultipartFile file = pictureInitialize.single(key);
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
                PlatformConfig platformConfig= platformConfigService.findFirst();
                String uploadUrl = platformConfig.getUploadUrl();
                if(uploadUrl!=null){
                    fileUrl = UploadAndDownloadUtil.fileUpload(file,uploadUrl);
                }
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

    //客服中心配置
    public void saveCustomerConfigureInfo(){
        List<CustomerConfigure> all = customerConfigureService.findAll();
        if (!LoginUtil.checkNull(all) && all.size() > CommonConst.NUMBER_0)
            return;
        //先判断是否有配置图片服务器
        PlatformConfig platformConfig= platformConfigService.findFirst();
        String uploadUrl = platformConfig.getUploadUrl();
        if(uploadUrl!=null){
            List<CustomerConfigure> customer = pictureInitialize.customer;
            customer.forEach(customerInfo ->{
                saveCustomerInfo(customerInfo,uploadUrl);
            });
        }
    }

    private void saveCustomerInfo(CustomerConfigure customerInfo,String uploadUrl){
        CustomerConfigure customerConfigureInfo = new CustomerConfigure();
        customerConfigureInfo.setState(customerInfo.getState());
        customerConfigureInfo.setCustomer(customerInfo.getCustomer());
        customerConfigureInfo.setCustomerMark(customerInfo.getCustomerMark());
        customerConfigureInfo.setCustomerAccount(customerInfo.getCustomerAccount());
        if (!CommonUtil.checkNull(customerInfo.getAppIconUrl())){
            MultipartFile appIconUrl = pictureInitialize.single(customerInfo.getAppIconUrl());
            try {
                String appFileUrl = UploadAndDownloadUtil.fileUpload(appIconUrl,uploadUrl);
                customerConfigureInfo.setAppIconUrl(appFileUrl);
            } catch (Exception e) {
                customerConfigureInfo.setAppIconUrl("");
                customerConfigureInfo.setPcIconUrl("");
            }
        }
        if (!CommonUtil.checkNull(customerInfo.getPcIconUrl())){
            MultipartFile pcIconUrl = pictureInitialize.single(customerInfo.getPcIconUrl());
            try {
                String pcFileUrl = UploadAndDownloadUtil.fileUpload(pcIconUrl,uploadUrl);
                customerConfigureInfo.setPcIconUrl(pcFileUrl);
            } catch (Exception e) {
                customerConfigureInfo.setAppIconUrl("");
                customerConfigureInfo.setPcIconUrl("");
            }
        }
        try {
            customerConfigureService.save(customerConfigureInfo);
        }catch (Exception e){
            log.error("初始化客服图标异常");
        }
    }


    //系统超级管理员角色
    private void runAddSysUser() {
        //创建出系统超级管理员角色
        initializationSuperRole.saveSuperRole();
        SysRole sysRole = new SysRole();
        sysRole.setRoleName("系统超级管理员");
        List<SysRole> sysRoleList = sysRoleService.findbyRoleName(sysRole);
        if(sysRoleList.isEmpty() || sysRoleList.size() == 0){
            return;
        }
        SysRole role = sysRoleList.get(0);
        String[] split = userNames.split(",");
        for (String name : split) {
            SysUser sys = sysUserService.findByUserName(name);
            if(sys != null){
                SysUserRole sysUserRole = sysUserRoleService.findbySysUserId(sys.getId());
                if(sysUserRole == null){
                    sysUserRole = new SysUserRole();
                }
                sysUserRole.setSysUserId(sys.getId());
                sysUserRole.setSysRoleId(role.getId());
                sysUserRoleService.save(sysUserRole);
                continue;
            }
            //加密
            String bcryptPassword = LoginUtil.bcrypt(password);
            SysUser sysUser = new SysUser();
            sysUser.setUserName(name);
            sysUser.setNickName(name);
            sysUser.setPassWord(bcryptPassword);
            sysUser.setUserFlag(Constants.open);
            sysUserService.save(sysUser);
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setSysUserId(sysUser.getId());
            sysUserRole.setSysRoleId(role.getId());
            sysUserRoleService.save(sysUserRole);
        }
    }

    private void runSysPermissionConfig() {
        sysPermissionConfigFile.getPermissionConfig();
    }

    private void runProxyRebateConfig(){
        List<RebateConfig> all = rebateConfigService.findAll();
        if (LoginUtil.checkNull(all) || all.size() == CommonConst.NUMBER_0){
            RebateConfig first = new RebateConfig();
            first.setFirstMoney(proxyRebateConfigFile.getFirstMoney());
            first.setFirstAmountLine(proxyRebateConfigFile.getFirstMoneyLine());
            first.setFirstProfit(proxyRebateConfigFile.getFirstProfit());
            first.setSecondMoney(proxyRebateConfigFile.getSecondMoney());
            first.setSecondAmountLine(proxyRebateConfigFile.getSecondMoneyLine());
            first.setSecondProfit(proxyRebateConfigFile.getSecondProfit());
            first.setThirdMoney(proxyRebateConfigFile.getThirdMoney());
            first.setThirdAmountLine(proxyRebateConfigFile.getThirdMoneyLine());
            first.setThirdProfit(proxyRebateConfigFile.getThirdProfit());
            first.setFourMoney(proxyRebateConfigFile.getFourMoney());
            first.setFourAmountLine(proxyRebateConfigFile.getFourMoneyLine());
            first.setFourProfit(proxyRebateConfigFile.getFourProfit());
            first.setFiveMoney(proxyRebateConfigFile.getFiveMoney());
            first.setFiveAmountLine(proxyRebateConfigFile.getFiveMoneyLine());
            first.setFiveProfit(proxyRebateConfigFile.getFiveProfit());
            first.setSixMoney(proxyRebateConfigFile.getSixMoney());
            first.setSixAmountLine(proxyRebateConfigFile.getSixMoneyLine());
            first.setSixProfit(proxyRebateConfigFile.getSixProfit());
            first.setSevenMoney(proxyRebateConfigFile.getSevenMoney());
            first.setSevenAmountLine(proxyRebateConfigFile.getSevenMoneyLine());
            first.setSevenProfit(proxyRebateConfigFile.getSevenProfit());
            first.setEightMoney(proxyRebateConfigFile.getEightMoney());
            first.setEightAmountLine(proxyRebateConfigFile.getEightMoneyLine());
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
            platformConfig.setDirectlyUnderTheLower(platformConfigFile.getDirectlyUnderTheLower());
            platformConfig.setCompanyInviteCode(platformConfigFile.getCompanyInviteCode());
            platformConfig.setCustomerCode(platformConfigFile.getCustomerCode());
            platformConfig.setUploadUrl(platformConfigFile.getUploadUrl());
            platformConfig.setReadUploadUrl(platformConfigFile.getReadUploadUrl());
            platformConfig.setMoneySymbol(platformConfigFile.getMoneySymbol());
            platformConfig.setPeopleProxySwitch(platformConfigFile.getPeopleProxySwitch());
            platformConfig.setBankcardRealNameSwitch(platformConfigFile.getBankcardRealNameSwitch());
            platformConfigService.save(platformConfig);
        }else {
            PlatformConfig platformConfig = all.get(CommonConst.NUMBER_0);
            String companyInviteCode = platformConfig.getCompanyInviteCode();
            if (companyInviteCode == null || !companyInviteCode.equals("999")){
                platformConfig.setCompanyInviteCode("999");
            }
            if (LoginUtil.checkNull(platformConfig.getCustomerCode())){
                platformConfig.setCustomerCode("21141305");
            }
            if (LoginUtil.checkNull(platformConfig.getBankcardRealNameSwitch())){
                platformConfig.setBankcardRealNameSwitch(platformConfigFile.getBankcardRealNameSwitch());
            }
            if (LoginUtil.checkNull(platformConfig.getPeopleProxySwitch())){
                platformConfig.setPeopleProxySwitch(platformConfigFile.getPeopleProxySwitch());
            }
            platformConfigService.save(platformConfig);
        }
    }
}
