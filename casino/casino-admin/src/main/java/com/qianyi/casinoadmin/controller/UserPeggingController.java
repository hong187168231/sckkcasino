package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.CommonConst;
import com.qianyi.casinoadmin.util.CommonUtil;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.model.LoginLog;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.BankcardsService;
import com.qianyi.casinocore.service.LoginLogService;
import com.qianyi.casinocore.service.UserService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/userPegging")
@Api(tags = "客户中心")
public class UserPeggingController {

    @Autowired
    private LoginLogService loginLogService;

    @Autowired
    private UserService userService;

    @Autowired
    private BankcardsService bankcardsService;
    /**
     * 查询操作
     * 注意：jpa 是从第0页开始的
     @param tag tag 反差类型 0 ip地址 1 银行卡号
     @param context 搜索内容
      * @return
     */
    @ApiOperation("客户反查")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tag", value = "tag 反差类型 0 ip地址 1 银行卡号", required = true),
            @ApiImplicitParam(name = "context", value = "context 搜索内容", required = true),
    })
    @GetMapping("findUserPegging")
    public ResponseEntity findUserPegging(Integer tag, String context){
        if (LoginUtil.checkNull(tag,context)){
            return ResponseUtil.custom("参数不合法");
        }
        if (tag == CommonConst.NUMBER_0){//反查ip
            User user = new User();
            user.setRegisterIp(context);
            List<User> userList = userService.findUserList(user);
            LoginLog loginLog = new LoginLog();
            loginLog.setIp(context);
            List<LoginLog> loginLogList = loginLogService.findLoginLogList(loginLog);
            if (loginLogList.size() > CommonConst.NUMBER_0){
                loginLogList = loginLogList.stream().filter(CommonUtil.distinctByKey(LoginLog::getAccount)).collect(Collectors.toList());
            }
            Map<String,Object> map = new HashMap<>();
            map.put("register",userList);
            map.put("login",loginLogList);
            return ResponseUtil.success(map);
        }else if(tag == CommonConst.NUMBER_1){//反查银行卡号
            Bankcards bankcards = new Bankcards();
            bankcards.setBankAccount(context);
            List<Bankcards> userBank = bankcardsService.findUserBank(bankcards);
            return ResponseUtil.success(userBank);
        }
        return ResponseUtil.fail();
    }
}