package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.model.BankInfo;
import com.qianyi.casinocore.service.AdGamesService;
import com.qianyi.casinocore.service.PlatformGameService;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 游戏管理
 */
@Slf4j
@RestController
@RequestMapping("/gamePlatform")
@Api(tags = "游戏中心")
public class PlatformGameController {

    @Autowired
    private AdGamesService adGameService;

    @Autowired
    private PlatformGameService platformGameService;

    @GetMapping("/platformList")
    @ApiOperation("银行列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "platformId", value = "游戏id", required = false),
    })
    @NoAuthorization
    public ResponseEntity<BankInfo> bankList(Integer disable) {
        return null;
    }
}
