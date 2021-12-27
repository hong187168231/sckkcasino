package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.model.UserWashCodeConfig;
import com.qianyi.casinocore.model.WashCodeConfig;
import com.qianyi.casinocore.service.UserWashCodeConfigService;
import com.qianyi.casinocore.service.WashCodeConfigService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
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

import java.math.BigDecimal;
import java.util.ArrayList;
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
            List<WashCodeConfig> washCodeConfigs = washCodeConfigList.stream().filter(washCodeConfig -> !CasinoProxyUtil.checkNull(washCodeConfig.getPlatform())).collect(Collectors.toList());

            for (WashCodeConfig washCodeConfig : washCodeConfigs) {
                UserWashCodeConfig userWashCodeConfig = new UserWashCodeConfig();
                userWashCodeConfig.setUserId(userId);
                BeanUtils.copyProperties(washCodeConfig, userWashCodeConfig, UserWashCodeConfig.class);
                byUserIdAndPlatform.add(userWashCodeConfig);
            }
        }

        List<UserWashCodeConfig> washCodeConfigs = byUserIdAndPlatform.stream().filter(userWashCodeConfig -> !CasinoProxyUtil.checkNull(userWashCodeConfig.getPlatform())).collect(Collectors.toList());

        Map<String, List<UserWashCodeConfig>> collect = washCodeConfigs.stream().collect(Collectors.groupingBy(UserWashCodeConfig::getPlatform));

        return ResponseUtil.success(collect);
    }


    @PostMapping("updateWashCodeConfigs")
    @Operation(summary = "编辑用户洗码配置表")
    public ResponseEntity<UserWashCodeConfig> updateUserWash(@RequestBody List<UserWashCodeConfig> userWashCodeConfigs){
        //第一次编辑，保存所有洗码数据

        if(userWashCodeConfigs != null && userWashCodeConfigs.size() > 0){
            for (UserWashCodeConfig u : userWashCodeConfigs){
                if (u.getRate().compareTo(BigDecimal.ZERO) < CommonConst.NUMBER_0){
                    return ResponseUtil.custom("参数不合法");
                }
                if(u.getRate().compareTo(BigDecimal.valueOf(0.9)) > 0){
                    return ResponseUtil.custom("洗码倍率超过限制");
                }
            }
            List<UserWashCodeConfig> codeConfigs = new ArrayList<>();
            Long userId = userWashCodeConfigs.get(0).getUserId();
            List<UserWashCodeConfig> userWashCodeConfigList = userWashCodeConfigService.findByUserId(userId);
            if(userWashCodeConfigList == null || userWashCodeConfigList.size() <= 0){
                List<WashCodeConfig> washCodeConfigList = washCodeConfigService.findAll();
                for (WashCodeConfig washCodeConfig : washCodeConfigList) {
                    UserWashCodeConfig userWashCodeConfig = new UserWashCodeConfig();
                    userWashCodeConfig.setUserId(userId);
                    BeanUtils.copyProperties(washCodeConfig, userWashCodeConfig, UserWashCodeConfig.class);
                    codeConfigs.add(userWashCodeConfig);
                }
            }else{
                userWashCodeConfigService.saveAll(userWashCodeConfigs);
                return ResponseUtil.success();
            }

            List<UserWashCodeConfig> washCodeConfigs = codeConfigs.stream().filter(userWashCodeConfig -> !CasinoProxyUtil.checkNull(userWashCodeConfig.getPlatform())).collect(Collectors.toList());

            washCodeConfigs.stream().forEach(UserWashCodeConfig ->{
                userWashCodeConfigs.stream().forEach(u ->{
                    if(UserWashCodeConfig.getGameId().equals(u.getGameId())){
                        UserWashCodeConfig.setRate(u.getRate());
                        UserWashCodeConfig.setState(u.getState());
                    }
                });
            });
            userWashCodeConfigService.saveAll(washCodeConfigs);
            return ResponseUtil.success();
        }
        return ResponseUtil.custom("保存游戏配置失败");
    }
}
