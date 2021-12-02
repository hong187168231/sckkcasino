package com.qianyi.casinoadmin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinoadmin.vo.SysPermissionVo;
import com.qianyi.casinoadmin.vo.SysUserRoleVo;
import com.qianyi.casinoadmin.vo.SysUserVo;
import com.qianyi.casinocore.business.RoleServiceBusiness;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.SysPermissionService;
import com.qianyi.casinocore.service.SysUserService;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/role")
@Api(tags = "系统管理")
public class RoleController {

    @Autowired
    private SysPermissionService sysPermissionService;

    @Autowired
    private RoleServiceBusiness roleServiceBusiness;

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("getSysUser")
    @ApiOperation("查询用户数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sysId", value = "id", required = false),
            @ApiImplicitParam(name = "account", value = "用户名", required = false),
    })
    public ResponseEntity<SysUser> getSysUser(Long sysId, String account) {
        List<SysUser> sysUsers = new ArrayList<>();
        if(sysId != null){
            SysUser sysUser = sysUserService.findById(sysId);
            if(sysUser != null){
                sysUsers.add(sysUser);
            }
        }else if(!LoginUtil.checkNull(account)){
            SysUser byUserName = sysUserService.findByUserName(account);
            if(byUserName != null){
                sysUsers.add(byUserName);
            }
        }else{
            sysUsers = sysUserService.findAll();
        }
        List<Long> userIds = sysUsers.stream().map(SysUser::getId).distinct().collect(Collectors.toList());
        List<SysUserRole> sysUserRoleList = roleServiceBusiness.findSysRoleUserList(userIds);
        if(LoginUtil.checkNull(sysUserRoleList)){
            return ResponseUtil.success(sysUsers);
        }
        List<Long> roleIds = sysUserRoleList.stream().map(SysUserRole::getSysRoleId).distinct().collect(Collectors.toList());
        List<SysRole> sysRoleList = roleServiceBusiness.findRoleIdsList(roleIds);

        List<SysUserRoleVo> sysUserRoleVoList = new ArrayList<>();
        for (SysUserRole sysUserRole : sysUserRoleList) {
            SysUserRoleVo sysUserRoleVo = new SysUserRoleVo();
            BeanUtils.copyProperties(sysUserRole, sysUserRoleVo);
            for (SysRole sysRole : sysRoleList) {
                if(sysRole.getId().intValue() == sysUserRole.getSysRoleId().intValue()){
                    sysUserRoleVo.setRoleName(sysRole.getRoleName());
                }
            }
            if(!LoginUtil.checkNull(sysUserRoleVo)){
                sysUserRoleVoList.add(sysUserRoleVo);
            }
        }
        List<SysUserVo> sysUserVoList = new ArrayList<>();
        for (SysUser sysUser : sysUsers) {
            SysUserVo sysUserVo = new SysUserVo();
            BeanUtils.copyProperties(sysUser, sysUserVo);
            for (SysUserRoleVo sysUserRoleVo : sysUserRoleVoList) {
                if(sysUser.getId().intValue() == sysUserRoleVo.getSysUserId().intValue()){
                    sysUserVo.setSysRoleId(sysUserRoleVo.getSysRoleId());
                    sysUserVo.setRoleName(sysUserRoleVo.getRoleName());
                    sysUserVo.setUpdateTime(sysUser.getUpdateTime());
                    if (!LoginUtil.checkNull(sysUser.getUpdateBy())){
                        SysUser byId = sysUserService.findById(Long.parseLong(sysUser.getUpdateBy()));
                        sysUserVo.setUpdateBy(byId==null?"":byId.getUserName());
                    }
                    sysUserVo.setCreateTime(sysUser.getCreateTime());
                    if (!LoginUtil.checkNull(sysUser.getCreateBy())){
                        SysUser byId = sysUserService.findById(Long.parseLong(sysUser.getCreateBy()));
                        sysUserVo.setCreateBy(byId==null?"":byId.getUserName());
                    }
                }
            }
            sysUserVoList.add(sysUserVo);
        }
        return ResponseUtil.success(sysUserVoList);
    }

    /**
     * 权限数据列表
     *
     * @return
     */
    @GetMapping("getRoleList")
    @ApiOperation("查询角色数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleId", value = "角色id", required = false),
            @ApiImplicitParam(name = "userId", value = "用户id", required = false)
    })
    public ResponseEntity<SysRole> getRoleList(Long roleId, Long userId) {
        List<SysRole> sysRoleList = roleServiceBusiness.findRoleList(roleId, userId);
        return ResponseUtil.success(sysRoleList);
    }

    /**
     * 权限数据列表

     * @return
     */
    @GetMapping("deleteRoleList")
    @ApiOperation("删除角色数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleId", value = "角色id", required = true)
    })
    public ResponseEntity<SysRole> deleteRoleList(Long roleId) {
        roleServiceBusiness.deleteRoleList(roleId);
        return ResponseUtil.success();
    }


    @PostMapping("getUserRoleBind")
    @ApiOperation("绑定用户角色")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", required = true),
            @ApiImplicitParam(name = "roleId", value = "角色id", required = true)
    })
    public ResponseEntity<SysRole> getUserRoleBind(Long userId, Long roleId) {
        SysUser sysUser = sysUserService.findById(userId);
        if(sysUser == null){
            return ResponseUtil.custom("用户不存在");
        }
        List<SysRole> sysRoleList = roleServiceBusiness.findRoleList(roleId, userId);
        if(LoginUtil.checkNull(sysRoleList)){
            return ResponseUtil.custom("角色不存在");
        }
        SysUserRole sysUserRole = roleServiceBusiness.getSysUserRole(userId);
        if(sysUserRole == null){
            sysUserRole = new SysUserRole();
        }
        Long loginUserId = LoginUtil.getLoginUserId();
        SysUser sysLogin = sysUserService.findById(loginUserId);
        if (sysLogin!=null){
            sysUser.setUpdateBy(sysLogin.getUserName());
            sysUserService.save(sysUser);
        }
        sysUserRole.setSysRoleId(roleId);
        sysUserRole.setSysUserId(userId);
        roleServiceBusiness.saveSysUserRole(sysUserRole);
        return ResponseUtil.success(sysRoleList);

    }



    /**
     * 权限数据列表
     *
     * @return
     */
    @GetMapping("findPermissionList")
    @ApiOperation("查询权限表数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleId", value = "角色id", required = false),
    })
    public ResponseEntity<SysPermissionVo> findPermissionList(Long roleId) {
        List<SysPermission> sysPermissionList = new ArrayList<>();
        if(roleId != null){
            sysPermissionList = roleServiceBusiness.getSysPermissionList(roleId);
        }else{
            //得到第一层数据
            sysPermissionList = sysPermissionService.findAll();
        }
        List<SysPermission> sysPermissions = sysPermissionList.stream().filter(sysPermission -> sysPermission.getIsDetele() == 0).collect(Collectors.toList());
        if(null == sysPermissions || sysPermissions.size() <= 0){
            return ResponseUtil.success();
        }
        List<SysPermissionVo> sysPermissionVos = JSON.parseArray(JSONObject.toJSONString(sysPermissions), SysPermissionVo.class);
        //得到第三层权限数据
        List<SysPermissionVo> sysPermissionThird = sysPermissionVos.stream().filter(sysPermissionVo -> sysPermissionVo.getMenuLevel() == 3).collect(Collectors.toList());
        List<SysPermissionVo> sysPermissionTwo = sysPermissionVos.stream().filter(sysPermissionVo -> sysPermissionVo.getMenuLevel() == 2).collect(Collectors.toList());
        List<SysPermissionVo> sysPermissionOne = sysPermissionVos.stream().filter(sysPermissionVo -> sysPermissionVo.getMenuLevel() == 1).collect(Collectors.toList());
        for (SysPermissionVo sysTwo : sysPermissionTwo) {
            for (SysPermissionVo sysThrid : sysPermissionThird) {
                if(sysThrid.getPid() == null){
                    continue;
                }
                if(sysTwo.getId().intValue() == sysThrid.getPid().intValue()){
                    sysTwo.getSysPermissionVoList().add(sysThrid);
                }
            }

        }
        sysPermissionOne.stream().forEach(sysOne -> {
            sysPermissionTwo.stream().forEach(sysTwo ->{
                if(sysOne.getId().intValue() == sysTwo.getPid().intValue()){
                    sysOne.getSysPermissionVoList().add(sysTwo);
                }
            });
        });

        return ResponseUtil.success(sysPermissionOne);
    }


    @ApiOperation("编辑角色权限")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleName", value = "角色名称", required = true),
            @ApiImplicitParam(name = "roleId", value = "角色id", required = false),
            @ApiImplicitParam(name = "remark", value = "备注", required = false),
            @ApiImplicitParam(name = "menuIdList", value = "权限id", required = false),
    })
    @PostMapping("/updatePermissionList")
    public ResponseEntity<SysRole> updatePermissionList(String roleName, String remark, Long roleId, @RequestBody List<Long> menuIdList){
        if(LoginUtil.checkNull(roleName)){
            return ResponseUtil.custom("参数错误");
        }
        Boolean result = roleServiceBusiness.save(roleName, remark, roleId, menuIdList);
        if(result){
            return ResponseUtil.success();
        }
        return ResponseUtil.fail();
    }

