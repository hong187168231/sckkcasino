package com.qianyi.casinoadmin.controller;

import java.util.List;

import com.qianyi.casinocore.business.UserWashCodeBusiness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.service.UserThirdService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Api(tags = "测试")
@RestController
@Slf4j
@RequestMapping("test")
public class TestController {
    @Autowired
    private UserThirdService userThirdService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMoneyService userMoneyService;

    @Autowired
    private UserWashCodeBusiness userWashCodeBusiness;

    @ApiOperation("回收WM余额")
    @GetMapping("/recoveryBalance")
    @NoAuthorization
    public ResponseEntity recoveryBalance() {
        log.info("初始化回收WM余额开始==========================================>");
        long startTime = System.currentTimeMillis();
        List<UserThird> allAcount = userThirdService.findAllAcount();
        if (LoginUtil.checkNull(allAcount) || allAcount.size() == CommonConst.NUMBER_0) {
            return ResponseUtil.success();
        }
        for (UserThird userThird : allAcount) {
            try {
                User user = userService.findById(userThird.getUserId());
                if (LoginUtil.checkNull(user)) {
                    continue;
                }
                JSONObject jsonObject = userMoneyService.oneKeyRecover(user);
                if (LoginUtil.checkNull(jsonObject)
                    || LoginUtil.checkNull(jsonObject.get("code"), jsonObject.get("msg"))) {
                    continue;
                }
                Integer code = (Integer)jsonObject.get("code");
                if (code == CommonConst.NUMBER_0) {
                    log.info("初始化回收WM余额成功{}", user.getAccount());
                }
            } catch (Exception ex) {
                log.error("回收失败userId:{}==={}", userThird.getUserId(), ex.getMessage());
            }
        }
        log.info("回收WM余额结束耗时{}==============================================>", System.currentTimeMillis() - startTime);
        return ResponseUtil.success();
    }

    @ApiOperation("测试洗码")
    @GetMapping("/getWashCodeConfig")
    @NoAuthorization
    public ResponseEntity getWashCodeConfig() {
        return ResponseUtil.success(userWashCodeBusiness.getWashCodeConfig("OBTY",202L));
    }
}
