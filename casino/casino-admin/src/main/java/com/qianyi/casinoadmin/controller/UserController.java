package com.qianyi.casinoadmin.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinoadmin.util.passwordUtil;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对用户表进行增删改查操作
 */
@RestController
@RequestMapping("user")
@Api(tags = "客户中心")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMoneyService userMoneyService;


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
    })
    @GetMapping("findUserList")
    public ResponseEntity findUserList(Integer pageSize,Integer pageCode,String account){

        //后续扩展加参数。
        User user = new User();
        user.setAccount(account);
        Sort sort=Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        Page<User> userPage = userService.findUserPage(pageable, user);
        List<User> userList = userPage.getContent();
        if(userList != null && userList.size() > 0){
            List<Long> userIds = userList.stream().map(User::getId).collect(Collectors.toList());
            List<UserMoney> userMoneyList =  userMoneyService.findAll(userIds);
            if(userMoneyList != null && userMoneyList.size() > 0){
                userList.stream().forEach(u -> {
                    userMoneyList.stream().forEach(userMoney -> {
                        if(u.getId().equals(userMoney.getUserId().intValue())){
                            u.setMoney(userMoney.getMoney());
                            u.setCodeNum(userMoney.getCodeNum());
                        }
                    });
                });
            }
        }

        return ResponseUtil.success(userPage);
    }

    @ApiOperation("添加用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "用户名", required = true),
            @ApiImplicitParam(name = "name", value = "用户昵称", required = false),
            @ApiImplicitParam(name = "phone", value = "电话号码", required = false),
    })
    @PostMapping("saveUser")
    public ResponseEntity saveUser(String account, String name, String phone){
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

        user.setState(Constants.open);

        if(!LoginUtil.checkNull(phone)){
            user.setPhone(phone);
        }

        //默认中文
        user.setLanguage(Constants.USER_LANGUAGE_CH);

        //随机生成
        String password = passwordUtil.getRandomPwd();
        String bcryptPassword = LoginUtil.bcrypt(password);
        user.setPassword(bcryptPassword);

        User save = userService.save(user);
        //userMoney表初始化数据
        UserMoney userMoney=new UserMoney();
        userMoney.setUserId(save.getId());
        userMoneyService.save(userMoney);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", account);
        jsonObject.put("password", password);
        return ResponseUtil.success(jsonObject);
    }

    /**
     * 修改用户
     * 只有修改电话功能，那电话不能为空
     *
     * @param id
     * @param state
     * @param phone
     * @return
     */
    @ApiOperation("修改用户电话")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true),
            @ApiImplicitParam(name = "phone", value = "电话号码", required = true),

    })
    @PostMapping("updateUser")
    public ResponseEntity updateUser(Long id, Integer state, String phone){
        //权限功能会过滤权限,此处不用

        //查询用户信息
        User user = userService.findById(id);
        if(user == null){
            return ResponseUtil.custom("账户不存在");
        }
        user.setPhone(phone);
        userService.save(user);
        return ResponseUtil.success();
    }

//    @ApiOperation("删除用户")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id", value = "用户id", required = true),
//    })
//    @PostMapping("deteleUser")
//    public ResponseEntity deteleUser(Long id){
//        User user = userService.findById(id);
//        if(user == null){
//            return ResponseUtil.custom("账户不存在");
//        }
//        userService.deleteById(id);
//        return ResponseUtil.success();
//    }

    @ApiOperation("修改用户状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true),
    })
    @PostMapping("updateUserStatus")
    public ResponseEntity updateUserStatus(Long id){
        User user = userService.findById(id);
        if(user == null){
            return ResponseUtil.custom("账户不存在");
        }
        //开启状态，冻结
        if(user.getState() == Constants.USER_NORMAL){
            user.setState(Constants.USER_LOCK_ACCOUNT);
        }else{
            user.setState(Constants.USER_NORMAL);
        }
        userService.save(user);
        return ResponseUtil.success();
    }

    @ApiOperation("重置用户提现密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true),
    })
    @PostMapping("withdrawPassword")
    public ResponseEntity withdrawPassword(Long id){
        User user = userService.findById(id);
        if(user == null){
            return ResponseUtil.custom("账户不存在");
        }
        //随机生成
        String withdrawPassword = passwordUtil.getRandomPwd();
        String bcryptPassword = LoginUtil.bcrypt(withdrawPassword);
        user.setWithdrawPassword(bcryptPassword);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", user.getAccount());
        jsonObject.put("withdrawPassword", withdrawPassword);
        return ResponseUtil.success(jsonObject);
    }

    @ApiOperation("重置用户密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true),
    })
    @PostMapping("resetPassword")
    public ResponseEntity resetPassword(Long id){
        User user = userService.findById(id);
        if(user == null){
            return ResponseUtil.custom("账户不存在");
        }
        //随机生成
        String password = passwordUtil.getRandomPwd();
        String bcryptPassword = LoginUtil.bcrypt(password);
        user.setPassword(bcryptPassword);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", user.getAccount());
        jsonObject.put("password", password);
        return ResponseUtil.success(jsonObject);
    }
}
