package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.UserWashCodeConfig;
import com.qianyi.casinocore.model.WashCodeConfig;
import com.qianyi.casinocore.service.UserWashCodeConfigService;
import com.qianyi.casinocore.service.WashCodeConfigService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("UserWashCodeConfig")
@Api(tags = "客户中心")
public class UserWashCodeConfigController {

    @Autowired
    private UserWashCodeConfigService userWashCodeConfigService;

    @Autowired
    private WashCodeConfigService washCodeConfigService;

    @GetMapping("findAll")
    @ApiOperation("用户洗码配置表查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", required = true),
    })
    public ResponseEntity<UserWashCodeConfig> findAll(Long userId) {
        List<UserWashCodeConfig> byUserIdAndPlatform = userWashCodeConfigService.findByUserId(userId);
        if(byUserIdAndPlatform == null || byUserIdAndPlatform.size() == 0){
            List<WashCodeConfig> washCodeConfigList = washCodeConfigService.findAll();
            for (WashCodeConfig washCodeConfig : washCodeConfigList) {
                UserWashCodeConfig userWashCodeConfig = new UserWashCodeConfig();
                userWashCodeConfig.setUserId(userId);
                BeanUtils.copyProperties(washCodeConfig, userWashCodeConfig, UserWashCodeConfig.class);
                byUserIdAndPlatform.add(userWashCodeConfig);
            }
        }
        Map<String, List<UserWashCodeConfig>> collect = byUserIdAndPlatform.stream().collect(Collectors.groupingBy(UserWashCodeConfig::getPlatform));

        return ResponseUtil.success(collect);
    }


    @PostMapping("updateWashCodeConfigs")
    @Operation(summary = "编辑用户洗码配置表")
    public ResponseEntity<UserWashCodeConfig> updateUserWash(@RequestBody List<UserWashCodeConfig> userWashCodeConfigs){
        if(userWashCodeConfigs != null && userWashCodeConfigs.size() > 0){
            List<UserWashCodeConfig> userWash= userWashCodeConfigService.saveAll(userWashCodeConfigs);
            Map<String, List<UserWashCodeConfig>> collect = userWash.stream().collect(Collectors.groupingBy(UserWashCodeConfig::getPlatform));

            return ResponseUtil.success(collect);
        }
        return ResponseUtil.custom("保存游戏配置失败");
    }
}
