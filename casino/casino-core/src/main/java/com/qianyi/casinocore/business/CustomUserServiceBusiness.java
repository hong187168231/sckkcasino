package com.qianyi.casinocore.business;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CustomUserServiceBusiness {// 自定义UserDetailsService 接口

    @Autowired
    SysUserService sysUserService;

    @Autowired
    SysPermissionService sysPermissionService;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysPermissionRoleService sysPermissionRoleService;

    @Autowired
    private RedisKeyUtil redisKeyUtil;

    /**
     * 获取当前用户的所有权限
     * 
     * @param id
     * @return
     */
    public List<SysPermission> findByAdminUserId(Long id) {
        try {
            // 得到用户角色表数据
            SysUserRole sysUserRole = sysUserRoleService.findbySysUserId(id);
            if (sysUserRole == null) {
                return Lists.newArrayList();
            }
            // 得到用户的角色
            SysRole sysRole = sysRoleService.findById(sysUserRole.getSysRoleId());
            if (sysRole == null || Objects.isNull(sysRole.getId())) {
                return Lists.newArrayList();
            }
            List<SysPermission> sysPermissionList = new ArrayList<>();
            RList<SysPermission> rList = redisKeyUtil.getSysPermissionList(sysRole.getId().toString());
            if (CollUtil.isNotEmpty(rList)) {
                log.info("查询角色权限走缓存{}rList{}", sysRole.getId(), rList.size());
                sysPermissionList.addAll(rList);
            } else {
                sysPermissionList = findBySysRoleId(sysRole.getId());
                log.info("查询角色权限走数据库{}rList{}", sysRole.getId(), rList.size());
                rList.addAll(sysPermissionList);
            }
            return sysPermissionList;
        } catch (Exception e) {
            throw new UsernameNotFoundException("admin: do not exist!");
        }
    }

    private List<SysPermission> findBySysRoleId(Long id) {
        List<SysPermission> sysPermissionList = new ArrayList<>();
        List<SysPermissionRole> sysPermissionRoles = sysPermissionRoleService.findByRoleId(id);
        if (sysPermissionRoles == null || sysPermissionRoles.size() <= 0) {
            return sysPermissionList;
        }
        List<Long> permissionIds =
            sysPermissionRoles.stream().map(SysPermissionRole::getPermissionId).collect(Collectors.toList());
        sysPermissionList = sysPermissionService.findAllCondition(permissionIds);
        return sysPermissionList;
    }
}
