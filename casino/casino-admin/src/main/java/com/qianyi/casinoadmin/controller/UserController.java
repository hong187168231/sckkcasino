package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.service.UserService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对用户表进行增删改查操作
 */
@RestController
@RequestMapping("user")
@Api(tags = "用户中心")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;


    /**
     * 查询操作
     * @return
     */
    @ApiOperation("用户列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "size", value = "每页大小", required = true),
            @ApiImplicitParam(name = "current", value = "当前页", required = true),
            @ApiImplicitParam(name = "name", value = "用户名", required = true),
            @ApiImplicitParam(name = "id", value = "当前页", required = true),
    })
    public ResponseEntity findUserPage(Integer size,Integer current,String name,Long id){
        if(size == null || current == null){
            return ResponseUtil.parameterNotNull();
        }
        return ResponseUtil.success();
    }
}
