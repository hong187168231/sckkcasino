package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.SysPermission;
import com.qianyi.casinocore.model.SysPermissionRole;
import com.qianyi.casinocore.model.SysRole;
import com.qianyi.casinocore.service.SysPermissionRoleService;
import com.qianyi.casinocore.service.SysPermissionService;
import com.qianyi.casinocore.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceBusiness {

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysPermissionRoleService sysPermissionRoleService;

    @Autowired
    private SysPermissionService sysPermissionService;

    @Transactional
    public Boolean save(String roleName, String remark, Long roleId, List<Long> menuIdList) {
        if(roleId != null){
            SysRole byId = sysRoleService.findById(roleId);
            if(byId == null){
                return false;
            }
            sysPermissionRoleService.delete(roleId);
        }else{
            SysRole sysRole = new SysRole();
            String name = "ROLE_" + roleName;
            sysRole.setName(name);
            sysRole.setRoleName(roleName);
            sysRole.setRemark(remark);
            SysRole role = sysRoleService.save(sysRole);
            if(role == null || role.getId() == null){
                return false;
            }
            roleId = sysRole.getId();
        }

        List<SysPermissionRole> sysPermissionRoleList = new ArrayList<>();
        for (Long id : menuIdList) {
            SysPermissionRole sysPermissionRole = new SysPermissionRole();
            sysPermissionRole.setRoleId(roleId);
            sysPermissionRole.setPermissionId(id);
            sysPermissionRoleList.add(sysPermissionRole);
        }
        sysPermissionRoleService.saveAll(sysPermissionRoleList);
        return true;
    }

    public List<SysPermission> getSysPermissionList(Long roleId) {
        List<SysPermissionRole> byRoleId = sysPermissionRoleService.findByRoleId(roleId);
        List<Long> permissionIds = byRoleId.stream().map(SysPermissionRole::getPermissionId).collect(Collectors.toList());
        List<SysPermission> sysPermissionList = sysPermissionService.findByPromission(permissionIds);
        return sysPermissionList;
    }


}
