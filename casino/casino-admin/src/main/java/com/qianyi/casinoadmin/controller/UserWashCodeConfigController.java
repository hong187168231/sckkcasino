package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.UserWashCodeConfig;
import com.qianyi.casinocore.model.WashCodeConfig;
import com.qianyi.casinocore.service.UserWashCodeConfigService;
import com.qianyi.casinocore.service.WashCodeConfigService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.RedisLockUtil;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("UserWashCodeConfig")
@Slf4j
@Api(tags = "客户中心")
public class UserWashCodeConfigController {

    @Autowired
    private UserWashCodeConfigService userWashCodeConfigService;

    @Autowired
    private WashCodeConfigService washCodeConfigService;

    @Autowired
    private RedisLockUtil redisLockUtil;

    @GetMapping("findAll")
    @ApiOperation("用户洗码配置表查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "userId", value = "用户id", required = true),})
    public ResponseEntity<UserWashCodeConfig> findAll(Long userId) {
        List<UserWashCodeConfig> byUserIdAndPlatform = userWashCodeConfigService.findByUserId(userId);
        if (byUserIdAndPlatform == null || byUserIdAndPlatform.size() == 0) {
            List<WashCodeConfig> washCodeConfigList = washCodeConfigService.findAll();
            List<WashCodeConfig> washCodeConfigs =
                washCodeConfigList.stream().filter(washCodeConfig -> !LoginUtil.checkNull(washCodeConfig.getPlatform()))
                    .collect(Collectors.toList());

            for (WashCodeConfig washCodeConfig : washCodeConfigs) {
                UserWashCodeConfig userWashCodeConfig = new UserWashCodeConfig();
                userWashCodeConfig.setUserId(userId);
                BeanUtils.copyProperties(washCodeConfig, userWashCodeConfig, UserWashCodeConfig.class);
                byUserIdAndPlatform.add(userWashCodeConfig);
            }
        } else {
            List<WashCodeConfig> washCodeConfigList = washCodeConfigService.findAll();
            if (byUserIdAndPlatform.size() != washCodeConfigList.size()) {
                Map<String, UserWashCodeConfig> userWashCodeConfigMap = byUserIdAndPlatform.stream()
                    .collect(Collectors.toMap(UserWashCodeConfig::getGameId, serWashCodeConfig -> serWashCodeConfig));
                Map<String, WashCodeConfig> washCodeConfigMap = washCodeConfigList.stream()
                    .collect(Collectors.toMap(WashCodeConfig::getGameId, washCodeConfig -> washCodeConfig));
                List<UserWashCodeConfig> userWashCodeConfigs = new ArrayList<>();
                for (String key : washCodeConfigMap.keySet()) {
                    if (!userWashCodeConfigMap.containsKey(key)) {
                        UserWashCodeConfig userWashCodeConfig = new UserWashCodeConfig();
                        userWashCodeConfig.setUserId(userId);
                        BeanUtils.copyProperties(washCodeConfigMap.get(key), userWashCodeConfig,
                            UserWashCodeConfig.class);
                        userWashCodeConfig.setId(null);
                        userWashCodeConfigs.add(userWashCodeConfig);
                    }
                }
                if (!userWashCodeConfigs.isEmpty()) {
                    userWashCodeConfigService.saveAll(userWashCodeConfigs);
                    byUserIdAndPlatform = userWashCodeConfigService.findByUserId(userId);
                }
            }
        }

        List<UserWashCodeConfig> washCodeConfigs = byUserIdAndPlatform.stream()
            .filter(userWashCodeConfig -> !LoginUtil.checkNull(userWashCodeConfig.getPlatform()))
            .collect(Collectors.toList());

        return ResponseUtil.success(washCodeConfigs);
    }

    @PostMapping("updateWashCodeConfigs")
    @Operation(summary = "编辑用户洗码配置表")
    public ResponseEntity<UserWashCodeConfig>
        updateUserWash(@RequestBody List<UserWashCodeConfig> userWashCodeConfigs) {
        // 第一次编辑，保存所有洗码数据
        if (userWashCodeConfigs != null && userWashCodeConfigs.size() > 0) {
            for (UserWashCodeConfig userWashCodeConfig : userWashCodeConfigs) {
                if (userWashCodeConfig.getRate().compareTo(BigDecimal.ZERO) < CommonConst.NUMBER_0) {
                    return ResponseUtil.custom("参数不合法");
                }
                if (userWashCodeConfig.getRate().compareTo(BigDecimal.valueOf(0.9)) > 0) {
                    return ResponseUtil.custom("洗码倍率超过限制");
                }
            }
            Long userId = userWashCodeConfigs.get(0).getUserId();
            if (LoginUtil.checkNull(userId)) {
                return ResponseUtil.custom("参数不合法");
            }
            List<UserWashCodeConfig> codeConfigs = new ArrayList<>();
            String key = MessageFormat.format(RedisLockUtil.USER_WASH_CODE_CONFIG, userId.toString());
            Boolean lock = false;
            try {
                lock = redisLockUtil.getLock(key, userId.toString());
                if (lock) {
                    log.info("用户洗码修改取到redis锁{}", key);
                    List<UserWashCodeConfig> userWashCodeConfigList = userWashCodeConfigService.findByUserId(userId);
                    if (userWashCodeConfigList == null || userWashCodeConfigList.size() <= 0) {
                        List<WashCodeConfig> washCodeConfigList = washCodeConfigService.findAll();
                        for (WashCodeConfig washCodeConfig : washCodeConfigList) {
                            UserWashCodeConfig userWashCodeConfig = new UserWashCodeConfig();
                            userWashCodeConfig.setUserId(userId);
                            BeanUtils.copyProperties(washCodeConfig, userWashCodeConfig, UserWashCodeConfig.class);
                            userWashCodeConfig.setId(null);
                            codeConfigs.add(userWashCodeConfig);
                        }
                    } else {
                        userWashCodeConfigService.saveAll(userWashCodeConfigs);
                        return ResponseUtil.success(userWashCodeConfigs);
                    }
                    List<UserWashCodeConfig> washCodeConfigs = codeConfigs.stream()
                        .filter(userWashCodeConfig -> !LoginUtil.checkNull(userWashCodeConfig.getPlatform()))
                        .collect(Collectors.toList());
                    washCodeConfigs.stream().forEach(UserWashCodeConfig -> {
                        userWashCodeConfigs.stream().forEach(u -> {
                            if (UserWashCodeConfig.getGameId().equals(u.getGameId())) {
                                UserWashCodeConfig.setRate(u.getRate());
                                UserWashCodeConfig.setState(u.getState());
                            }
                        });
                    });
                    userWashCodeConfigService.saveAll(washCodeConfigs);
                    List<UserWashCodeConfig> byUserIdAndPlatform = userWashCodeConfigService.findByUserId(userId);
                    return ResponseUtil.success(byUserIdAndPlatform);
                } else {
                    return ResponseUtil.custom("请重试一次");
                }
            } catch (Exception ex) {
                log.error("用户洗码修改请求异常{}" + ex.getMessage());
                return ResponseUtil.custom("请重试一次");
            } finally {
                if (lock) {
                    log.info("释放用户洗码修改redis锁{}", key);
                    redisLockUtil.releaseLock(key, userId.toString());
                }
            }
        }
        return ResponseUtil.custom("保存游戏配置失败");
    }
}
