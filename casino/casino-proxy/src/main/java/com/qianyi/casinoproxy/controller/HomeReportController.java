package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.casinoproxy.vo.ProxyHomePageReportVo;
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
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "代理首页报表")
@RestController
@Slf4j
@RequestMapping("thridHomeReport")
public class HomeReportController {
    @Autowired
    private ProxyHomePageReportService proxyHomePageReportService;

    public final static String start = " 00:00:00";

    public final static String end = " 23:59:59";

    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private CompanyProxyMonthService companyProxyMonthService;

    @Autowired
    private UserRunningWaterService userRunningWaterService;

    @ApiOperation("查询代理首页报表")
    @GetMapping("/find")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
    })
    public ResponseEntity<ProxyHomePageReportVo> find(@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        ProxyHomePageReport proxyHomeReport = new  ProxyHomePageReport();
        proxyHomeReport.setProxyUserId(CasinoProxyUtil.getAuthId());
        ProxyHomePageReportVo proxyHomePageReportVo = null;
        try {
            proxyHomePageReportVo = this.assemble();
            String startTime = startDate==null? null:DateUtil.getSimpleDateFormat1().format(startDate);
            String endTime =  endDate==null? null:DateUtil.getSimpleDateFormat1().format(endDate);
            List<ProxyHomePageReport> proxyHomePageReports = proxyHomePageReportService.findHomePageReports(proxyHomeReport,startTime,endTime);
            if (CasinoProxyUtil.checkNull(proxyHomePageReports) || proxyHomePageReports.size() == CommonConst.NUMBER_0){
                return ResponseUtil.success(proxyHomePageReportVo);
            }
            BigDecimal chargeAmount = proxyHomePageReports.stream().map(ProxyHomePageReport::getChargeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            Integer chargeNums = proxyHomePageReports.stream().mapToInt(ProxyHomePageReport::getChargeNums).sum();
            BigDecimal withdrawMoney = proxyHomePageReports.stream().map(ProxyHomePageReport::getWithdrawMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            Integer withdrawNums = proxyHomePageReports.stream().mapToInt(ProxyHomePageReport::getWithdrawNums).sum();
            BigDecimal validbetAmount = proxyHomePageReports.stream().map(ProxyHomePageReport::getValidbetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal winLossAmount = proxyHomePageReports.stream().map(ProxyHomePageReport::getWinLossAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            Integer newUsers = proxyHomePageReports.stream().mapToInt(ProxyHomePageReport::getNewUsers).sum();
            Integer newThirdProxys = proxyHomePageReports.stream().mapToInt(ProxyHomePageReport::getNewThirdProxys).sum();
            Integer newSecondProxys = proxyHomePageReports.stream().mapToInt(ProxyHomePageReport::getNewSecondProxys).sum();
            proxyHomePageReportVo.setChargeAmount(proxyHomePageReportVo.getChargeAmount().add(chargeAmount));
            proxyHomePageReportVo.setWithdrawMoney(proxyHomePageReportVo.getWithdrawMoney().add(withdrawMoney));
            proxyHomePageReportVo.setValidbetAmount(proxyHomePageReportVo.getValidbetAmount().add(validbetAmount));
            proxyHomePageReportVo.setWinLossAmount(proxyHomePageReportVo.getWinLossAmount().add(winLossAmount));
            proxyHomePageReportVo.setChargeNums(chargeNums + proxyHomePageReportVo.getChargeNums());
            proxyHomePageReportVo.setWithdrawNums(withdrawNums + proxyHomePageReportVo.getWithdrawNums());
            proxyHomePageReportVo.setNewUsers(newUsers + proxyHomePageReportVo.getNewUsers());
            proxyHomePageReportVo.setNewThirdProxys(newThirdProxys + proxyHomePageReportVo.getNewThirdProxys());
            proxyHomePageReportVo.setNewSecondProxys(newSecondProxys + proxyHomePageReportVo.getNewSecondProxys());
            UserRunningWater userRunningWater = new UserRunningWater();
            if (CasinoProxyUtil.setParameter(userRunningWater)){
                return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
            }
            List<UserRunningWater> userRunningWaterList = userRunningWaterService.findUserRunningWaterList(userRunningWater, startTime, endTime);
            Set<Long> userIdSet = proxyHomePageReportVo.getUserIdSet();
            if (CasinoProxyUtil.checkNull(userIdSet)){
                userIdSet = new HashSet<>();
            }
            for (UserRunningWater u : userRunningWaterList){
                userIdSet.add(u.getUserId());
            }
            proxyHomePageReportVo.setActiveUsers(userIdSet.size());
            userIdSet.clear();
            CompanyProxyMonth companyProxyMonth = new CompanyProxyMonth();
            companyProxyMonth.setUserId(CasinoProxyUtil.getAuthId());
            this.findCompanyProxyDetails(companyProxyMonth,startTime,endTime,proxyHomePageReportVo);
        }catch (Exception ex){
            log.error("首页报表统计失败",ex);
            return ResponseUtil.custom("查询失败");
        }
        return ResponseUtil.success(proxyHomePageReportVo);
    }
    @ApiOperation("查找走势图")
    @GetMapping("/findTrendChart")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "代理账号", required = false),
            @ApiImplicitParam(name = "tag", value = "1:每日 2:每月 3:每年", required = false),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity<ProxyHomePageReportVo> findTrendChart(Integer tag,@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate,String userName) {
        if (CasinoProxyUtil.checkNull(startDate, endDate)) {
            return ResponseUtil.custom("参数必填");
        }
        ProxyHomePageReport proxyHomeReport = new  ProxyHomePageReport();
        List<ProxyHomePageReportVo> list = new LinkedList<>();
        if (CasinoProxyUtil.checkNull(userName)){
            proxyHomeReport.setProxyUserId(CasinoProxyUtil.getAuthId());
        }else {
            ProxyUser byUserName = proxyUserService.findByUserName(userName);
            if (CasinoProxyUtil.checkNull(byUserName)){
                return ResponseUtil.success(byUserName);
            }
            proxyHomeReport.setProxyUserId(CasinoProxyUtil.getAuthId());
            if (CasinoProxyUtil.setParameter(proxyHomeReport)){
                return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
            }
        }
        try {
            if ((DateUtil.isEffectiveDate(new Date(),startDate,endDate))){
                ProxyHomePageReportVo proxyHomePageReportVo = this.assemble();
                list.add(proxyHomePageReportVo);
            }
            Sort sort=Sort.by("id").descending();
            List<ProxyHomePageReport> proxyHomePageReports = proxyHomePageReportService.findHomePageReports(proxyHomeReport,DateUtil.getSimpleDateFormat1().format(startDate), DateUtil.getSimpleDateFormat1().format(endDate),sort);
            if (CasinoProxyUtil.checkNull(proxyHomePageReports) || proxyHomePageReports.size() == CommonConst.NUMBER_0) {
                return ResponseUtil.success(list);
            }
            proxyHomePageReports.forEach(homePageReport1 -> {
                ProxyHomePageReportVo vo = new ProxyHomePageReportVo(homePageReport1);
                list.add(vo);
            });
            if (CasinoProxyUtil.checkNull(tag) || tag == CommonConst.NUMBER_1){
                Collections.reverse(list);
                return ResponseUtil.success(list);
            }else if (tag == CommonConst.NUMBER_2){
                Map<String, List<ProxyHomePageReportVo>> map = list.stream().collect(Collectors.groupingBy(ProxyHomePageReportVo::getStaticsMonth));
                list.clear();
                map.forEach((key,value)->{
                    list.add(this.getHomePageReportVo(value,key));
                });
            }else {
                Map<String, List<ProxyHomePageReportVo>> map = list.stream().collect(Collectors.groupingBy(ProxyHomePageReportVo::getStaticsYear));
                list.clear();
                map.forEach((key,value)->{
                    list.add(this.getHomePageReportVo(value,key));
                });
            }
        } catch (Exception ex) {
            log.error("首页报表查找走势图失败", ex);
            return ResponseUtil.custom("查询失败");
        }
        Collections.reverse(list);
        return ResponseUtil.success(list);
    }
    private ProxyHomePageReportVo getHomePageReportVo(List<ProxyHomePageReportVo> list,String time){
        ProxyHomePageReportVo vo = new ProxyHomePageReportVo();
        BigDecimal chargeAmount = list.stream().map(ProxyHomePageReportVo::getChargeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setChargeAmount(chargeAmount);
        BigDecimal validbetAmount = list.stream().map(ProxyHomePageReportVo::getValidbetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setValidbetAmount(validbetAmount);
        if (validbetAmount.compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 || chargeAmount.compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 ){
            vo.setOddsRatio(chargeAmount);
        }else {
            vo.setOddsRatio(chargeAmount.divide(validbetAmount,2, RoundingMode.HALF_UP));
        }
        Integer newUsers = list.stream().mapToInt(ProxyHomePageReportVo::getNewUsers).sum();
        vo.setNewUsers(newUsers);
        Integer activeUsers = list.stream().mapToInt(ProxyHomePageReportVo::getActiveUsers).sum();
        vo.setActiveUsers(activeUsers);
        vo.setTime(time);
        return vo;
    }

    private void findCompanyProxyDetails(CompanyProxyMonth companyProxyMonth, String startTime, String endTime,ProxyHomePageReportVo proxyHomePageReportVo){
        List<CompanyProxyMonth> companyProxyMonths = companyProxyMonthService.findCompanyProxyMonths(companyProxyMonth, startTime, endTime);
        if (CasinoProxyUtil.checkNull(companyProxyMonths) || companyProxyMonths.size() == CommonConst.NUMBER_0){
            proxyHomePageReportVo.setGroupTotalProfit(BigDecimal.ZERO);
            proxyHomePageReportVo.setTotalProfit(BigDecimal.ZERO);
            return;
        }
        companyProxyMonths = companyProxyMonths.stream().filter(companyProxy -> !companyProxy.getStaticsTimes().equals(proxyHomePageReportVo.getStaticsMonth())).collect(Collectors.toList());
        BigDecimal groupTotalprofit = companyProxyMonths.stream().map(CompanyProxyMonth::getGroupTotalprofit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal profitAmount = companyProxyMonths.stream().map(CompanyProxyMonth::getProfitAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        proxyHomePageReportVo.setGroupTotalProfit(groupTotalprofit);
        proxyHomePageReportVo.setTotalProfit(profitAmount);
    }
    private ProxyHomePageReportVo assemble() throws ParseException {
        Calendar nowTime = Calendar.getInstance();
        String format = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        ProxyHomePageReport proxyHomePageReport = new ProxyHomePageReport();
        proxyHomePageReport.setStaticsTimes(format);
        proxyHomePageReport.setStaticsYear(format.substring(CommonConst.NUMBER_0,CommonConst.NUMBER_4));
        proxyHomePageReport.setStaticsMonth(format.substring(CommonConst.NUMBER_0,CommonConst.NUMBER_7));
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        String startTime = format + start;
        String endTime = format + end;
        Date start = DateUtil.getSimpleDateFormat().parse(startTime);
        Date end = DateUtil.getSimpleDateFormat().parse(endTime);
        proxyHomePageReportService.chargeOrder(byId, start, end, proxyHomePageReport);
        proxyHomePageReportService.withdrawOrder(byId, start, end, proxyHomePageReport);
        Set<Long> set = proxyHomePageReportService.gameRecord(byId, startTime, endTime, proxyHomePageReport);
        proxyHomePageReportService.getNewUsers(byId, start, end, proxyHomePageReport);
        if (byId.getProxyRole() == CommonConst.NUMBER_1){
            proxyHomePageReportService.getNewSecondProxys(byId,start, end,proxyHomePageReport);
        }
        proxyHomePageReportService.getNewThirdProxys(byId,start, end,proxyHomePageReport);
        ProxyHomePageReportVo proxyHomePageReportVo = new ProxyHomePageReportVo(proxyHomePageReport,set);
        return proxyHomePageReportVo;
    }
}
