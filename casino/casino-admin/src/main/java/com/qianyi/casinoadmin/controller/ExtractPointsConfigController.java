package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.business.ExtractPointsConfigBusiness;
import com.qianyi.casinocore.co.extractpoints.ExtractPointsConfigCo;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.ExtractPointsConfigService;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/extractPointsConfig")
@Api(tags = "代理抽点配置")
public class ExtractPointsConfigController {

    @Autowired
    private ExtractPointsConfigService service;

    @Autowired
    private ExtractPointsConfigBusiness business;

    @GetMapping("/findAll")
    @ApiOperation("代理抽点默认配置查询")
    public ResponseEntity<List<ExtractPointsConfig>> findAll(@ModelAttribute ExtractPointsConfigCo co) {
        List<ExtractPointsConfig> list = service.findList(co);
        return ResponseUtil.success(list);
    }


    @GetMapping("/poxy/findAll")
    @ApiOperation("代理抽点配置表查询")
    public ResponseEntity<List<PoxyExtractPointsConfig>> poxyFindAll(
            @ApiParam("基础代代理id") @RequestParam Long poxyId
    ) {
        return ResponseUtil.success(business.findPoxyExtractPointsConfig(poxyId));
    }


    @GetMapping("/user/findAll")
    @ApiOperation("用户抽点配置表查询")
    public ResponseEntity<List<UserExtractPointsConfig>> userFindAll(
            @ApiParam("用户id") @RequestParam Long userId
    ) {
        return ResponseUtil.success(business.findUserExtractPointsConfig(userId));
    }

    @PostMapping("/update")
    @ApiOperation("更新默认的抽点配置")
    public ResponseEntity<Map<String, List<ExtractPointsConfig>>> updateExtractPointsConfigs(
            @RequestBody List<ExtractPointsConfig> configList
    ) {
        return ResponseUtil.success(business.updateExtractPointsConfigs(configList));
    }


    @PostMapping("/poxy/update")
    @ApiOperation("更新基础代代理抽点配置")
    public ResponseEntity<List<PoxyExtractPointsConfig>> updatePoxyExtractPointsConfigs(
            @RequestBody List<PoxyExtractPointsConfig> configList
    ) {
        return ResponseUtil.success(business.updatePoxyExtractPointsConfigs(configList));
    }


    @PostMapping("/user/update")
    @ApiOperation("更新用户抽点配置")
    public ResponseEntity<List<UserExtractPointsConfig>> updateUserExtractPointsConfigs(
            @RequestBody List<UserExtractPointsConfig> configList
    ) {
        return ResponseUtil.success(business.updateUserExtractPointsConfigs(configList));
    }

    /*@Autowired
    private ExtractPointsConfigBusiness extractPointsConfigBusiness;

    @Autowired
    GameRecordAsyncOper gameRecordAsyncOper;

    @Autowired
    UserService userService;

    @Autowired
    UserThirdService userThirdService;

    @NoAuthorization
    @GetMapping("/test")
    public ResponseEntity<String> test(){
        String result = "[{\"user\":\"9032e2ccd0164a6cb5adcef53d9385\",\"betId\":\"179122069\",\"betTime\":\"2022-02-28 12:57:47\",\"beforeCash\":\"4064.0000\",\"bet\":\"50.0000\",\"validbet\":\"50.0000\",\"water\":\"0.0000\",\"result\":\"50.0000\",\"betCode\":\"Player\",\"waterbet\":\"50.0000\",\"winLoss\":\"50.0000\",\"ip\":\"116.212.142.251\",\"gid\":\"101\",\"event\":\"112929592\",\"eventChild\":\"31\",\"round\":\"112929592\",\"subround\":\"31\",\"tableId\":\"206\",\"commission\":\"0\",\"settime\":\"2022-02-28 13:00:30\",\"reset\":\"N\",\"betResult\":\"闲\",\"gameResult\":\"庄:♠3♦K♣3 闲:♦3♥4\",\"gname\":\"百家乐\"}]";
        List<GameRecord> gameRecords = JSON.parseArray(result, GameRecord.class);
        for (GameRecord record : gameRecords) {
            record.setId(203166L);
            UserThird account = userThirdService.findByAccount(record.getUser());
            record.setUserId(account.getUserId());
            User user = userService.findById(record.getUserId());
            if (user != null) {
                record.setFirstProxy(user.getFirstProxy());
                record.setSecondProxy(user.getSecondProxy());
                record.setThirdProxy(user.getThirdProxy());
            }
            // 抽点
            gameRecordAsyncOper.extractPoints(Constants.PLATFORM_WM, record);
        }
        return ResponseUtil.success();
    }*/

}
