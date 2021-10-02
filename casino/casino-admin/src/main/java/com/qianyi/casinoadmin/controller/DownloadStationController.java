package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.DownloadStation;
import com.qianyi.casinocore.service.DownloadStationService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/download")
@Api(tags = "系统管理")
public class DownloadStationController {

    @Autowired
    private DownloadStationService downloadStationService;

    /**
     *
     * @param pageSize
     * @param pageCode
     * @return
     */
    @ApiOperation("下载站查询")
    @GetMapping("/findDownloadStationPage")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
    })
    public ResponseEntity<DownloadStation> findDownloadStationPage(Integer pageSize, Integer pageCode){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        Page<DownloadStation> downloadStationPage = downloadStationService.findPage(pageable);
        return ResponseUtil.success(downloadStationPage);
    }

    @ApiOperation("编辑下载站")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = false),
            @ApiImplicitParam(name = "name", value = "版本名称", required = true),
            @ApiImplicitParam(name = "downloadUrl", value = "下载地址", required = true),
            @ApiImplicitParam(name = "versionNumber", value = "版本号", required = true),
            @ApiImplicitParam(name = "terminalType", value = "终端类型,1：安卓，2：ios", required = true),
            @ApiImplicitParam(name = "remark", value = "备注", required = false),
            @ApiImplicitParam(name = "isForced", value = "强制更新,1：是，2：否", required = true),
    })
    @PostMapping("/saveDownloadStation")
    public ResponseEntity<DownloadStation> saveDownloadStation(Long id, String name, String downloadUrl, String versionNumber, Integer terminalType
            , String remark, Integer isForced){

        if(LoginUtil.checkNull(name, downloadUrl, versionNumber)){
            return ResponseUtil.custom("参数错误");
        }

        if(terminalType == null || terminalType.intValue() < 1 || terminalType.intValue() > 2 ){
            return ResponseUtil.custom("参数错误");
        }

        if(isForced == null || isForced.intValue() < 1 || isForced.intValue() > 2 ){
            return ResponseUtil.custom("参数错误");
        }

        DownloadStation downloadStation = null;
        if(id != null){
            downloadStation = downloadStationService.findById(id);
        }
        if(downloadStation == null){
            downloadStation = new DownloadStation();
        }
        downloadStation.setName(name);
        downloadStation.setDownloadUrl(downloadUrl);
        downloadStation.setVersionNumber(versionNumber);
        downloadStation.setIsForced(isForced);
        downloadStation.setTerminalType(terminalType);
        if(!LoginUtil.checkNull(remark)){
            downloadStation.setRemark(remark);
        }
        downloadStationService.save(downloadStation);
        return ResponseUtil.success();
    }

}