//    @PostMapping("/addPermissionList")
//    @ApiOperation("添加权限表数据")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "descritpion", value = "权限描述", required = false),
//            @ApiImplicitParam(name = "id", value = "权限描述", required = false),
//            @ApiImplicitParam(name = "name", value = "权限名称", required = true),
//            @ApiImplicitParam(name = "pid", value = "上级id,没有传0", required = true),
//            @ApiImplicitParam(name = "permissionUrl", value = "权限id", required = true),
//    })
//    public ResponseEntity<SysPermission> addPermissionList(String descritpion, String name, Long pid, String permissionUrl, Long id){
//
//        if(LoginUtil.checkNull(name, permissionUrl)){
//            return ResponseUtil.custom("参数错误");
//        }
//        Boolean result = roleServiceBusiness.savePermission(descritpion, name, pid, permissionUrl, id);
//        if(result){
//            return ResponseUtil.success();
//        }
//        return ResponseUtil.fail();
//    }




    @PostMapping("/deletePermissionList")
    @ApiOperation("删除权限表数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "permissionList", value = "权限id", required = true),
    })
    public ResponseEntity<SysPermission> deletePermissionList(@RequestBody List<Long> permissionList){

        if(LoginUtil.checkNull(permissionList)){
            return ResponseUtil.custom("参数错误");
        }
        Boolean result = roleServiceBusiness.deleteAllPermission(permissionList);
        if(result){
            return ResponseUtil.success();
        }
        return ResponseUtil.fail();
    }
}
