package com.qianyi.casinoadmin.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoadmin.util.CommonUtil;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.CoreConstants;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.SysConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.service.SysConfigService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/managementRisk")
@Api(tags = "客户中心")
public class RiskManagementController {
    @Autowired
    private PlatformConfigService platformConfigService;

    private final static Map<String,String> mapName = new HashMap<>();
    static {
        mapName.put("注册ip限制","ipMaxNum");
        mapName.put("WM余额警戒线","wmMoneyWarning");
    }
    /**
     * 客户风险配置查询
     * @return
     */
    @ApiOperation("客户风险配置查询")
    @GetMapping("/findRiskConfig")
    public ResponseEntity findSysConfig(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        JSONArray jsonArray = new JSONArray();
        if (LoginUtil.checkNull(platformConfig)){
            return ResponseUtil.success(jsonArray);
        }
        for (Map.Entry<String, String> entry : mapName.entrySet()) {
            try {
                JSONObject jsonObject = new JSONObject();
                Method getMethod = platformConfig.getClass().getMethod("get" + CommonUtil.toUpperCaseFirstOne(entry.getValue()));
                jsonObject.put("key",entry.getValue());
                jsonObject.put("value",getMethod.invoke(platformConfig));
                jsonObject.put("name",entry.getKey());
                jsonArray.add(jsonObject);
            } catch (Exception e) {

            }
        }
        return ResponseUtil.success(jsonArray);
    }

    /**
     * 客户风险配置修改
     * @param ipMaxNum 注册ip限制
     * @param wmMoneyWarning WM余额警戒线
     * @return
     */
    @ApiOperation("客户风险配置修改")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ipMaxNum", value = "注册ip限制", required = false),
            @ApiImplicitParam(name = "wmMoneyWarning", value = "WM余额警戒线", required = false),
    })
    @PostMapping("/saveRiskConfig")
    public ResponseEntity saveSysConfig(Integer ipMaxNum, BigDecimal wmMoneyWarning){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (LoginUtil.checkNull(platformConfig)){
            platformConfig = new PlatformConfig();
        }
        if (!LoginUtil.checkNull(ipMaxNum)){
            platformConfig.setIpMaxNum(ipMaxNum);
        }
        if (!LoginUtil.checkNull(wmMoneyWarning)){
            platformConfig.setWmMoneyWarning(wmMoneyWarning);
        }
        platformConfigService.save(platformConfig);
        return ResponseUtil.success();
    }
    @ApiOperation("客户风险配置名称获取")
    @GetMapping("/findSysConfigName")
    public ResponseEntity findSysConfigName(){
        return ResponseUtil.success(mapName.keySet());
    }

}
