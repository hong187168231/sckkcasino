package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.vo.UserVo;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user")
@Api(tags = "用户中心")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("info")
    @ApiOperation("获取当前用户的基本信息")
    public ResponseEntity info() {
        Long authId = CasinoWebUtil.getAuthId();
        User user = userService.findById(authId);
        UserVo vo = new UserVo();
        vo.setUserId(user.getId());
        vo.setAccount(user.getAccount());
        vo.setName(user.getName());
        vo.setHeadImg(user.getHeadImg());

        //TODO 设置可提金额，未完成流水

        return ResponseUtil.success(vo);
    }

}
