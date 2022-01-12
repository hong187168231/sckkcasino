package com.qianyi.casinoadmin.controller;

import com.mysql.cj.log.Log;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.ReportService;
import com.qianyi.casinocore.service.WashCodeChangeService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;

@Api(tags = "代理实时报表")
@Slf4j
@RestController
@RequestMapping("inTimeReport")
public class InTimeReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private WashCodeChangeService washCodeChangeService;

    @ApiOperation("查询代理报表")
    @GetMapping("/find")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "代理账号", required = false),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity find( String userName,@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (LoginUtil.checkNull(startDate) ||  LoginUtil.checkNull(endDate)){
            return ResponseUtil.custom("参数不合法");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.HOUR, 12);
        startDate = calendar.getTime();
        String startTime = DateUtil.dateToPatten(startDate);
        calendar.setTime(endDate);
        calendar.add(Calendar.HOUR, 12);
        endDate = calendar.getTime();
        String endTime = DateUtil.dateToPatten(endDate);
        if (LoginUtil.checkNull(userName)){
            Map<String, Object> gameResult = reportService.queryReportAll(startTime, endTime);
            BigDecimal washCodeResult = washCodeChangeService.queryWashCodeChangeAll(startTime,endTime);
            Map<String, Object> newMap = new HashMap<>(gameResult);
            newMap.put("wash_amount",washCodeResult);
            return ResponseUtil.success(newMap);
        }else {
            ProxyUser byUserName = proxyUserService.findByUserName(userName);
            if(byUserName != null){
                Map<String,Object> reportResult = null;
                if (byUserName.getProxyRole() == CommonConst.NUMBER_1){
                    reportResult = reportService.queryReportByFirst(byUserName.getId(),startTime,endTime);
                }else if (byUserName.getProxyRole() == CommonConst.NUMBER_2){
                    reportResult = reportService.queryReportBySecond(byUserName.getId(),startTime,endTime);
                }else {
                    reportResult = reportService.queryReportByThird(byUserName.getId(),startTime,endTime);
                }
                return ResponseUtil.success(reportResult);
            }
            List<Map<String,Object>> emptyResult = new ArrayList<Map<String,Object>>();
            return ResponseUtil.success(emptyResult);
        }
    }
}
