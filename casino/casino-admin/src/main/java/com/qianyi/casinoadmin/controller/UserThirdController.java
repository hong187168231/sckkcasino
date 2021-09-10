package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.model.UserThird;
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

    @ApiOperation("根据我方用户id查询三方账号")
    @GetMapping("/findUserThird")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "我方用户id(目前只有一个参数，后续可以会加)", required = true),
    })
    public ResponseEntity findUserThird(Long userId){
        UserThird byUserId = userThirdService.findByUserId(userId);
        return ResponseUtil.success(byUserId.getAccount());

    }
}
