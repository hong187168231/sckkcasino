package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.service.HomePageReportService;
import com.qianyi.casinoadmin.task.HomePageReportTask;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinoadmin.vo.HomePageReportVo;
import com.qianyi.casinoadmin.model.HomePageReport;
import com.qianyi.casinocore.model.CompanyProxyDetail;
import com.qianyi.casinocore.service.CompanyProxyDetailService;
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
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Api(tags = "首页报表")
@RestController
@Slf4j
@RequestMapping("homePageReport")
public class HomePageReportController {
    @Autowired
    private HomePageReportService homePageReportService;

    @Autowired
    private HomePageReportTask homePageReportTask;

    public final static String start = " 00:00:00";

    public final static String end = " 23:59:59";

    @Autowired
    private CompanyProxyDetailService companyProxyDetailService;
    @ApiOperation("查询首页报表")
    @GetMapping("/find")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity<HomePageReportVo> find(@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                 @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (LoginUtil.checkNull(startDate,endDate)){
            ResponseUtil.custom("参数必填");
        }
        HomePageReportVo homePageReportVo = null;
        try {
            homePageReportVo = this.assemble(startDate,endDate);
            List<HomePageReport> homePageReports = homePageReportService.findHomePageReports(DateUtil.getSimpleDateFormat1().format(startDate), DateUtil.getSimpleDateFormat1().format(endDate));
            if (LoginUtil.checkNull(homePageReports) || homePageReports.size() == CommonConst.NUMBER_0){
                return ResponseUtil.success(this.getHomePageReportVo(homePageReportVo));
            }
            BigDecimal chargeAmount = homePageReports.stream().map(HomePageReport::getChargeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            Integer chargeNums = homePageReports.stream().mapToInt(HomePageReport::getChargeNums).sum();
            BigDecimal withdrawMoney = homePageReports.stream().map(HomePageReport::getWithdrawMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            Integer withdrawNums = homePageReports.stream().mapToInt(HomePageReport::getWithdrawNums).sum();
            BigDecimal washCodeAmount = homePageReports.stream().map(HomePageReport::getWashCodeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal validbetAmount = homePageReports.stream().map(HomePageReport::getValidbetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal winLossAmount = homePageReports.stream().map(HomePageReport::getWinLossAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal shareAmount = homePageReports.stream().map(HomePageReport::getShareAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal bonusAmount = homePageReports.stream().map(HomePageReport::getBonusAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal serviceCharge = homePageReports.stream().map(HomePageReport::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
            Integer activeUsers = homePageReports.stream().mapToInt(HomePageReport::getActiveUsers).sum();
            Integer newUsers = homePageReports.stream().mapToInt(HomePageReport::getNewUsers).sum();
            homePageReportVo.setChargeAmount(homePageReportVo.getChargeAmount().add(chargeAmount));
            homePageReportVo.setWithdrawMoney(homePageReportVo.getWithdrawMoney().add(withdrawMoney));
            homePageReportVo.setWashCodeAmount(homePageReportVo.getWashCodeAmount().add(washCodeAmount));
            homePageReportVo.setValidbetAmount(homePageReportVo.getValidbetAmount().add(validbetAmount));
            homePageReportVo.setWinLossAmount(homePageReportVo.getWinLossAmount().add(winLossAmount));
            homePageReportVo.setShareAmount(homePageReportVo.getShareAmount().add(shareAmount));
            homePageReportVo.setBonusAmount(homePageReportVo.getBonusAmount().add(bonusAmount));
            homePageReportVo.setServiceCharge(homePageReportVo.getServiceCharge().add(serviceCharge));
            homePageReportVo.setChargeNums(chargeNums + homePageReportVo.getChargeNums());
            homePageReportVo.setWithdrawNums(withdrawNums + homePageReportVo.getWithdrawNums());
            homePageReportVo.setActiveUsers(activeUsers + homePageReportVo.getActiveUsers());
            homePageReportVo.setNewUsers(newUsers + homePageReportVo.getNewUsers());
            this.findCompanyProxyDetails(new CompanyProxyDetail(),DateUtil.getSimpleDateFormat1().format(startDate), DateUtil.getSimpleDateFormat1().format(endDate),homePageReportVo);
        }catch (Exception ex){
            log.error("首页报表统计失败",ex);
        }
        return ResponseUtil.success(this.getHomePageReportVo(homePageReportVo));
    }
    @ApiOperation("查找走势图")
    @GetMapping("/findTrendChart")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity<HomePageReportVo> findTrendChart(@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                              @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate) {
        if (LoginUtil.checkNull(startDate, endDate)) {
            ResponseUtil.custom("参数必填");
        }
        List<HomePageReportVo> list = new LinkedList<>();
        try {
            HomePageReportVo homePageReportVo = this.assemble(startDate,endDate);
            list.add(this.getHomePageReportVo(homePageReportVo));
            Sort sort=Sort.by("id").descending();
            List<HomePageReport> homePageReports = homePageReportService.findHomePageReports(sort,DateUtil.getSimpleDateFormat1().format(startDate), DateUtil.getSimpleDateFormat1().format(endDate));
            if (LoginUtil.checkNull(homePageReports) || homePageReports.size() == CommonConst.NUMBER_0){
                return ResponseUtil.success(list);
            }
            homePageReports.forEach(homePageReport1 -> {
                HomePageReportVo vo = new HomePageReportVo(homePageReport1);
                list.add(this.getHomePageReportVo(vo));
            });
        }catch (Exception ex){
            log.error("首页报表统计失败",ex);
        }
        return ResponseUtil.success(list);

    }
    private void findCompanyProxyDetails(CompanyProxyDetail companyProxyDetail, String startTime, String endTime, HomePageReportVo homePageReportVo){
        List<CompanyProxyDetail> companyProxyDetails = companyProxyDetailService.findCompanyProxyDetails(companyProxyDetail, startTime, endTime);
        if (LoginUtil.checkNull(companyProxyDetails) || companyProxyDetails.size() == CommonConst.NUMBER_0){
            homePageReportVo.setProxyProfit(BigDecimal.ZERO);
            return;
        }
        BigDecimal profitAmount = companyProxyDetails.stream().map(CompanyProxyDetail::getProfitAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        homePageReportVo.setProxyProfit(profitAmount);
    }

    private HomePageReportVo assemble(Date startDate,Date endDate) throws ParseException {
        Calendar nowTime = Calendar.getInstance();
        String format = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        String startTime = format + start;
        String endTime = format + end;
        Date start = DateUtil.getSimpleDateFormat().parse(startTime);
        Date end = DateUtil.getSimpleDateFormat().parse(endTime);
        HomePageReport homePageReport = new HomePageReport();
        homePageReport.setStaticsTimes(format);
        homePageReportTask.chargeOrder(start,end,homePageReport);
        homePageReportTask.withdrawOrder(start,end,homePageReport);
        homePageReportTask.gameRecord(startTime,endTime,homePageReport);
        homePageReportTask.shareProfitChange(start,end,homePageReport);
        homePageReportTask.getNewUsers(start,end,homePageReport);
        homePageReportTask.bonusAmount(startDate,endDate,homePageReport);
        homePageReportTask.washCodeAmount(startDate,endDate,homePageReport);
        HomePageReportVo homePageReportVo = new HomePageReportVo(homePageReport);
        return homePageReportVo;
    }
    private HomePageReportVo getHomePageReportVo(HomePageReportVo homePageReportVo){
        homePageReportVo.setGrossMargin1(homePageReportVo.getWinLossAmount().subtract(homePageReportVo.getWashCodeAmount()));
        homePageReportVo.setGrossMargin2(homePageReportVo.getGrossMargin1().subtract(homePageReportVo.getShareAmount()).subtract(homePageReportVo.getBonusAmount()).add(homePageReportVo.getServiceCharge()));
        homePageReportVo.setGrossMargin3(homePageReportVo.getGrossMargin2().subtract(homePageReportVo.getProxyProfit()));
        return homePageReportVo;
    }
}
