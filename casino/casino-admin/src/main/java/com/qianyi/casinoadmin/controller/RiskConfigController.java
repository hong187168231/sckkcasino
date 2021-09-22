package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.model.RiskConfig;
import com.qianyi.casinocore.service.RiskConfigService;
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
@RequestMapping("/riskConfig")
@Api(tags = "客户中心")
public class RiskConfigController {
    @Autowired
    private RiskConfigService riskConfigService;
    /**
     * 客户风险配置查询
     * @return
     */
    @ApiOperation("客户风险配置查询")
    @GetMapping("/findRiskConfig")
    public ResponseEntity findRiskConfig(){
        List<RiskConfig> all = riskConfigService.findAll();
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
    })
    @PostMapping("/saveRiskConfig")
    public ResponseEntity saveRiskConfig(Long id,String remark,Integer timeLimit){
        if (id == null || timeLimit == null){
            return ResponseUtil.custom("参数错误");
        }
        RiskConfig riskConfig = new RiskConfig();
        riskConfig.setId(id);
        riskConfig.setTimeLimit(timeLimit);
        riskConfig.setRemark(remark);
        RiskConfig save = riskConfigService.save(riskConfig);
        return ResponseUtil.success(save);
    }

}
