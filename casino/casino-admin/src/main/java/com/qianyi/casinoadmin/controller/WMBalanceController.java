package com.qianyi.casinoadmin.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoadmin.util.CommonConst;
import com.qianyi.casinocore.CoreConstants;
import com.qianyi.casinocore.model.SysConfig;
import com.qianyi.casinocore.service.SysConfigService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/wmBalance")
@Api(tags = "资金中心")
public class WMBalanceController {
    @Autowired
    private SysConfigService sysConfigService;
    /**
     * web定时查询WM总余额
     *
     * @return
     */
    @ApiOperation("web定时查询WM总余额")
    @GetMapping("/findWMBalance")
    public ResponseEntity findWMBalance(){
        SysConfig balance = sysConfigService.findBySysGroupAndName(CoreConstants.SysConfigGroup.GROUP_FINANCE, CoreConstants.SysConfigName.WM_TOTAL_BALANCE);
        SysConfig risk = sysConfigService.findBySysGroupAndName(CoreConstants.SysConfigGroup.GROUP_FINANCE, CoreConstants.SysConfigName.WM_TOTALBALANCE_RISK);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("balance", balance == null? CommonConst.NUMBER_0:balance.getValue());
        jsonObject.put("risk", risk == null? CommonConst.NUMBER_0:risk.getValue());
        return ResponseUtil.success(jsonObject);
    }
}
