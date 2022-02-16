package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.vo.UserThirdVo;
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

import java.util.ArrayList;
import java.util.List;

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
            @ApiImplicitParam(name = "platform", value = "游戏类别编号 WM、PG/CQ9", required = false),
    })
    public ResponseEntity<UserThirdVo> findUserThird(String userAccount,Integer tag,String platform){
        if (LoginUtil.checkNull(tag,userAccount)){
            return ResponseUtil.custom("参数不合法");
        }
        if (tag != CommonConst.NUMBER_0 && tag != CommonConst.NUMBER_1){
            return ResponseUtil.custom("参数不合法");
        }
        User user;
        UserThird userThird;
        List<UserThirdVo> list = new ArrayList<>();
        if (tag == CommonConst.NUMBER_0){
             user = userService.findByAccount(userAccount);
            if (LoginUtil.checkNull(user)){
                return ResponseUtil.success();
            }
             userThird = userThirdService.findByUserId(user.getId());
            if (LoginUtil.checkNull(userThird)){
                return ResponseUtil.success();
            }
            if (LoginUtil.checkNull(platform)){
                UserThirdVo WM = new UserThirdVo();
                WM.setAccount(user.getAccount());
                WM.setThirdAccount(userThird.getAccount());
                WM.setPlatform("WM");
                UserThirdVo PG = new UserThirdVo();
                PG.setAccount(user.getAccount());
                PG.setThirdAccount(userThird.getGoldenfAccount());
                PG.setPlatform("PG/CQ9");
                list.add(WM);
                list.add(PG);
            }else if (platform.equals("WM")){
                UserThirdVo WM = new UserThirdVo();
                WM.setAccount(user.getAccount());
                WM.setThirdAccount(userThird.getAccount());
                WM.setPlatform("WM");
                list.add(WM);
            }else {
                UserThirdVo PG = new UserThirdVo();
                PG.setAccount(user.getAccount());
                PG.setThirdAccount(userThird.getGoldenfAccount());
                PG.setPlatform("PG/CQ9");
                list.add(PG);
            }
        }else{
            userThird = userThirdService.findByAccount(userAccount);
            UserThirdVo userThirdVo = new UserThirdVo();
            if (LoginUtil.checkNull(userThird)){
                userThird =  userThirdService.findByGoldenfAccount(userAccount);
                if (LoginUtil.checkNull(userThird)){
                    return ResponseUtil.success();
                }
                user = userService.findById(userThird.getUserId());
                if (LoginUtil.checkNull(user)){
                    return ResponseUtil.success();
                }
                userThirdVo.setAccount(user.getAccount());
                userThirdVo.setThirdAccount(userThird.getGoldenfAccount());
                userThirdVo.setPlatform("PG/CQ9");
                list.add(userThirdVo);
                return ResponseUtil.success(list);
            }
            user = userService.findById(userThird.getUserId());
            if (LoginUtil.checkNull(user)){
                return ResponseUtil.success();
            }
            userThirdVo.setAccount(user.getAccount());
            userThirdVo.setThirdAccount(userThird.getAccount());
            userThirdVo.setPlatform("WM");
            list.add(userThirdVo);
        }
        return ResponseUtil.success(list);
    }


}
