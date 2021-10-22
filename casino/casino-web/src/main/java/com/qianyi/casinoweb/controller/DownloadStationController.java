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

import javax.servlet.http.HttpServletRequest;
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
            return ResponseUtil.custom("网站域名未配置,请联系客服");
        }
        String domain = platformConfig.getDomainNameConfiguration();
        return ResponseUtil.success(domain);
    }

    @GetMapping("requestTest")
    @ApiOperation("requestTest")
    @NoAuthentication
    public ResponseEntity requestTest(HttpServletRequest request) {
        System.out.println("Protocol: " + request.getProtocol());

        System.out.println("Scheme: " + request.getScheme());

        System.out.println("Server Name: " + request.getServerName() );

        System.out.println("Server Port: " + request.getServerPort());

        System.out.println("Protocol: " + request.getProtocol());

        System.out.println("Server Info: " + request.getServletContext().getServerInfo());

        System.out.println("Remote Addr: " + request.getRemoteAddr());

        System.out.println("Remote Host: " + request.getRemoteHost());

        System.out.println("Remote Port: " + request.getRemotePort());

        System.out.println("Local Port: " + request.getLocalPort());

        System.out.println("Local Addr: " + request.getLocalAddr());

        System.out.println("Local Name: " + request.getLocalName());

        System.out.println("Character Encoding: " + request.getCharacterEncoding());

        System.out.println("Content Length: " + request.getContentLength());

        System.out.println("Content Type: "+ request.getContentType());

        System.out.println("Auth Type: " + request.getAuthType());

        System.out.println("HTTP Method: " + request.getMethod());

        System.out.println("Path Info: " + request.getPathInfo());

        System.out.println("Path Trans: " + request.getPathTranslated());

        System.out.println("Query String: " + request.getQueryString());

        System.out.println("Remote User: " + request.getRemoteUser());

        System.out.println("Session Id: " + request.getRequestedSessionId());

        System.out.println("Request URI: " + request.getRequestURI());

        System.out.println("Servlet Path: " + request.getServletPath());

        System.out.println("Accept: " + request.getHeader("Accept"));

        System.out.println("Host: " + request.getHeader("Host"));

        System.out.println("Referer : " + request.getHeader("Referer"));

        System.out.println("Accept-Language : " + request.getHeader("Accept-Language"));

        System.out.println("Accept-Encoding : " + request.getHeader("Accept-Encoding"));

        System.out.println("User-Agent : " + request.getHeader("User-Agent"));

        System.out.println("Connection : " + request.getHeader("Connection"));

        System.out.println("Cookie : " + request.getHeader("Cookie"));

        System.out.println("Created : " + request.getSession().getCreationTime());

        System.out.println("LastAccessed : " + request.getSession().getLastAccessedTime());
        return ResponseUtil.success();
    }
}
