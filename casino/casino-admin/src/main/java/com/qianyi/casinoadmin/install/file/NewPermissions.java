package com.qianyi.casinoadmin.install.file;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.SysPermission;
import com.qianyi.casinocore.service.SysPermissionService;
import com.qianyi.casinocore.util.CommonConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 新添加权限在此方法中写
 *
 */
@Configuration
public class NewPermissions {

    @Autowired
    private SysPermissionService sysPermissionService;

    public void addNewPermission() {
        List<SysPermission> sysPermissionList = sysPermissionService.findAll();
        if(LoginUtil.checkNull(sysPermissionList)){
            return;
        }
        sysPermissionList = removeSysPermission(sysPermissionList);
        List<SysPermission> sysPermissions = new ArrayList<>();

        Map<String, SysPermission> collect = sysPermissionList.stream().collect(Collectors.toMap(SysPermission::getUrl, sysPermission -> sysPermission));
        if(collect.containsKey("/systemCenter")){
            //设置平台配置
            setSystemConfig(collect);
        }
        if(collect.containsKey("/operateCenter")){
            //设置运营客服中心配置
            setCustomerConfigure(collect);
        }
        if(collect.containsKey("/agentCenter")){
            //设置代理中心
            setAgentCenter(collect);
        }
        if(collect.containsKey("/orderCenter")){
            //设置订单中心
            setOrderCenter(collect);
        }
        if(collect.containsKey("/reportCenter")){
            //报表管理
            setReportCenter(collect);
        }
        if(collect.containsKey("/memberCenter")){
            //报表管理
            setMemberCenter(collect);
        }
        if(!collect.containsKey("/thirdGameMange")){
            //第三方游戏管理
            thirdGameMange();
        }
        sysPermissionService.saveAllList(sysPermissions);
    }

    private void thirdGameMange() {
        SysPermission sysPermission = new SysPermission("第三方游戏管理", "第三方游戏管理", "/thirdGameMange", 0l, CommonConst.NUMBER_1, CommonConst.NUMBER_0);
        sysPermissionService.save(sysPermission);
        List<SysPermission> sysPermissions = new ArrayList<>();
        SysPermission pgPlatform = new SysPermission("PG电子游戏管理", "PG电子游戏管理", "/adGame/platformListPG", sysPermission.getId(), CommonConst.NUMBER_2, CommonConst.NUMBER_0);
        SysPermission cq9Permission = new SysPermission("CQ9电子游戏管理", "CQ9电子游戏管理", "/adGame/platformListCQ9", sysPermission.getId(), CommonConst.NUMBER_2, CommonConst.NUMBER_0);
        SysPermission wmPermission = new SysPermission("WM电子游戏管理", "WM电子游戏管理", "/adGame/platformListWM", sysPermission.getId(), CommonConst.NUMBER_2, CommonConst.NUMBER_0);
        sysPermissions.add(pgPlatform);
        sysPermissions.add(cq9Permission);
        sysPermissions.add(wmPermission);
        sysPermissionService.saveAllList(sysPermissions);
        List<SysPermission> pgPermissions = new ArrayList<>();
        List<SysPermission> cq9Permissions = new ArrayList<>();
        List<SysPermission> wmPermissions = new ArrayList<>();
        SysPermission pgAdgamePlatform = new SysPermission("游戏列表", "游戏列表", "/adGame/findGameList", pgPlatform.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);
        SysPermission pgPlatformUpdate = new SysPermission("平台是否维护", "平台是否维护", "/adGame/updatePlatformStatus", pgPlatform.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);
        SysPermission pgAdGameUpdate = new SysPermission("游戏状态修改", "游戏状态修改", "/adGame/updateDomainName", pgPlatform.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);
        pgPermissions.add(pgAdgamePlatform);
        pgPermissions.add(pgPlatformUpdate);
        pgPermissions.add(pgAdGameUpdate);
        sysPermissionService.saveAllList(pgPermissions);
        SysPermission cq9AdgamePlatform = new SysPermission("游戏列表", "游戏列表", "/adGame/findGameList", cq9Permission.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);
        SysPermission cq9PlatformUpdate = new SysPermission("平台是否维护", "平台是否维护", "/adGame/updatePlatformStatus", cq9Permission.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);
        SysPermission cq9AdGameUpdate = new SysPermission("游戏状态修改", "游戏状态修改", "/adGame/updateDomainName", cq9Permission.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);

        cq9Permissions.add(cq9AdgamePlatform);
        cq9Permissions.add(cq9PlatformUpdate);
        cq9Permissions.add(cq9AdGameUpdate);
        sysPermissionService.saveAllList(cq9Permissions);

        SysPermission wm9AdgamePlatform = new SysPermission("游戏列表", "游戏列表", "/adGame/findGameList", wmPermission.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);
        SysPermission wm9PlatformUpdate = new SysPermission("平台是否维护", "平台是否维护", "/adGame/updatePlatformStatus", wmPermission.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);
        SysPermission wm9AdGameUpdate = new SysPermission("游戏状态修改", "游戏状态修改", "/adGame/updateDomainName", wmPermission.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);

        wmPermissions.add(wm9AdgamePlatform);
        wmPermissions.add(wm9PlatformUpdate);
        wmPermissions.add(wm9AdGameUpdate);
        sysPermissionService.saveAllList(wmPermissions);
    }

