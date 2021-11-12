package com.qianyi.casinoproxy.controller.jiceng;

import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinoproxy.model.ProxyHomePageReport;
import com.qianyi.casinoproxy.service.ProxyHomePageReportService;
import com.qianyi.casinoproxy.task.ProxyHomePageReportTask;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

@Api(tags = "首页报表")
@RestController
@Slf4j
@RequestMapping("thridHomeReport/jiceng")
public class ThridHomeReportController {
    @Autowired
    private ProxyHomePageReportService proxyHomePageReportService;

    public final static String start = " 00:00:00";

    public final static String end = " 23:59:59";

    @Autowired
    private ProxyHomePageReportTask proxyHomePageReportTask;

    @Autowired
    private ProxyUserService proxyUserService;

    @ApiOperation("查询基层代理首页报表")
    @GetMapping("/find")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity<ProxyHomePageReport> find(@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (CasinoProxyUtil.checkNull(startDate,endDate)){
            ResponseUtil.custom("参数必填");
        }
        ProxyHomePageReport proxyHomeReport = new  ProxyHomePageReport();
        if (CasinoProxyUtil.setParameter(proxyHomeReport)){
            return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
        }
        ProxyHomePageReport proxyHomePageReport = null;
        try {
            proxyHomePageReport = this.assemble(startDate,endDate);
            List<ProxyHomePageReport> proxyHomePageReports = proxyHomePageReportService.findHomePageReports(proxyHomeReport,DateUtil.getSimpleDateFormat1().format(startDate), DateUtil.getSimpleDateFormat1().format(endDate));
            if (CasinoProxyUtil.checkNull(proxyHomePageReports) || proxyHomePageReports.size() == CommonConst.NUMBER_0){
                return ResponseUtil.success(proxyHomePageReport);
            }
            BigDecimal chargeAmount = proxyHomePageReports.stream().map(ProxyHomePageReport::getChargeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            Integer chargeNums = proxyHomePageReports.stream().mapToInt(ProxyHomePageReport::getChargeNums).sum();
            BigDecimal withdrawMoney = proxyHomePageReports.stream().map(ProxyHomePageReport::getWithdrawMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            Integer withdrawNums = proxyHomePageReports.stream().mapToInt(ProxyHomePageReport::getWithdrawNums).sum();
            BigDecimal validbetAmount = proxyHomePageReports.stream().map(ProxyHomePageReport::getValidbetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal winLossAmount = proxyHomePageReports.stream().map(ProxyHomePageReport::getWinLossAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            Integer activeUsers = proxyHomePageReports.stream().mapToInt(ProxyHomePageReport::getActiveUsers).sum();
            Integer newUsers = proxyHomePageReports.stream().mapToInt(ProxyHomePageReport::getNewUsers).sum();
            proxyHomePageReport.setChargeAmount(proxyHomePageReport.getChargeAmount().add(chargeAmount));
            proxyHomePageReport.setWithdrawMoney(proxyHomePageReport.getWithdrawMoney().add(withdrawMoney));
            proxyHomePageReport.setValidbetAmount(proxyHomePageReport.getValidbetAmount().add(validbetAmount));
            proxyHomePageReport.setWinLossAmount(proxyHomePageReport.getWinLossAmount().add(winLossAmount));
            proxyHomePageReport.setChargeNums(chargeNums + proxyHomePageReport.getChargeNums());
            proxyHomePageReport.setWithdrawNums(withdrawNums + proxyHomePageReport.getWithdrawNums());
            proxyHomePageReport.setActiveUsers(activeUsers + proxyHomePageReport.getActiveUsers());
            proxyHomePageReport.setNewUsers(newUsers + proxyHomePageReport.getNewUsers());
        }catch (Exception ex){
            log.error("首页报表统计失败",ex);
        }
        return ResponseUtil.success(proxyHomePageReport);
    }
    @ApiOperation("查找走势图")
    @GetMapping("/findTrendChart")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity<ProxyHomePageReport> findTrendChart(@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate) {
        if (CasinoProxyUtil.checkNull(startDate, endDate)) {
            ResponseUtil.custom("参数必填");
        }
        ProxyHomePageReport proxyHomeReport = new  ProxyHomePageReport();
        if (CasinoProxyUtil.setParameter(proxyHomeReport)){
            return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
        }
        List<ProxyHomePageReport> list = new LinkedList<>();
        try {
            ProxyHomePageReport proxyHomePageReport = this.assemble(startDate,endDate);
            list.add(proxyHomePageReport);
            Sort sort=Sort.by("id").descending();
            List<ProxyHomePageReport> proxyHomePageReports = proxyHomePageReportService.findHomePageReports(proxyHomeReport,DateUtil.getSimpleDateFormat1().format(startDate), DateUtil.getSimpleDateFormat1().format(endDate),sort);
            if (CasinoProxyUtil.checkNull(proxyHomePageReports) || proxyHomePageReports.size() == CommonConst.NUMBER_0) {
                return ResponseUtil.success(list);
            }
            list.addAll(proxyHomePageReports);
        } catch (Exception ex) {
            log.error("首页报表查找走势图失败", ex);
        }
        return ResponseUtil.success(list);
    }
    private ProxyHomePageReport assemble(Date startDate,Date endDate) throws ParseException {
        Calendar nowTime = Calendar.getInstance();
        String format = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        ProxyHomePageReport proxyHomePageReport = new ProxyHomePageReport();
        proxyHomePageReport.setStaticsTimes(format);
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        String startTime = format + start;
        String endTime = format + end;
        Date start = DateUtil.getSimpleDateFormat().parse(startTime);
        Date end = DateUtil.getSimpleDateFormat().parse(endTime);
        proxyHomePageReportTask.chargeOrder(byId, start, end, proxyHomePageReport);
        proxyHomePageReportTask.withdrawOrder(byId, start, end, proxyHomePageReport);
        proxyHomePageReportTask.gameRecord(byId, startTime, endTime, proxyHomePageReport);
        proxyHomePageReportTask.getNewUsers(byId, start, end, proxyHomePageReport);
        proxyHomePageReportTask.proxyAmount(byId, startDate, endDate, proxyHomePageReport);
        return proxyHomePageReport;
    }
}
