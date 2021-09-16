package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.model.BetRatioConfig;
import com.qianyi.casinocore.model.Notice;
import com.qianyi.casinocore.service.BetRatioConfigService;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/betRatioConfig")
@Api(tags = "资金中心")
public class BetRatioConfigController {

    @Autowired
    private BetRatioConfigService betRatioConfigService;

    @ApiOperation("查询所有")
    @GetMapping("/findAll")
    public ResponseEntity<BetRatioConfig> findAll(){
        List<BetRatioConfig> betRatioConfigList = betRatioConfigService.findAll();
        return new ResponseEntity(ResponseCode.SUCCESS, betRatioConfigList);
    }

    @ApiOperation("编辑打码倍率")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id主键", required = false),
            @ApiImplicitParam(name = "name", value = "名称", required = true),
            @ApiImplicitParam(name = "codeTimes", value = "打码倍率", required = true),
            @ApiImplicitParam(name = "minMoney", value = "最低金额重置打码量", required = true)
    })
    @GetMapping("/update")
    public ResponseEntity<BetRatioConfig> update(Long id, String name, Float codeTimes, BigDecimal minMoney){
        BetRatioConfig betRatioConfig = null;
        if(id != null){
            betRatioConfig = betRatioConfigService.findById(id);
        }else{
            List<BetRatioConfig> betRatioConfigList =  betRatioConfigService.findAll();
            if(betRatioConfigList != null){
                if(betRatioConfigList.size() >= 1){
                    return ResponseUtil.custom("数据错误");
                }else{
                    betRatioConfig = betRatioConfigList.get(0);
                }
            }else{
                betRatioConfig = new BetRatioConfig();
            }

            betRatioConfig.setName(name);
            betRatioConfig.setCodeTimes(codeTimes);
            betRatioConfig.setMinMoney(minMoney);
            betRatioConfigService.save(betRatioConfig);
        }

        return new ResponseEntity(ResponseCode.SUCCESS, betRatioConfig);
    }
}
