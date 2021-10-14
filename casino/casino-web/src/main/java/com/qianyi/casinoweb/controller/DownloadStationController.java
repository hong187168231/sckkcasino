package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.DownloadStation;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.DownloadStationService;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinoweb.vo.DownloadStationVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
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
    @Autowired
    private PlatformConfigService platformConfigService;

    @GetMapping("getIosNewestVersion")
    @ApiOperation("ios最新版本检查")
    @NoAuthentication
    public ResponseEntity<DownloadStationVo> getIosNewestVersion() {
        DownloadStation newest = downloadStationService.getNewestVersion(2);
        DownloadStation forcedNewest = downloadStationService.getForcedNewestVersion(2, 1);
        DownloadStationVo vo=new DownloadStationVo();
        vo.setNewest(newest);
        vo.setForcedNewest(forcedNewest);
        return ResponseUtil.success(vo);
    }

    @GetMapping("getAndroidNewestVersion")
    @ApiOperation("Android最新版本检查")
    @NoAuthentication
    public ResponseEntity<DownloadStationVo> getAndroidNewestVersion() {
        DownloadStation newest = downloadStationService.getNewestVersion(1);
        DownloadStation forcedNewest = downloadStationService.getForcedNewestVersion(1, 1);
        DownloadStationVo vo=new DownloadStationVo();
        vo.setNewest(newest);
        vo.setForcedNewest(forcedNewest);
        return ResponseUtil.success(vo);
    }

    @GetMapping("getWebUrl")
    @ApiOperation("网页版链接")
    @NoAuthentication
    public ResponseEntity getWebUrl() {
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (platformConfig == null || ObjectUtils.isEmpty(platformConfig.getDomainNameConfiguration())) {
            return ResponseUtil.custom("网站域名未配置");
        }
        String domain = platformConfig.getDomainNameConfiguration();
        return ResponseUtil.success(domain);
    }
}