    private static ArrayList<SysPermission> removeSysPermission( List<SysPermission> sysPermissionList){
        Set<SysPermission> set = new TreeSet<SysPermission>(new Comparator<SysPermission>() {
            @Override
            public int compare(SysPermission o1, SysPermission o2) {
                return o1.getUrl().compareTo(o2.getUrl());
            }
        });
        set.addAll(sysPermissionList);
        return new ArrayList<>(set);
    }

    private void setMemberCenter(Map<String, SysPermission> collect){
        if (collect.containsKey("/user/findUserList")) {
            Long pid = collect.get("/user/findUserList").getId();
            if (!collect.containsKey("/user/refreshPGAndCQ9")) {
                SysPermission sysPermission = new SysPermission("查询用户PG/CQ9余额", "查询用户PG/CQ9余额", "/user/refreshPGAndCQ9", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/user/oneKeyRecoverApi")) {
                SysPermission sysPermission = new SysPermission("一键回收用户PG/CQ9余额", "一键回收用户PG/CQ9余额", "/user/oneKeyRecoverApi", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/user/findUserTotal")) {
                SysPermission sysPermission = new SysPermission("当前查询结果总计", "当前查询结果总计", "/user/findUserTotal", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/bankcard/unboundBankName")) {
                SysPermission sysPermission = new SysPermission("解除银行卡实名认证", "解除银行卡实名认证", "/bankcard/unboundBankName", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }

        }
    }
    /**
     * 报表管理
     * @param collect
     */
    private void setReportCenter(Map<String, SysPermission> collect) {
        if (collect.containsKey("/reportCenter")) {
            Long pid = collect.get("/reportCenter").getId();
            if (!collect.containsKey("/report/queryPersonReport")) {
                SysPermission sysPermission = new SysPermission("历史盈亏报表", "历史盈亏报表", "/report/queryPersonReport", pid, CommonConst.NUMBER_2, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
                if(sysPermission.getId() != null){
                    SysPermission sysPermission1 = new SysPermission("盈亏报表总计", "盈亏报表总计", "/report/queryTotal", sysPermission.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                    sysPermissionService.save(sysPermission1);
                }
            }
            if (!collect.containsKey("/inTimeReport/find")) {
                SysPermission sysPermission = new SysPermission("代理对账报表", "代理对账报表", "/inTimeReport/find", pid, CommonConst.NUMBER_2, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/companyReport/find")) {
                SysPermission sysPermission = new SysPermission("普通会员报表", "普通会员报表", "/companyReport/find", pid, CommonConst.NUMBER_2, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
        }
    }


    /**
     * 设置订单中心
     * @param collect
     */
    private void setOrderCenter(Map<String, SysPermission> collect) {
        if (collect.containsKey("/chargeOrder/chargeOrderList")) {
            Long pid = collect.get("/chargeOrder/chargeOrderList").getId();
            if (!collect.containsKey("/chargeOrder/updateChargeOrdersRemark")) {
                SysPermission sysPermission = new SysPermission("修改充值备注", "修改充值备注", "/chargeOrder/updateChargeOrdersRemark", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }

        }
        if (collect.containsKey("/withdraw/withdrawList")) {
            Long pid = collect.get("/withdraw/withdrawList").getId();
            if (!collect.containsKey("/withdraw/updateWithdrawOrderRemark")) {
                SysPermission sysPermission = new SysPermission("修改提现备注", "修改提现备注", "/withdraw/updateWithdrawOrderRemark", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }

        }
    }
    /**
     * 设置代理中心
     * @param collect
     */
    private void setAgentCenter(Map<String, SysPermission> collect) {
        if(collect.containsKey("/proxyUser/findProxyUser")){
            Long pid = collect.get("/proxyUser/findProxyUser").getId();
            if (!collect.containsKey("/proxyUser/transferUser")){
                SysPermission sysPermission = new SysPermission("转移会员", "转移会员", "/proxyUser/transferUser", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/proxyUser/transferProxy")){
                SysPermission sysPermission = new SysPermission("转移代理", "转移代理", "/proxyUser/transferProxy", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/proxyUser/delete")){
                SysPermission sysPermission = new SysPermission("删除", "删除", "/proxyUser/delete", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
        }
    }

    /**
     * 运营中心 - 客服中心配置
     * @param collect
     */
    private void setCustomerConfigure(Map<String, SysPermission> collect) {
        SysPermission save =new SysPermission();
        if(!collect.containsKey("/customer/findCustomerList")){
            //先删除之前的菜单
            SysPermission byUrl = sysPermissionService.findByUrl("/customer/findCustomer");
            SysPermission updateByUrl = sysPermissionService.findByUrl("/customer/updateKeyCustomer");
            if (byUrl!=null){
                sysPermissionService.deleteById(byUrl.getId());
            }
            if (updateByUrl!=null){
                sysPermissionService.deleteById(updateByUrl.getId());
            }
            Long pid = collect.get("/operateCenter").getId();
            SysPermission sysConfigPermission = new SysPermission("客服中心配置", "客服中心配置", "/customer/findCustomerList", pid, 2, 0);
            save = sysPermissionService.save(sysConfigPermission);
        }
        if(save.getUrl()!=null && save.getUrl().equals("/customer/findCustomerList")){
            Long pid = save.getId();
            if(!collect.containsKey("/customer/updateKeyCustomerConfigure")){
                SysPermission sysPermission = new SysPermission("保存", "保存", "/customer/updateKeyCustomerConfigure", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
        }

        //人人贷开关
        if(collect.containsKey("/chargeConfig/findChargeConfig")){
            Long pid = collect.get("/chargeConfig/findChargeConfig").getId();
            if (!collect.containsKey("/platformConfig/findPeopleProxySwitch")){
                SysPermission sysPermission = new SysPermission("查询人人代开关", "查询人人代开关", "/platformConfig/findPeopleProxySwitch", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/platformConfig/updatePeopleProxySwitch")){
                SysPermission sysPermission = new SysPermission("编辑人人代开关", "编辑人人代开关", "/platformConfig/updatePeopleProxySwitch", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/platformConfig/findBankcardRealNameSwitch")){
                SysPermission sysPermission = new SysPermission("查询银行卡账号校验开关", "查询银行卡账号校验开关", "/platformConfig/findBankcardRealNameSwitch", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/platformConfig/updateBankcardRealNameSwitch")){
                SysPermission sysPermission = new SysPermission("编辑银行卡账号校验开关", "编辑银行卡账号校验开关", "/platformConfig/updateBankcardRealNameSwitch", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/platformConfig/findPlatformMaintenance")){
                SysPermission sysPermission = new SysPermission("查询平台维护开关", "查询平台维护开关", "/platformConfig/findPlatformMaintenance", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/platformConfig/updatePlatformMaintenance")){
                SysPermission sysPermission = new SysPermission("修改平台维护开关", "修改平台维护开关", "/platformConfig/updatePlatformMaintenance", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
        }
    }
    private void setSystemConfig(Map<String, SysPermission> collect) {
        if(!collect.containsKey("/systemMessage/systemConfig")){
            Long pid = collect.get("/systemCenter").getId();
            SysPermission sysConfigPermission = new SysPermission("系统配置", "系统配置", "/systemMessage/systemConfig", pid, 2, 0);
            sysPermissionService.save(sysConfigPermission);
            if(collect.containsKey("/platformConfig/updateDomainName")){
                SysPermission sysPermission = collect.get("/platformConfig/updateDomainName");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/findDomainName")){
                SysPermission sysPermission = collect.get("/platformConfig/findDomainName");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/updateUploadUrl")){
                SysPermission sysPermission = collect.get("/platformConfig/updateUploadUrl");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/findUploadUrl")){
                SysPermission sysPermission = collect.get("/platformConfig/findUploadUrl");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/updateReadUploadUrl")){
                SysPermission sysPermission = collect.get("/platformConfig/updateReadUploadUrl");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/findReadUploadUrl")){
                SysPermission sysPermission = collect.get("/platformConfig/findReadUploadUrl");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/updateMoneySymbol")){
                SysPermission sysPermission = collect.get("/platformConfig/updateMoneySymbol");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/findMoneySymbol")){
                SysPermission sysPermission = collect.get("/platformConfig/findMoneySymbol");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/updateWebConfiguration")){
                SysPermission sysPermission = collect.get("/platformConfig/updateWebConfiguration");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/findWebConfiguration")){
                SysPermission sysPermission = collect.get("/platformConfig/findWebConfiguration");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/findCustomerCode")){
                SysPermission sysPermission = collect.get("/platformConfig/findCustomerCode");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/updateCustomerCode")){
                SysPermission sysPermission = collect.get("/platformConfig/updateCustomerCode");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
        }
        if(collect.containsKey("/systemMessage/systemConfig")){
            Long pid = collect.get("/systemMessage/systemConfig").getId();
            if(collect.containsKey("/platformConfig/findCustomerCode") && !collect.get("/platformConfig/findCustomerCode").getPid().toString().equals(pid.toString()) ){
                SysPermission sysPermission = collect.get("/platformConfig/findCustomerCode");
                sysPermission.setPid(pid);
                //                SysPermission sysPermission = new SysPermission("查询客服脚本的代号", "查询客服脚本的代号", "/platformConfig/findCustomerCode", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/updateCustomerCode") && !collect.get("/platformConfig/updateCustomerCode").getPid().toString().equals(pid.toString())){
                SysPermission sysPermission = collect.get("/platformConfig/updateCustomerCode");
                sysPermission.setPid(pid);
                //                SysPermission sysPermission = new SysPermission("修改客服脚本的代号", "修改客服脚本的代号", "/platformConfig/updateCustomerCode", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
        }

    }
}
