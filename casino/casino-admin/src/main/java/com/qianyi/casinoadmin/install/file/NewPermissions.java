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
        sysPermissionService.saveAllList(sysPermissions);
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

    /**
     * 运营中心 - 客服中心配置
     * @param collect
     */
    private void setCustomerConfigure(Map<String, SysPermission> collect) {
        SysPermission save =null;
        if(!collect.containsKey("/customer/findCustomerList")){
            Long pid = collect.get("/operateCenter").getId();
            SysPermission sysConfigPermission = new SysPermission("客服中心配置", "客服中心配置", "/customer/findCustomerList", pid, 2, 0);
            save = sysPermissionService.save(sysConfigPermission);
        }
        if(save.getUrl().equals("/customer/findCustomerList")){
            Long pid = save.getId();
            if(!collect.containsKey("/customer/updateKeyCustomerConfigure")){
                SysPermission sysPermission = new SysPermission("保存", "保存", "/customer/updateKeyCustomerConfigure", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
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

        }

        if(collect.containsKey("/systemMessage/systemConfig")){
            Long pid = collect.get("/systemMessage/systemConfig").getId();
            if(!collect.containsKey("/platformConfig/findCustomerCode")){
                SysPermission sysPermission = new SysPermission("查询客服脚本的代号", "查询客服脚本的代号", "/platformConfig/findCustomerCode", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if(!collect.containsKey("/platformConfig/updateCustomerCode")){
                SysPermission sysPermission = new SysPermission("修改客服脚本的代号", "修改客服脚本的代号", "/platformConfig/updateCustomerCode", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
        }
    }
}
