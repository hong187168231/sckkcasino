package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.SysPermission;
import com.qianyi.casinocore.model.SysPermissionRole;
import com.qianyi.casinocore.model.SysRole;
import com.qianyi.casinocore.model.SysUserRole;
import com.qianyi.casinocore.service.*;
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

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Transactional
    public Boolean save(String roleName, String remark, Long roleId, List<Long> menuIdList) {
        if(roleId != null){
            SysRole sysRole = sysRoleService.findById(roleId);
            if(sysRole == null){
                return false;
            }
            String name = "ROLE_" + roleName;
            sysRole.setName(name);
            sysRole.setRoleName(roleName);
            sysRole.setRemark(remark);
            SysRole role = sysRoleService.save(sysRole);
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


    public List<SysRole> findRoleList(Long roleId, Long userId) {
        if(roleId != null){
            List<SysRole> sysRoleList = new ArrayList<>();
            SysRole sysRole = sysRoleService.findById(roleId);
            if(sysRole != null){
                sysRoleList.add(sysRole);
            }
            return sysRoleList;
        }

        if(userId != null){
            List<SysRole> sysRoleList = new ArrayList<>();
            SysUserRole sysUserRole = sysUserRoleService.findbySysUserId(userId);
            SysRole sysRole = sysRoleService.findById(sysUserRole.getSysRoleId());
            if(sysRole != null){
                sysRoleList.add(sysRole);
            }
            return sysRoleList;
        }

        return sysRoleService.findAll();
    }

    public void saveSysUserRole(SysUserRole sysUserRole) {
        SysUserRole sys = sysUserRoleService.save(sysUserRole);

    }

    public Boolean savePermission(String descritpion, String name, Long pid, String permissionUrl) {
        SysPermission sys = sysPermissionService.findById(pid);
        if(sys == null){
            return false;
        }

        SysPermission sysPermission = new SysPermission();
        sysPermission.setName(name);
        sysPermission.setUrl(permissionUrl);
        sysPermission.setPid(pid);
        sysPermission.setMenuLevel(sys.getMenuLevel() + 1);
        if(descritpion != null && !sysPermission.equals("")){
            sysPermission.setDescritpion(descritpion);
        }else{
            sysPermission.setDescritpion(name);
        }
        sysPermissionService.save(sysPermission);
        return true;
    }
}
