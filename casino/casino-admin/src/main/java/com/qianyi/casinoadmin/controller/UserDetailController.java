package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.vo.request.UserDetailRequest;
import com.qianyi.casinocore.service.UserDetailService;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("userDetail")
@Api(tags = "用户详情中心，暂时不用")
@Slf4j
public class UserDetailController {

    @Autowired
    private UserDetailService userDetailService;

    @Autowired
    private UserService userService;

    @ApiOperation("用户中心列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "size", value = "每页大小", required = true),
            @ApiImplicitParam(name = "current", value = "当前页", required = true),
            @ApiImplicitParam(name = "registerStartTime", value = "开始时间"),
            @ApiImplicitParam(name = "registerEndTime", value = "结束时间"),
            @ApiImplicitParam(name = "agentLevel", value = "客户身份"),
            @ApiImplicitParam(name = "riskLevel", value = "风险等级"),
            @ApiImplicitParam(name = "vipLevel", value = "VIP等级"),
            @ApiImplicitParam(name = "status", value = "用户状态"),
            @ApiImplicitParam(name = "userId", value = "用户ID"),
            @ApiImplicitParam(name = "userName", value = "用户名称"),
    })
    @GetMapping(value = "/findUserPage")
    public ResponseEntity findUserPage(Integer size,
                                       Integer current,
                                       Date registerStartTime,
                                       Date registerEndTime,
                                       Integer agentLevel,
                                       Integer riskLevel,
                                       Integer vipLevel,
                                       Integer status,
                                       String userId,
                                       String userName
                                       ){
        try {
            UserDetailRequest userDetailRequest =  UserDetailRequest.builder()
                    .status(status)
                    .size(size)
                    .current(current)
                    .registerStartTime(registerStartTime)
                    .registerEndTime(registerEndTime)
                    .agentLevel(agentLevel)
                    .riskLevel(riskLevel)
                    .vipLevel(vipLevel)
                    .userId(userId)
                    .userName(userName)
                    .build();
            return ResponseUtil.success(userDetailService.findUserPage(userDetailRequest));
        }catch (Exception e){
            log.error("userDetail findUserPage error", e);
            return ResponseUtil.success();
        }
    }

    @NoAuthentication
    @ApiOperation("修改用户状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户Id", required = true),
    })
    @PostMapping(value = "/lockUser")
    public ResponseEntity lockUser(String userId, Integer status){
        if(StringUtils.isEmpty(userId) || status == null){
            return ResponseUtil.parameterNotNull();
        }
        try {
            return userDetailService.updateUserStatus(userId);
        }catch (Exception e){
            log.error("用户状态修改操作异常" , e);
            return ResponseUtil.fail();
        }
    }

    @NoAuthentication
    @ApiOperation("修改用户风险等级")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名", required = true),
            @ApiImplicitParam(name = "riskLevel", value = "风险等级", required = true),
    })
    @PostMapping("updateRiskLevel")
    public ResponseEntity updateRiskLevel(String userName, Integer riskLevel){
        if(StringUtils.isEmpty(userName) || riskLevel == null){
            return ResponseUtil.parameterNotNull();
        }
        return userDetailService.updateRiskLevel(userName, riskLevel);
    }


    /**
     * 重置密码，默认是 888888
     * @param userName
     * @return
     */
    @NoAuthentication
    @ApiOperation("重置用户密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名", required = true),
    })
    @PostMapping("resetPassword")
    public ResponseEntity resetPassword(String userName){
        if(StringUtils.isEmpty(userName)){
            return ResponseUtil.parameterNotNull();
        }
        return userService.resetPassword(userName);
    }

    /**
     * 用户详细信息
     *
     * @return
     */
    @NoAuthentication
    @ApiOperation("用户详细信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名", required = true),
    })
    @GetMapping("getUserDetail")
    private ResponseEntity getUserDetail(String userName){
        if(StringUtils.isEmpty(userName)){
            return ResponseUtil.parameterNotNull();
        }
        return userDetailService.getUserDetail(userName);
    }


}
