package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.model.dto.VipReportDTO;
import com.qianyi.casinoadmin.model.dto.VipReportOtherProxyDTO;
import com.qianyi.casinoadmin.model.dto.VipReportProxyDTO;
import com.qianyi.casinoadmin.model.dto.VipReportTotalDTO;
import com.qianyi.casinoadmin.service.VipReportService;
import com.qianyi.casinoadmin.util.PageBounds;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.vo.*;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "vip报表")
@Slf4j
@RestController
@RequestMapping("vipReport")
public class VipReportController {
    @Autowired
    private UserService userService;
    @Autowired
    private VipReportService vipReportService;


    @ApiOperation("查询Vip代理报表")
    @GetMapping("/queryVipZdProxy")
    @NoAuthentication
    @NoAuthorization
    public ResponseEntity<VipReportVo> queryVipZdProxy(VipReportProxyDTO vipReportDTO) {
        PageBounds pageBounds = new PageBounds();
        pageBounds.setPageNo(vipReportDTO.getPageCode());
        pageBounds.setPageSize(vipReportDTO.getPageSize());
        return ResponseUtil.success(vipReportService.findVipByProxy(vipReportDTO, pageBounds));
    }


    @ApiOperation("查询下级代理报表")
    @GetMapping("/queryVipOtherProxy")
    @NoAuthentication
    @NoAuthorization
    public ResponseEntity<VipReportVo> queryVipOtherProxy(VipReportOtherProxyDTO vipReportDTO) {
        return ResponseUtil.success(vipReportService.findVipByProxy2(vipReportDTO));
    }


    @ApiOperation("查询Vip报表")
    @GetMapping("/queryPersonReport")
    @NoAuthentication
    @NoAuthorization
    public ResponseEntity<VipReportVo> queryPersonReport(VipReportDTO vipReportDTO) {
        return ResponseUtil.success(vipReportService.findVipReport(vipReportDTO));
    }


    @ApiOperation("查询Vip报表总计")
    @GetMapping("/queryTotal")
    @NoAuthentication
    @NoAuthorization
    public ResponseEntity<VipReportTotalVo> queryTotal(VipReportTotalDTO vipReportTotalDTO) {
//        if (LoginUtil.checkNull(vipReportTotalDTO.getStartDate(), vipReportTotalDTO.getEndDate())) {
//            return ResponseUtil.custom("参数不合法");
//        }
        Long userId;
        LevelReportTotalVo itemObject = null;
        if (StringUtils.hasLength(vipReportTotalDTO.getAccount())) {
            User user = userService.findByAccount(vipReportTotalDTO.getAccount());
            if (user != null) {
                userId = user.getId();
                vipReportTotalDTO.setUserId(userId);
                itemObject = vipReportService.findVipReportTotal(vipReportTotalDTO);

            }
        }else {
            itemObject = vipReportService.findVipReportTotal(vipReportTotalDTO);
        }
        return ResponseUtil.success(itemObject);
    }


}