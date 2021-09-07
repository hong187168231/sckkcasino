package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.IpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "account", value = "用户名", required = false),
            @ApiImplicitParam(name = "id", value = "用户id", required = false),
    })
    @GetMapping("findUserList")
    public ResponseEntity findUserList(Integer pageSize,Integer pageCode,String account,Long id){

        //后续扩展加参数。
        User user = new User();
        user.setId(id);
        user.setAccount(account);
        Sort sort=Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        Page<User> userPage = userService.findUserPage(pageable, user);
        return ResponseUtil.success(userPage);
    }

    @ApiOperation("添加用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "用户名", required = true),
            @ApiImplicitParam(name = "name", value = "用户昵称", required = false),
            @ApiImplicitParam(name = "state", value = "用户状态(1：启用，其他：禁用)", required = false),
            @ApiImplicitParam(name = "phone", value = "电话号码", required = false),
            @ApiImplicitParam(name = "headImg", value = "用户头像", required = false),
            @ApiImplicitParam(name = "password", value = "用户密码", required = false),
            @ApiImplicitParam(name = "language", value = "语言", required = false),

    })
    @PostMapping("saveUser")
    public ResponseEntity saveUser(String account, String name, Integer state, String phone,
                                   String headImg, String password, Integer language){
        User us = userService.findByAccount(account);
        if(us != null){
            return ResponseUtil.custom("账户已存在");
        }

        User user = new User();
        user.setAccount(account);
        if(LoginUtil.checkNull(name)){
            user.setName(account);
        }else{
            user.setName(name);
        }

        if(LoginUtil.checkNull(state)){
            user.setState(Constants.USER_NORMAL);
        }else{
            user.setState(state);
        }

        if(!LoginUtil.checkNull(phone)){
            user.setPhone(phone);
        }

        if(!LoginUtil.checkNull(headImg)){
            user.setHeadImg(headImg);
        }

        if(!LoginUtil.checkNull(language)){
            user.setLanguage(language);
        }
        //默认密码是
        if(LoginUtil.checkNull(password)){
            password = Constants.USER_SET_PASSWORD;
        }
        String bcryptPassword = LoginUtil.bcrypt(password);
        user.setPassword(bcryptPassword);

        //获取ip
        String ip = IpUtil.getIp(LoginUtil.getRequest());
        user.setRegisterIp(ip);
        userService.save(user);
        return ResponseUtil.success();
    }

    /**
     * 修改用户
     *
     * @param id
     * @param state
     * @param phone
     * @param headImg
     * @return
     */
    @ApiOperation("修改用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true),
            @ApiImplicitParam(name = "state", value = "用户状态(1：启用，其他：禁用)", required = false),
            @ApiImplicitParam(name = "phone", value = "电话号码", required = false),
            @ApiImplicitParam(name = "headImg", value = "用户头像", required = false),
            @ApiImplicitParam(name = "password", value = "用户密码", required = false),

    })
    @PostMapping("updateUser")
    public ResponseEntity updateUser(Long id, Integer state, String phone, String headImg, String password){
        //权限功能会过滤权限,此处不用

        //查询用户信息
        User user = userService.findById(id);
        if(user == null){
            return ResponseUtil.parameterNotNull();
        }
        this.setUserParams(user, state, phone, headImg, password);
        userService.save(user);
        return ResponseUtil.success();
    }

    @ApiOperation("删除用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true),
    })
    @PostMapping("deteleUser")
    public ResponseEntity deteleUser(Long id){
        User user = userService.findById(id);
        if(user == null){
            ResponseUtil.fail();
        }
        userService.deleteById(id);
        return ResponseUtil.success();
    }

    private void setUserParams(User user, Integer state, String phone, String headImg, String password) {
        if(!LoginUtil.checkNull(state)){
            user.setState(state);
        }
        if(!LoginUtil.checkNull(phone)){
            user.setPhone(phone);
        }
        if(!LoginUtil.checkNull(headImg)){
            user.setHeadImg(headImg);
        }
        if(!LoginUtil.checkNull(password)){
            String bcryptPassword = LoginUtil.bcrypt(password);
            user.setPassword(bcryptPassword);
        }
    }


}
