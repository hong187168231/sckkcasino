package com.qianyi.casinoadmin.controller;

import cn.hutool.core.date.DateUtil;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.UserLevelService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.DTOUtil;
import com.qianyi.casinocore.vo.*;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Api(tags = "vip报表")
@Slf4j
@RestController
@RequestMapping("vipReport")
public class VipReportController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserLevelService userLevelService;

    @ApiOperation("查询Vip报表")
    @GetMapping("/queryPersonReport")
    @NoAuthentication
    @NoAuthorization
    @ApiImplicitParams({@ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "account", value = "账号", required = false),
            @ApiImplicitParam(name = "startTime", value = "起始时间查询", required = true),
            @ApiImplicitParam(name = "endTime", value = "结束时间查询", required = true),
            @ApiImplicitParam(name = "levelArray", value = "等级数组,逗号分隔", required = true)})
    public ResponseEntity<PersonReportVo> queryPersonReport(Integer pageSize, Integer pageCode, String account,
                                                            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
                                                            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime, String levelArray) {
        if (LoginUtil.checkNull(startTime, endTime, pageSize, pageCode)) {
            return ResponseUtil.custom("参数不合法");
        }
        String orderTimeStart = "'" + DateUtil.formatDate(startTime) + "'";
        String orderTimeEnd = "'" + DateUtil.formatDate(endTime) + "'";
        String startTimeStr = DateUtil.formatDateTime(startTime);
        String endTimeStr = DateUtil.formatDateTime(endTime);
        if (StringUtils.hasLength(account)) {
            User user = userService.findByAccount(account);
            if (user != null) {
                List<VipReportVo> reportResult = userLevelService.findVipMap(startTimeStr, endTimeStr, levelArray, user.getId());
                PageResultVO<VipReportVo> mapPageResultVO = combinePage(reportResult, 1, pageCode, pageSize);
                return ResponseUtil.success(mapPageResultVO);
            }
        } else {
            List<VipReportVo> reportResult = userLevelService.findVipMap(startTimeStr, endTimeStr, levelArray, null);
            PageResultVO<VipReportVo> mapPageResultVO = combinePage(reportResult, 1, pageCode, pageSize);
            return ResponseUtil.success(mapPageResultVO);
        }
        return ResponseUtil.success();
    }


    private PageResultVO<VipReportVo> combinePage(List<VipReportVo> reportResult, int totalElement, int page,
                                                  int num) {
        PageResultVO<VipReportVo> pageResult =
                new PageResultVO<>(page, num, Long.parseLong(totalElement + ""), reportResult);
        return pageResult;
    }


    @ApiOperation("查询Vip报表总计")
    @GetMapping("/queryTotal")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "账号", required = false),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
            @ApiImplicitParam(name = "levelArray", value = "等级数组,逗号分隔", required = true)
    })
    public ResponseEntity<VipReportTotalVo> queryTotal(String account,
                                                          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
                                                          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate, String levelArray) {
        if (LoginUtil.checkNull(startDate, endDate)) {
            return ResponseUtil.custom("参数不合法");
        }
        String orderTimeStart = "'" + DateUtil.formatDate(startDate) + "'";
        String orderTimeEnd = "'" + DateUtil.formatDate(endDate) + "'";
        String startTime = DateUtil.formatDateTime(startDate);
        String endTime = DateUtil.formatDateTime(endDate);
        Long userId;
        VipReportTotalVo itemObject = null;
        if (StringUtils.hasLength(account)) {
            User user = userService.findByAccount(account);
            if (user != null) {
                userId = user.getId();
                Map<String, Object> maps =
                        userLevelService.findVipTotalMap(startTime, endTime, levelArray, userId);
                itemObject = DTOUtil.toDTO(maps.get(0), VipReportTotalVo.class);
            }
        } else {
            Map<String, Object> result =
                    userLevelService.findVipTotalMap(startTime, endTime, levelArray, null);
            itemObject = DTOUtil.toDTO(result, VipReportTotalVo.class);
        }
        return ResponseUtil.success(itemObject);
    }


}