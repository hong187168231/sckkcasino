package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
     * 注意：jpa 是从第0页开始的
     * @return
     */
    @ApiOperation("用户列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "size", value = "每页大小(默认10条)", required = true),
            @ApiImplicitParam(name = "current", value = "当前页(默认第一页)", required = true),
            @ApiImplicitParam(name = "name", value = "用户名", required = true),
            @ApiImplicitParam(name = "id", value = "用户id", required = true),
    })
    @GetMapping("findUserPage")
    public ResponseEntity findUserPage(Integer size,Integer current,String name,Long id){
        if(size == null || current == null){
            return ResponseUtil.parameterNotNull();
        }
        //后续扩展加参数。
        User user = new User();
        user.setId(id);
        user.setName(name);
        return ResponseUtil.success(userService.findUserPage(current, size, user));
    }
}
