package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.service.UserThirdService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.UserThirdVo;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
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
    public ResponseEntity<UserThirdVo> findUserThird(String userAccount, Integer tag){
        if (CasinoProxyUtil.checkNull(tag,userAccount)){
            return ResponseUtil.custom("参数不合法");
        }
        if (tag != CommonConst.NUMBER_0 && tag != CommonConst.NUMBER_1){
            return ResponseUtil.custom("参数不合法");
        }
        User user;
        UserThird userThird;
        if (tag == CommonConst.NUMBER_0){
            User u = new User();
            u.setAccount(userAccount);
            if (CasinoProxyUtil.setParameter(u)){
                return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
            }
             user = userService.findOne(u);
            if (CasinoProxyUtil.checkNull(user)){
                return ResponseUtil.success();
            }
             userThird = userThirdService.findByUserId(user.getId());
            if (CasinoProxyUtil.checkNull(userThird)){
                return ResponseUtil.success();
            }
        }else{
             userThird = userThirdService.findByAccount(userAccount);
            if (CasinoProxyUtil.checkNull(userThird)){
                return ResponseUtil.success();
            }
            User u = new User();
            u.setId(userThird.getUserId());
            if (CasinoProxyUtil.setParameter(u)){
                return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
            }
             user = userService.findOne(u);
            if (CasinoProxyUtil.checkNull(user)){
                return ResponseUtil.success();
            }
        }
        UserThirdVo userThirdVo = new UserThirdVo();
        userThirdVo.setAccount(user.getAccount());
        userThirdVo.setThirdAccount(userThird.getAccount());
        return ResponseUtil.success(userThirdVo);
    }


}
