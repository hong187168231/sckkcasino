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

import java.math.BigDecimal;

@RestController
@RequestMapping("user")
@Api(tags = "用户中心")
@Slf4j
public class UserDetailController {

    @Autowired
    private UserDetailService userDetailService;

    @Autowired
    private UserService userService;

    @ApiOperation("用户中心列表")
    @RequestMapping(value = "/findUserPage", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity findUserPage(@RequestBody UserDetailRequest userDetailRequest){
        try {
            return ResponseUtil.success(userDetailService.findUserPage(userDetailRequest));
        }catch (Exception e){
            log.error("userDetail findUserPage error", e);
            return ResponseUtil.userError();
        }
    }

    @NoAuthentication
    @ApiOperation("修改用户状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名", required = true),
            @ApiImplicitParam(name = "status", value = "用户状态", required = true),
    })
    @RequestMapping(value = "/lockUser", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity lockUser(String userName, Integer status){
        if(StringUtils.isEmpty(userName) || status == null){
            return ResponseUtil.parameterNotNull();
        }
        try {
            return userDetailService.lockUser(userName, status);
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



}
