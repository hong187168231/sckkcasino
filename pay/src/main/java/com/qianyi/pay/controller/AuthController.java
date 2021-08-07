package com.qianyi.pay.controller;

import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "认证中心 控制器")
@RestController
@RequestMapping("auth")
public class AuthController {

    @NoAuthentication
    @ApiOperation("帐密登陆")
    @ApiImplicitParams({
            @ApiImplicitParam(name="account",value="帐号",required = true),
            @ApiImplicitParam(name="password",value="密码",required = true),
            @ApiImplicitParam(name="identification",value="识别码",required = true),
    })
    @PostMapping("loginA/{identification}")
    public ResponseEntity loginA(String account,String password,@PathVariable("identification") String identification) {
        if (ObjectUtils.isEmpty(account) || ObjectUtils.isEmpty(password) || ObjectUtils.isEmpty(identification)) {
            return ResponseUtil.parameterNotNull();
        }

        return ResponseUtil.success();
    }
}
