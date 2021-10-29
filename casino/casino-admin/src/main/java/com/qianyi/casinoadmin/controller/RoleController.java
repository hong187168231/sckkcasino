package com.qianyi.casinoadmin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.business.RoleServiceBusiness;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.SysPermissionService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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

    /**
     * 权限数据列表
     *
     * @return
     */
    @GetMapping("findPermissionList")
    @ApiOperation("查询权限略表数据")
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
    @PostMapping("/findIp")
    public ResponseEntity<SysRole> findIp(String roleName, String remark, Long roleId, @RequestBody List<Long> menuIdList){
        if(LoginUtil.checkNull(roleName)){
            return ResponseUtil.custom("参数错误");
        }
        Boolean result = roleServiceBusiness.save(roleName, remark, roleId, menuIdList);
        if(result){
            return ResponseUtil.success();
        }
        return ResponseUtil.fail();
    }


}
