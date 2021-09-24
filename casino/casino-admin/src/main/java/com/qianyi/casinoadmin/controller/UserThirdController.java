package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.CommonConst;
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
@Api(tags = "客户中心")
public class UserThirdController {
    @Autowired
    private UserThirdService userThirdService;
    @Autowired
    private UserService userService;

    @ApiOperation("根据我方用户账号查询三方账号")
    @GetMapping("/findUserThird")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userAccount", value = "用户账号", required = true),
            @ApiImplicitParam(name = "tag", value = "tag 0 用我方账号查第三方账号 ,1 第三方账号查我方账号", required = true),
    })
    public ResponseEntity findUserThird(String userAccount,Integer tag){
        if (tag == null || userAccount == null){
            return ResponseUtil.custom("参数不合法");
        }
        if (tag == CommonConst.NUMBER_0){
            User user = userService.findByAccount(userAccount);
            if (user==null){
                return ResponseUtil.custom("账户不存在");
            }
            UserThird userThird = userThirdService.findByUserId(user.getId());
            if (userThird==null){
                return ResponseUtil.custom("三方账号不存在");
            }
            return ResponseUtil.success(userThird.getAccount());
        }else if(tag == CommonConst.NUMBER_1){
            UserThird userThird = userThirdService.findByAccount(userAccount);
            if (userThird==null){
                return ResponseUtil.custom("三方账号不存在");
            }
            User user = userService.findById(userThird.getUserId());
            if (user==null){
                return ResponseUtil.custom("账户不存在");
            }
            return ResponseUtil.success(user.getAccount());
        }
        return ResponseUtil.fail();
    }
}
