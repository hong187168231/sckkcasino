package com.qianyi.casinoadmin.controller;


import com.qianyi.casinoadmin.util.LoginUtil;
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
            @ApiImplicitParam(name = "id", value = "id(目前只限制注册 id传1)", required = true),
            @ApiImplicitParam(name = "remark", value = "备注", required = false),
            @ApiImplicitParam(name = "timeLimit", value = "限制次数", required = true),
            @ApiImplicitParam(name = "timeLimit", value = "限制次数", required = true),
            @ApiImplicitParam(name = "sysGroup", value = "1 财务 2 ip", required = true),
    })
    @PostMapping("/saveRiskConfig")
    public ResponseEntity saveSysConfig(Long id,String remark,String timeLimit,String name,Integer sysGroup){
        if (LoginUtil.checkNull(timeLimit,sysGroup,id)){
            return ResponseUtil.custom("参数错误");
        }
        SysConfig sysConfig = new SysConfig();
        sysConfig.setId(id);
        sysConfig.setValue(timeLimit);
        sysConfig.setRemark(remark);
        sysConfig.setName(name);
        sysConfig.setSysGroup(sysGroup);
        SysConfig save = sysConfigService.save(sysConfig);
        return ResponseUtil.success(save);
    }

}
