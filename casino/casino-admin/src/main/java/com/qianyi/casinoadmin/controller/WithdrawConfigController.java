package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.CommonConst;
import com.qianyi.casinocore.model.AmountConfig;
import com.qianyi.casinocore.service.AmountConfigService;
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

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/withdrawConfig")
@Api(tags = "资金中心")
public class WithdrawConfigController {
    @Autowired
    private AmountConfigService amountConfigService;

    /**
     * 提款设置列表
     * @return
     */
    @ApiOperation("提款设置列表")
    @GetMapping("/findWithdrawConfig")
    public ResponseEntity findWithdrawConfig(){
        AmountConfig amountConfigById = amountConfigService.findAmountConfigById(CommonConst.withdraw);
        return ResponseUtil.success(amountConfigById);
    }

    /**
     * 提现设置修改
     * @param status 状态 0免手续费 1 有手续费
     * @param fixedAmount 固定金额
     * @param percentage 百分比金额
     * @param maxMoney 最大金额
     * @param minMoney 最小金额
     * @return
     */
    @ApiOperation("提现设置修改")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "状态 0免手续费 1 有手续费", required = false),
            @ApiImplicitParam(name = "fixedAmount", value = "固定金额", required = false),
            @ApiImplicitParam(name = "percentage", value = "百分比金额", required = false),
            @ApiImplicitParam(name = "maxMoney", value = "最大金额", required = false),
            @ApiImplicitParam(name = "minMoney", value = "最小金额", required = false),
    })
    @PostMapping("/saveWithdrawConfig")
    public ResponseEntity saveWithdrawConfig(Integer status, BigDecimal fixedAmount, Float percentage, BigDecimal maxMoney, BigDecimal minMoney){
        if (fixedAmount != null && percentage != null){
            return ResponseUtil.custom("参数错误");
        }
        if (percentage != null && (percentage > CommonConst.FLOAT_1 || percentage < CommonConst.FLOAT_0)){
            return ResponseUtil.custom("百分比金额设置错误");
        }
        AmountConfig amountConfig = new AmountConfig(status,fixedAmount,percentage,maxMoney,minMoney);
        amountConfig.setId(CommonConst.withdraw);
        amountConfigService.save(amountConfig);
        return ResponseUtil.success();
    }
}
