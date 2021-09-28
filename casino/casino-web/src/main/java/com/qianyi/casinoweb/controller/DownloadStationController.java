package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.DownloadStation;
import com.qianyi.casinocore.service.DownloadStationService;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("downloadStation")
@Api(tags = "应用版本控制")
public class DownloadStationController {

    @Autowired
    private DownloadStationService downloadStationService;

    @GetMapping("getIosNewestVersion")
    @ApiOperation("ios最新版本检查")
    @NoAuthentication
    public ResponseEntity getIosNewestVersion() {
        DownloadStation newest = downloadStationService.getNewestVersion(2);
        DownloadStation forcedNewest = downloadStationService.getForcedNewestVersion(2, 1);
        Map<String, DownloadStation> map = new HashMap<>();
        map.put("newest", newest);
        map.put("forcedNewest", forcedNewest);
        return ResponseUtil.success(map);
    }

    @GetMapping("getAndroidNewestVersion")
    @ApiOperation("Android最新版本检查")
    @NoAuthentication
    public ResponseEntity getAndroidNewestVersion() {
        DownloadStation newest = downloadStationService.getNewestVersion(1);
        DownloadStation forcedNewest = downloadStationService.getForcedNewestVersion(1, 1);
        Map<String, DownloadStation> map = new HashMap<>();
        map.put("newest", newest);
        map.put("forcedNewest", forcedNewest);
        return ResponseUtil.success(map);
    }
}
