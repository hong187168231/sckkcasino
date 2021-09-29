package com.qianyi.casinoadmin.controller;


import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.CoreConstants;
import com.qianyi.casinocore.model.SysConfig;
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

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/managementRisk")
@Api(tags = "客户中心")
public class RiskManagementController {
    @Autowired
    private SysConfigService sysConfigService;
    /**
     * 客户风险配置查询
     * @return
     */
    @ApiOperation("客户风险配置查询")
    @GetMapping("/findRiskConfig")
    public ResponseEntity findSysConfig(){
        List<SysConfig> all = sysConfigService.findAll();
        return ResponseUtil.success(all);
    }

    /**
     * 客户风险配置修改
     * @param id id
     * @param remark 备注
     * @param timeLimit 限制次数
     * @return
     */
    @ApiOperation("客户风险配置修改")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id(无id是新增)", required = false),
            @ApiImplicitParam(name = "timeLimit", value = "限制次数", required = true),
            @ApiImplicitParam(name = "remark", value = "备注", required = false),
            @ApiImplicitParam(name = "name", value = "配置名", required = true),
    })
    @PostMapping("/saveRiskConfig")
    public ResponseEntity saveSysConfig(Long id,String remark,String timeLimit,String name){
        if (LoginUtil.checkNull(timeLimit,name)){
            return ResponseUtil.custom("参数错误");
        }
        if (LoginUtil.checkNull(id)){
            SysConfig byName = sysConfigService.findByName(name);
            if (!LoginUtil.checkNull(byName)){
                return ResponseUtil.custom("配置名称重复");
            }
        }
        CoreConstants.SysConfigEnum sysConfigEnum;
        try {
            sysConfigEnum = CoreConstants.SysConfigEnum.valueOf(name);
        }catch (Exception ex){
            return ResponseUtil.custom("无效配置名称");
        }
        SysConfig sysConfig = new SysConfig();
        sysConfig.setId(id);
        sysConfig.setValue(timeLimit);
        sysConfig.setRemark(remark);
        sysConfig.setName(sysConfigEnum.getCode());
        sysConfig.setSysGroup(sysConfigEnum.getGroup());
        SysConfig save = sysConfigService.save(sysConfig);
        return ResponseUtil.success(save);
    }
    @ApiOperation("客户风险配置名称获取")
    @GetMapping("/findSysConfigName")
    public ResponseEntity findSysConfigName(){
        return ResponseUtil.success(CoreConstants.SysConfigEnum.values());
    }

}
