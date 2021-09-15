package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.service.UserThirdService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/userThird")
@Api(tags = "用户管理")
public class UserThirdController {
    @Autowired
    private UserThirdService userThirdService;
    @Autowired
    private UserService userService;

    @ApiOperation("根据我方用户账号查询三方账号")
    @GetMapping("/findUserThird")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userAccount", value = "我方用户账号(目前只有一个参数，后续可以会加)", required = true),
    })
    public ResponseEntity findUserThird(String userAccount){
        User user = userService.findByAccount(userAccount);
        UserThird userThird = userThirdService.findByUserId(user.getId());
        return ResponseUtil.success(userThird.getAccount());

    }
}
