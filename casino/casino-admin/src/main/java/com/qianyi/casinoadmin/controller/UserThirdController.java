package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.vo.UserThirdVo;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.service.UserMoneyService;
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
    public ResponseEntity<UserThirdVo> findUserThird(String userAccount,Integer tag){
        if (LoginUtil.checkNull(tag,userAccount)){
            return ResponseUtil.custom("参数不合法");
        }
        if (tag != CommonConst.NUMBER_0 && tag != CommonConst.NUMBER_1){
            return ResponseUtil.custom("参数不合法");
        }
        User user;
        UserThird userThird;
        if (tag == CommonConst.NUMBER_0){
             user = userService.findByAccount(userAccount);
            if (LoginUtil.checkNull(user)){
                return ResponseUtil.success();
            }
             userThird = userThirdService.findByUserId(user.getId());
            if (LoginUtil.checkNull(userThird)){
                return ResponseUtil.success();
            }
        }else{
             userThird = userThirdService.findByAccount(userAccount);
            if (LoginUtil.checkNull(userThird)){
                return ResponseUtil.success();
            }
             user = userService.findById(userThird.getUserId());
            if (LoginUtil.checkNull(user)){
                return ResponseUtil.success();
            }
        }
        UserThirdVo userThirdVo = new UserThirdVo();
        userThirdVo.setAccount(user.getAccount());
        userThirdVo.setThirdAccount(userThird.getAccount());
//        UserMoney userMoney = userMoneyService.findByUserId(user.getId());
//        if (LoginUtil.checkNull(userMoney)){
//            return ResponseUtil.custom("找不到该会员钱包");
//        }
//        userThirdVo.setMoney(userMoney.getMoney());
//        userThirdVo.setCodeNum(userMoney.getCodeNum());
//        userThirdVo.setWmMoney(userMoneyService.getWMonetUser(user,userThird));
        return ResponseUtil.success(userThirdVo);
    }


}
