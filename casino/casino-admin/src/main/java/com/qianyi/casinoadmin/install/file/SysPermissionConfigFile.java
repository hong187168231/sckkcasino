package com.qianyi.casinoadmin.install.file;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.SysPermission;
import com.qianyi.casinocore.service.SysPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SysPermissionConfigFile {

    @Autowired
    private SysPermissionService sysPermissionService;

    public List<SysPermission> getPermissionConfig() {
        List<SysPermission> all = sysPermissionService.findAll();
        if(!LoginUtil.checkNull(all)){
            //TODO 调试完需要删除
//            sysPermissionService.delete();
            return null;
        }
        List<SysPermission> sysPermissionList = new ArrayList<>();
        //一级菜单栏位
        SysPermission memberPermission = new SysPermission("会员中心", "会员相关数据", "/memberCenter", 0l, 1, 0);
        SysPermission agentPermission = new SysPermission("代理中心", "代理后台相关数据", "/agentCenter", 0l, 1, 0);
        SysPermission orderPermission = new SysPermission("订单中心", "游戏注单相关数据", "/orderCenter", 0l, 1, 0);
        SysPermission financePermission = new SysPermission("财务中心", "财务数据", "/financeCenter", 0l, 1, 0);
        SysPermission operatePermission = new SysPermission("运营中心", "运营相关数据", "/operateCenter", 0l, 1, 0);
        SysPermission reportPermission = new SysPermission("报表管理", "报表统计相关数据", "/reportCenter", 0l, 1, 0);
        SysPermission systemPermission = new SysPermission("系统管理", "系统管理相关数据", "/systemCenter", 0l, 1, 0);
        sysPermissionList.add(memberPermission);
        sysPermissionList.add(agentPermission);
        sysPermissionList.add(orderPermission);
        sysPermissionList.add(financePermission);
        sysPermissionList.add(operatePermission);
        sysPermissionList.add(reportPermission);
        sysPermissionList.add(systemPermission);
        sysPermissionService.saveAllList(sysPermissionList);

        List<SysPermission> secordPermissions = new ArrayList<>();
        //会员中心菜单下
        SysPermission memberTranslate = new SysPermission("会员管理", "会员查询列表", "/user/findUserList", memberPermission.getId(), 2, 0);
        SysPermission memberCheck = new SysPermission("会员反查", "会员ip和地址查询", "/userPegging/findUserPegging", memberPermission.getId(), 2, 0);
        SysPermission thridMemberTranslate = new SysPermission("三方账户查询", "三方账户查询", "/userThird/findUserThird", memberPermission.getId(), 2, 0);
        secordPermissions.add(memberTranslate);
        secordPermissions.add(memberCheck);
        secordPermissions.add(thridMemberTranslate);

        //代理中心下菜单
        SysPermission agentSystem = new SysPermission("代理后台管理", "代理后台查询", "/proxyUser/findProxyUser", agentPermission.getId(), 2, 0);
        SysPermission agentReport = new SysPermission("代理后台日报表", "代理后台日报表", "/companyProxyDetail/find", agentPermission.getId(), 2, 0);
        SysPermission agentMonthReport = new SysPermission("代理后台月报表", "代理后台月报表", "/companyProxyMonth/find", agentPermission.getId(), 2, 0);
        secordPermissions.add(agentSystem);
        secordPermissions.add(agentReport);
        secordPermissions.add(agentMonthReport);

        //订单中心
        SysPermission chargeOrder = new SysPermission("充值记录", "充值记录", "/chargeOrder/chargeOrderList", orderPermission.getId(), 2, 0);
        SysPermission withdrawalOrder = new SysPermission("提现记录", "提现记录", "/withdraw/withdrawList", orderPermission.getId(), 2, 0);
        secordPermissions.add(chargeOrder);
        secordPermissions.add(withdrawalOrder);

        SysPermission bankChannel = new SysPermission("银行渠道设置", "银行渠道设置", "/bankcard/banklist", financePermission.getId(), 2, 0);
        SysPermission collectionSet = new SysPermission("收款银行卡设置", "收款银行卡设置", "/chargeOrder/chargeOrderList", financePermission.getId(), 2, 0);
        secordPermissions.add(bankChannel);
        secordPermissions.add(collectionSet);

        SysPermission platformConfig = new SysPermission("平台配置", "平台配置", "/chargeConfig/findChargeConfig", operatePermission.getId(), 2, 0);
        SysPermission agentPromotion = new SysPermission("代理推广返佣配置", "代理推广返佣配置", "/proxyRebate/findAll", operatePermission.getId(), 2, 0);
        SysPermission pcBanner = new SysPermission("PC端轮播图配置", "PC端轮播图配置", "/picture/findByBannerList", operatePermission.getId(), 2, 0);
        SysPermission mobileBanner = new SysPermission("移动端轮播图配置", "移动端轮播图配置", "/picture/findByBannerList", operatePermission.getId(), 2, 0);
        SysPermission noticeConfig = new SysPermission("公告消息配置", "公告消息配置", "/notice/findNotice", operatePermission.getId(), 2, 0);
        SysPermission gameConfig = new SysPermission("游戏洗码配置", "游戏洗码配置", "/washCodeConfig/findAll", operatePermission.getId(), 2, 0);
        SysPermission customerConfig = new SysPermission("客服中心配置", "客服中心配置", "/customer/findCustomer", operatePermission.getId(), 2, 0);
        SysPermission IPConfig = new SysPermission("IP黑名单管理", "IP黑名单管理", "/ipBlack/findIpBlackPag", operatePermission.getId(), 2, 0);
        secordPermissions.add(platformConfig);
        secordPermissions.add(agentPromotion);
        secordPermissions.add(pcBanner);
        secordPermissions.add(mobileBanner);
        secordPermissions.add(noticeConfig);
        secordPermissions.add(gameConfig);
        secordPermissions.add(customerConfig);
        secordPermissions.add(IPConfig);

        SysPermission WMbanlance = new SysPermission("WM资金明细", "WM资金明细", "/order/findOrderList", reportPermission.getId(), 2, 0);
        SysPermission userCharge = new SysPermission("会员账变", "会员账变", "/accountChange/findAccountChangePage", reportPermission.getId(), 2, 0);
        SysPermission chargeLog = new SysPermission("充值订单流水", "充值订单流水", "/rechargeTurnover/findPage", reportPermission.getId(), 2, 0);
        SysPermission thridGameRecord = new SysPermission("第三方游戏注单", "第三方游戏注单", "/gameRecord/findGameRecordPage", reportPermission.getId(), 2, 0);
        SysPermission userLog = new SysPermission("用户登录日志", "用户登录日志", "/loginLog/findLoginLogPage", reportPermission.getId(), 2, 0);
        secordPermissions.add(WMbanlance);
        secordPermissions.add(userCharge);
        secordPermissions.add(chargeLog);
        secordPermissions.add(thridGameRecord);
        secordPermissions.add(userLog);

        SysPermission APPMeange = new SysPermission("APP升级管理", "APP升级管理", "/download/findDownloadStationPage", systemPermission.getId(), 2, 0);
        SysPermission sysUserList = new SysPermission("用户列表", "用户列表", "/sysUser/userList", systemPermission.getId(), 2, 0);
        SysPermission roleUserList = new SysPermission("角色列表", "角色列表", "/sysUser/roleList", systemPermission.getId(), 2, 0);
        secordPermissions.add(APPMeange);
        secordPermissions.add(sysUserList);
        sysPermissionService.saveAllList(secordPermissions);

        //会员查询下菜单
        List<SysPermission> thridPermissions = new ArrayList<>();
        SysPermission saveCharge = new SysPermission("上分", "上分", "/user/saveChargeOrder", memberTranslate.getId(), 3, 0);
        SysPermission withdrawOrder = new SysPermission("下分", "下分", "/user/saveWithdrawOrder", memberTranslate.getId(), 3, 0);
        SysPermission resetPassword = new SysPermission("充值登录密码", "充值登录密码", "/user/resetPassword", memberTranslate.getId(), 3, 0);
        SysPermission withdrawPassword = new SysPermission("重置提现密码", "重置提现密码", "/user/withdrawPassword", memberTranslate.getId(), 3, 0);
        SysPermission saveUser = new SysPermission("新增客户", "新增用户", "/user/saveUser", memberTranslate.getId(), 3, 0);
        SysPermission refreshWM = new SysPermission("WM余额", "WM余额", "/user/refreshWM", memberTranslate.getId(), 3, 0);
        SysPermission updateUserStatus = new SysPermission("用户禁用/启用", "用户禁用/启用", "/user/updateUserStatus", memberTranslate.getId(), 3, 0);
        SysPermission updateUser = new SysPermission("编辑客户", "编辑客户", "/user/updateUser", memberTranslate.getId(), 3, 0);
        SysPermission boundList = new SysPermission("银行卡列表", "银行卡列表", "/bankcard/boundList", memberTranslate.getId(), 3, 0);
        SysPermission bound = new SysPermission("绑定银行卡", "绑定银行卡", "/bankcard/bound", memberTranslate.getId(), 3, 0);
        SysPermission userWashCodeList = new SysPermission("用户洗码查询", "用户洗码查询", "/UserWashCodeConfig/findAll", memberTranslate.getId(), 3, 0);
        SysPermission updateWashCodeConfigs = new SysPermission("编辑用户洗码设置", "编辑用户洗码设置", "/UserWashCodeConfig/updateWashCodeConfigs", memberTranslate.getId(), 3, 0);
        SysPermission findOrderList = new SysPermission("WM资金明细", "WM资金明细", "/order/findOrderList", memberTranslate.getId(), 3, 0);
        SysPermission findAccountChangePage = new SysPermission("会员账变", "会员账变", "/accountChange/findAccountChangePage", memberTranslate.getId(), 3, 0);
        SysPermission rechargeTurnover = new SysPermission("充值流水", "充值流水", "/rechargeTurnover/findPage", memberTranslate.getId(), 3, 0);
        SysPermission findCodeNumChangeList = new SysPermission("会员打码量", "会员打码量", "/codeNumChange/findCodeNumChangeList", memberTranslate.getId(), 3, 0);
        SysPermission findIp = new SysPermission("IP地址", "IP地址查询", "/user/findIp", memberTranslate.getId(), 3, 0);
        thridPermissions.add(saveCharge);
        thridPermissions.add(withdrawOrder);
        thridPermissions.add(resetPassword);
        thridPermissions.add(withdrawPassword);
        thridPermissions.add(saveUser);
        thridPermissions.add(refreshWM);
        thridPermissions.add(updateUserStatus);
        thridPermissions.add(updateUser);
        thridPermissions.add(boundList);
        thridPermissions.add(bound);
        thridPermissions.add(userWashCodeList);
        thridPermissions.add(updateWashCodeConfigs);
        thridPermissions.add(findOrderList);
        thridPermissions.add(findAccountChangePage);
        thridPermissions.add(rechargeTurnover);
        thridPermissions.add(findCodeNumChangeList);
        thridPermissions.add(findIp);

        //代理管理下菜单
        SysPermission resetPasswords = new SysPermission("重置密码", "重置密码", "/proxyUser/resetPasswords", agentSystem.getId(), 3, 0);
        SysPermission saveProxyUser = new SysPermission("新增代理", "新增代理", "/proxyUser/saveProxyUser", agentSystem.getId(), 3, 0);
        SysPermission updateUserFlag = new SysPermission("锁定", "锁定", "/proxyUser/updateUserFlag", agentSystem.getId(), 3, 0);
        SysPermission proxyRebateConfig = new SysPermission("全局返佣配置", "全局返佣配置", "/proxyRebateConfig/findAll", agentSystem.getId(), 3, 0);
        SysPermission findUsersNum = new SysPermission("直属下级玩家查询", "直属下级玩家查询", "/proxyUser/findUsersNum", agentSystem.getId(), 3, 0);
        SysPermission updateProxyRebate = new SysPermission("个人返佣设置", "个人返佣设置", "/proxyRebateConfig/updateProxyRebate", agentSystem.getId(), 3, 0);
        thridPermissions.add(resetPasswords);
        thridPermissions.add(saveProxyUser);
        thridPermissions.add(updateUserFlag);
        thridPermissions.add(proxyRebateConfig);
        thridPermissions.add(findUsersNum);
        thridPermissions.add(updateProxyRebate);

        SysPermission findSum = new SysPermission("代理日报表统计", "代理报表统计", "/companyProxyDetail/findSum", agentReport.getId(), 3, 0);
        SysPermission companyProxyDetail = new SysPermission("代理月报表统计", "代理月报表统计", "/companyProxyDetail/findSum", agentMonthReport.getId(), 3, 0);
        thridPermissions.add(findSum);
        thridPermissions.add(companyProxyDetail);

        SysPermission updateChargeOrder = new SysPermission("充值审核", "充值审核", "/chargeOrder/updateChargeOrder", chargeOrder.getId(), 3, 0);
        SysPermission saveWithdraw = new SysPermission("提现审核", "提现审核", "/withdraw/saveWithdraw", withdrawalOrder.getId(), 3, 0);
        thridPermissions.add(updateChargeOrder);
        thridPermissions.add(saveWithdraw);

        SysPermission updateBankStatus = new SysPermission("禁用/启用", "禁用/启用", "/bankcard/updateBankStatus", bankChannel.getId(), 3, 0);
        SysPermission updateBankInfo = new SysPermission("编辑", "编辑", "/bankcard/updateBankInfo", bankChannel.getId(), 3, 0);
        thridPermissions.add(updateBankStatus);
        thridPermissions.add(updateBankInfo);

        SysPermission banklist = new SysPermission("银行渠道列表", "银行渠道列表", "/bankcard/banklist", collectionSet.getId(), 3, 0);
        SysPermission updateDisable = new SysPermission("上架", "上架", "/collection/updateDisable", collectionSet.getId(), 3, 0);
        SysPermission deleteBankInfo = new SysPermission("删除", "删除", "/collection/deleteBankInfo", collectionSet.getId(), 3, 0);
        SysPermission saveBankInfo = new SysPermission("新增", "新增", "/collection/saveBankInfo", collectionSet.getId(), 3, 0);
        thridPermissions.add(banklist);
        thridPermissions.add(updateDisable);
        thridPermissions.add(deleteBankInfo);
        thridPermissions.add(saveBankInfo);

        SysPermission findWithdrawConfig = new SysPermission("提款配置查询", "提款配置查询", "/withdrawConfig/findWithdrawConfig", platformConfig.getId(), 3, 0);
        SysPermission saveChargeConfig = new SysPermission("充值配置修改", "充值配置修改", "/chargeConfig/saveChargeConfig", platformConfig.getId(), 3, 0);
        SysPermission saveWithdrawConfig = new SysPermission("提现配置修改", "提现配置修改", "/withdrawConfig/saveWithdrawConfig", platformConfig.getId(), 3, 0);
        SysPermission betRatioConfig = new SysPermission("打码倍率查询", "打码倍率查询", "/betRatioConfig/findAll", platformConfig.getId(), 3, 0);
        SysPermission betRatioConfigUpdate = new SysPermission("打码倍率编辑", "打码倍率编辑", "/betRatioConfig/update", platformConfig.getId(), 3, 0);
        SysPermission findCommission = new SysPermission("推广配置查询", "推广配置查询", "/platformConfig/findCommission", platformConfig.getId(), 3, 0);
        SysPermission updateCommission = new SysPermission("推广配置编辑", "推广配置编辑", "/platformConfig/updateCommission", platformConfig.getId(), 3, 0);
        SysPermission findDomainName = new SysPermission("域名配置查询", "域名配置查询", "/platformConfig/findDomainName", platformConfig.getId(), 3, 0);
        SysPermission updateDomainName = new SysPermission("域名配置编辑", "域名配置编辑", "/platformConfig/updateDomainName", platformConfig.getId(), 3, 0);
        SysPermission findRegisterSwitch = new SysPermission("注册开关查询", "注册开关查询", "/platformConfig/findRegisterSwitch", platformConfig.getId(), 3, 0);
        SysPermission updateRegisterSwitch = new SysPermission("注册开关编辑", "注册开关编辑", "/platformConfig/updateRegisterSwitch", platformConfig.getId(), 3, 0);
        SysPermission findRiskConfig = new SysPermission("平台风险配置", "平台风险配置", "/managementRisk/findRiskConfig", platformConfig.getId(), 3, 0);
        SysPermission saveRiskConfig = new SysPermission("平台风险编辑", "平台风险编辑", "/managementRisk/saveRiskConfig", platformConfig.getId(), 3, 0);
        thridPermissions.add(findWithdrawConfig);
        thridPermissions.add(saveChargeConfig);
        thridPermissions.add(saveWithdrawConfig);
        thridPermissions.add(betRatioConfig);
        thridPermissions.add(betRatioConfigUpdate);
        thridPermissions.add(findCommission);
        thridPermissions.add(updateCommission);
        thridPermissions.add(findDomainName);
        thridPermissions.add(updateDomainName);
        thridPermissions.add(findRegisterSwitch);
        thridPermissions.add(updateRegisterSwitch);
        thridPermissions.add(findRiskConfig);
        thridPermissions.add(saveRiskConfig);

        SysPermission updateProxy = new SysPermission("保存", "保存", "/proxyRebate/updateProxyRebate", agentPromotion.getId(), 3, 0);
        thridPermissions.add(updateProxy);

        SysPermission washCodeConfigs = new SysPermission("编辑", "编辑", "/washCodeConfig/updateWashCodeConfigs", gameConfig.getId(), 3, 0);
        thridPermissions.add(washCodeConfigs);

        SysPermission savePCPicture = new SysPermission("编辑PC轮播图", "编辑轮播图", "/picture/savePCPicture", pcBanner.getId(), 3, 0);
        SysPermission savePicture = new SysPermission("编辑移动端轮播图", "编辑轮播图", "/picture/savePCPicture", mobileBanner.getId(), 3, 0);
        thridPermissions.add(savePCPicture);
        thridPermissions.add(savePicture);

        SysPermission deleteNotice = new SysPermission("上下架", "上下架", "/notice/deleteNotice", noticeConfig.getId(), 3, 0);
        SysPermission updateNotice = new SysPermission("编辑", "编辑", "/notice/updateNotice", noticeConfig.getId(), 3, 0);
        SysPermission saveNotice = new SysPermission("新增", "新增", "/notice/saveNotice", noticeConfig.getId(), 3, 0);
        thridPermissions.add(deleteNotice);
        thridPermissions.add(updateNotice);
        thridPermissions.add(saveNotice);

        SysPermission updateKeyCustomer = new SysPermission("保存", "保存", "/customer/updateKeyCustomer", customerConfig.getId(), 3, 0);
        thridPermissions.add(updateKeyCustomer);

        SysPermission disable = new SysPermission("删除", "删除", "/ipBlack/disable", IPConfig.getId(), 3, 0);
        thridPermissions.add(disable);

        SysPermission saveDownloadStation = new SysPermission("保存", "保存", "/download/saveDownloadStation", APPMeange.getId(), 3, 0);
        thridPermissions.add(saveDownloadStation);

        SysPermission findPermissionList = new SysPermission("权限查询", "权限查询", "/role/findPermissionList", roleUserList.getId(), 3, 0);
        SysPermission updatePermissionList = new SysPermission("编辑权限", "编辑权限", "/role/updatePermissionList", roleUserList.getId(), 3, 0);
        SysPermission getRoleList = new SysPermission("查询角色数据", "查询角色数据", "/role/getRoleList", roleUserList.getId(), 3, 0);
        SysPermission getUserRoleBind = new SysPermission("绑定用户角色", "绑定用户角色", "/role/getUserRoleBind", sysUserList.getId(), 3, 0);
        SysPermission getSysUser = new SysPermission("查询用户数据", "查询用户数据", "/role/getSysUser", sysUserList.getId(), 3, 0);
//        SysPermission addPermissionList = new SysPermission("添加权限表数据", "添加权限表数据", "/role/addPermissionList", roleUserList.getId(), 3, 0);
//        SysPermission deletePermissionList = new SysPermission("删除权限表数据", "删除权限表数据", "/role/deletePermissionList", sysUserList.getId(), 3, 0);
        SysPermission deleteRoleList = new SysPermission("删除角色", "删除角色", "/role/deleteRoleList", sysUserList.getId(), 3, 0);
        thridPermissions.add(findPermissionList);
        thridPermissions.add(updatePermissionList);
        thridPermissions.add(getRoleList);
        thridPermissions.add(getUserRoleBind);
        thridPermissions.add(getSysUser);
//        thridPermissions.add(addPermissionList);
//        thridPermissions.add(deletePermissionList);
        thridPermissions.add(deleteRoleList);


        sysPermissionService.saveAllList(thridPermissions);
        return null;
    }
}
