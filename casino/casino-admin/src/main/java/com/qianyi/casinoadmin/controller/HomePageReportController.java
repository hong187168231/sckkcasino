package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.service.HomePageReportService;
import com.qianyi.casinoadmin.task.HomePageReportTask;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinoadmin.vo.HomePageReportVo;
import com.qianyi.casinoadmin.model.HomePageReport;
import com.qianyi.casinocore.model.CompanyProxyDetail;
import com.qianyi.casinocore.model.CompanyProxyMonth;
import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.UserRunningWater;
import com.qianyi.casinocore.service.CompanyProxyMonthService;
import com.qianyi.casinocore.service.GameRecordGoldenFService;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.casinocore.service.UserRunningWaterService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.DateUtil;
import com.qianyi.modulejjwt.JjwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

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

    public final static String startMonth = "-01";

    public final static String endMonth = "-12";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CompanyProxyMonthService companyProxyMonthService;

    @Autowired
    private UserRunningWaterService userRunningWaterService;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;
    @ApiOperation("查询首页报表")
    @GetMapping("/find")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<HomePageReportVo> find(@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        HomePageReportVo homePageReportVo = null;
        try {
            homePageReportVo = this.assemble();

            // yyyy-MM-dd
            String startTime = startDate==null? null:DateUtil.getSimpleDateFormat1().format(startDate);
            String endTime =  endDate==null? null:DateUtil.getSimpleDateFormat1().format(endDate);

            // home_page_report
            List<HomePageReport> homePageReports = homePageReportService.findHomePageReports(startTime,endTime);
            if (LoginUtil.checkNull(homePageReports) || homePageReports.size() == CommonConst.NUMBER_0){
                return ResponseUtil.success(this.getHomePageReportVo(homePageReportVo));
            }

            // 充值金额
            BigDecimal chargeAmount = homePageReports.stream().map(HomePageReport::getChargeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            // 充值单数
            Integer chargeNums = homePageReports.stream().mapToInt(HomePageReport::getChargeNums).sum();
            // 提款金额
            BigDecimal withdrawMoney = homePageReports.stream().map(HomePageReport::getWithdrawMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            // 提款单数
            Integer withdrawNums = homePageReports.stream().mapToInt(HomePageReport::getWithdrawNums).sum();
            // 派发洗码
            BigDecimal washCodeAmount = homePageReports.stream().map(HomePageReport::getWashCodeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            // 投注金额
            BigDecimal validbetAmount = homePageReports.stream().map(HomePageReport::getValidbetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            // 平台输赢
            BigDecimal winLossAmount = homePageReports.stream().map(HomePageReport::getWinLossAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            // 人人代佣金
            BigDecimal shareAmount = homePageReports.stream().map(HomePageReport::getShareAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal bonusAmount = homePageReports.stream().map(HomePageReport::getBonusAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            // 抽取提款手续费
            BigDecimal serviceCharge = homePageReports.stream().map(HomePageReport::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
            // 抽点总额
            BigDecimal extractPointsAmount = homePageReports.stream().map(HomePageReport::getExtractPointsAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            Integer newUsers = homePageReports.stream().mapToInt(HomePageReport::getNewUsers).sum();
            homePageReportVo.setChargeAmount(homePageReportVo.getChargeAmount().add(chargeAmount));
            homePageReportVo.setWithdrawMoney(homePageReportVo.getWithdrawMoney().add(withdrawMoney));
            homePageReportVo.setWashCodeAmount(homePageReportVo.getWashCodeAmount().add(washCodeAmount));
            homePageReportVo.setValidbetAmount(homePageReportVo.getValidbetAmount().add(validbetAmount));
            homePageReportVo.setWinLossAmount(homePageReportVo.getWinLossAmount().subtract(winLossAmount));
            homePageReportVo.setShareAmount(homePageReportVo.getShareAmount().add(shareAmount));
            homePageReportVo.setBonusAmount(homePageReportVo.getBonusAmount().add(bonusAmount));
            homePageReportVo.setServiceCharge(homePageReportVo.getServiceCharge().add(serviceCharge));
            homePageReportVo.setChargeNums(chargeNums + homePageReportVo.getChargeNums());
            homePageReportVo.setWithdrawNums(withdrawNums + homePageReportVo.getWithdrawNums());
            homePageReportVo.setNewUsers(newUsers + homePageReportVo.getNewUsers());
            // 设置抽点总额
            homePageReportVo.setExtractPointsAmount(extractPointsAmount);

            UserRunningWater userRunningWater = new UserRunningWater();
            List<UserRunningWater> userRunningWaterList = userRunningWaterService.findUserRunningWaterList(userRunningWater, startTime, endTime);
            Set<Long> userIdSet = homePageReportVo.getUserIdSet();
            if (LoginUtil.checkNull(userIdSet)){
                userIdSet = new HashSet<>();
            }
            for (UserRunningWater u : userRunningWaterList){
                userIdSet.add(u.getUserId());
            }
            // 活跃用户
            homePageReportVo.setActiveUsers(userIdSet.size());
            userIdSet.clear();
            this.findCompanyProxyDetails(new CompanyProxyMonth(),startTime,endTime,homePageReportVo);
        }catch (Exception ex){
            log.error("首页报表统计失败",ex);
            return ResponseUtil.custom("查询失败");
        }
        return ResponseUtil.success(this.getHomePageReportVo(homePageReportVo));
    }
    @ApiOperation("查找走势图")
    @GetMapping("/findTrendChart")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "tag", value = "1:每日 2:每月 3:每年", required = false),
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    @NoAuthorization
    public ResponseEntity<HomePageReportVo> findTrendChart(Integer tag,@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate) {
        if (LoginUtil.checkNull(startDate, endDate)) {
            ResponseUtil.custom("参数必填");
        }
        List<HomePageReportVo> list = new LinkedList<>();
        try {
            if ((DateUtil.isEffectiveDate(new Date(),startDate,endDate))){
                HomePageReportVo homePageReportVo = this.assemble();
                list.add(this.getHomePageReportVo(homePageReportVo));
            }
            Sort sort=Sort.by("id").descending();
            List<HomePageReport> homePageReports = homePageReportService.findHomePageReports(sort,DateUtil.getSimpleDateFormat1().format(startDate), DateUtil.getSimpleDateFormat1().format(endDate));
            if (LoginUtil.checkNull(homePageReports) || homePageReports.size() == CommonConst.NUMBER_0){
                return ResponseUtil.success(list);
            }
            homePageReports.forEach(homePageReport1 -> {
                HomePageReportVo vo = new HomePageReportVo(homePageReport1);
                vo.setWinLossAmount(vo.getWinLossAmount().negate());
                list.add(this.getHomePageReportVo(vo));
            });
            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_1){
                Collections.reverse(list);
                return ResponseUtil.success(list);
            }else if (tag == CommonConst.NUMBER_2){
                Map<String, List<HomePageReportVo>> map = list.stream().collect(Collectors.groupingBy(HomePageReportVo::getStaticsMonth));
                list.clear();
                map.forEach((key,value)->{
                    list.add(this.getHomePageReportVo(value,key,CommonConst.NUMBER_2));
                });
            }else {
                Map<String, List<HomePageReportVo>> map = list.stream().collect(Collectors.groupingBy(HomePageReportVo::getStaticsYear));
                list.clear();
                map.forEach((key,value)->{
                    list.add(this.getHomePageReportVo(value,key,CommonConst.NUMBER_3));
                });
            }
        }catch (Exception ex){
            log.error("首页报表统计失败",ex);
            return ResponseUtil.custom("查询失败");
        }
        Collections.reverse(list);
        return ResponseUtil.success(list);
    }

    @ApiOperation("查找在线人数")
    @GetMapping("/findOnlineUser")
    @NoAuthorization
    public ResponseEntity findOnlineUser(){
        Set<String> keys = redisTemplate.keys("token::casino-web::*");
        Integer count = 0;
        try {
            for (String s: keys){
                JjwtUtil.Token refreshJwtToken = (JjwtUtil.Token)redisTemplate.opsForValue().get(s);
                if (LoginUtil.checkNull(refreshJwtToken)){
                    continue;
                }
                if (JjwtUtil.check(LoginUtil.checkNull(refreshJwtToken.getNewToken())?refreshJwtToken.getOldToken():refreshJwtToken.getNewToken(), Constants.CASINO_WEB)) {
                    count++;
                }
            }
            keys.clear();
        }catch (Exception ex){
            return ResponseUtil.custom("查询失败");
        }
        return ResponseUtil.success(count);
    }
    private HomePageReportVo getHomePageReportVo(List<HomePageReportVo> list,String time,Integer tag){
        HomePageReportVo vo = new HomePageReportVo();
        BigDecimal chargeAmount = list.stream().map(HomePageReportVo::getChargeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setChargeAmount(chargeAmount);
        BigDecimal validbetAmount = list.stream().map(HomePageReportVo::getValidbetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setValidbetAmount(validbetAmount);
        if (validbetAmount.compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 || chargeAmount.compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 ){
            vo.setOddsRatio(chargeAmount);
        }else {
            vo.setOddsRatio(chargeAmount.divide(validbetAmount,2, RoundingMode.HALF_UP));
        }
        BigDecimal grossMargin1 = list.stream().map(HomePageReportVo::getGrossMargin1).reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setGrossMargin1(grossMargin1);
        BigDecimal grossMargin2 = list.stream().map(HomePageReportVo::getGrossMargin2).reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setGrossMargin2(grossMargin2);
        BigDecimal grossMargin3 = list.stream().map(HomePageReportVo::getGrossMargin3).reduce(BigDecimal.ZERO, BigDecimal::add);
        Calendar nowTime = Calendar.getInstance();
        vo.setStaticsMonth(DateUtil.getSimpleDateFormat1().format(nowTime.getTime()).substring(CommonConst.NUMBER_0,CommonConst.NUMBER_7));
        if (tag == CommonConst.NUMBER_2){
            this.findCompanyProxyDetails(new CompanyProxyMonth(),time,time,vo);
        }else {
            this.findCompanyProxyDetails(new CompanyProxyMonth(),time + startMonth,time + endMonth,vo);
        }
        vo.setGrossMargin3(grossMargin3.subtract(vo.getProxyProfit()));
        vo.setTime(time);
        return vo;
    }

    private void findCompanyProxyDetails(CompanyProxyMonth companyProxyMonth, String startTime, String endTime, HomePageReportVo homePageReportVo){
        List<CompanyProxyMonth> companyProxyMonths = companyProxyMonthService.findCompanyProxyMonths(companyProxyMonth, startTime, endTime);
        if (LoginUtil.checkNull(companyProxyMonths) || companyProxyMonths.size() == CommonConst.NUMBER_0){
            homePageReportVo.setProxyProfit(BigDecimal.ZERO);
            return;
        }
        companyProxyMonths = companyProxyMonths.stream().filter(companyProxy -> !companyProxy.getStaticsTimes().equals(homePageReportVo.getStaticsMonth())).collect(Collectors.toList());
        BigDecimal profitAmount = companyProxyMonths.stream().map(CompanyProxyMonth::getProfitAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        homePageReportVo.setProxyProfit(profitAmount);
    }

    private HomePageReportVo assemble() throws ParseException {
        Calendar nowTime = Calendar.getInstance();
        String format = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        String startTime = format + start;
        String endTime = format + end;
        Date start = DateUtil.getSimpleDateFormat().parse(startTime);
        Date end = DateUtil.getSimpleDateFormat().parse(endTime);
        HomePageReport homePageReport = new HomePageReport();
        homePageReport.setStaticsTimes(format);
        homePageReport.setStaticsYear(format.substring(CommonConst.NUMBER_0, CommonConst.NUMBER_4));
        homePageReport.setStaticsMonth(format.substring(CommonConst.NUMBER_0, CommonConst.NUMBER_7));
        homePageReportTask.chargeOrder(start, end, homePageReport);
        homePageReportTask.withdrawOrder(start, end, homePageReport);
        Set<Long> set = this.gameRecord(startTime, endTime, homePageReport);
        homePageReportTask.shareProfitChange(start, end, homePageReport);
        homePageReportTask.getNewUsers(start, end, homePageReport);
        homePageReportTask.bonusAmount(start, end, homePageReport);
        homePageReportTask.washCodeAmount(start, end, homePageReport);
        homePageReportTask.extractPointsAmount(start, end, homePageReport);
        HomePageReportVo homePageReportVo = new HomePageReportVo(homePageReport,set);
        return homePageReportVo;
    }
    private Set<Long>  gameRecord(String startTime, String endTime, HomePageReport homePageReport){
        try {
            Map<String, Object> gameRecordSum = gameRecordService.findSumBetAndWinLoss(startTime, endTime);
            BigDecimal gameRecordValidbet = gameRecordSum.get("validbet") == null?BigDecimal.ZERO:new BigDecimal(gameRecordSum.get("validbet").toString());
            BigDecimal gameRecordWinLoss = gameRecordSum.get("winLoss") == null?BigDecimal.ZERO:new BigDecimal(gameRecordSum.get("winLoss").toString());

            Map<String, Object> gameRecordGoldenFSum = gameRecordGoldenFService.findSumBetAndWinLoss(startTime, endTime);
            BigDecimal gameRecordGoldenFValidbet = gameRecordGoldenFSum.get("betAmount") == null?BigDecimal.ZERO:new BigDecimal(gameRecordGoldenFSum.get("betAmount").toString());
            BigDecimal gameRecordGoldenFWinLoss = gameRecordGoldenFSum.get("winAmount") == null?BigDecimal.ZERO:new BigDecimal(gameRecordGoldenFSum.get("winAmount").toString());

            homePageReport.setValidbetAmount(gameRecordValidbet.add(gameRecordGoldenFValidbet));
            homePageReport.setWinLossAmount(BigDecimal.ZERO.subtract(gameRecordWinLoss).subtract(gameRecordGoldenFWinLoss));
            Set<Long> gameRecordGoldenFUser = gameRecordGoldenFService.findGroupByUser(startTime, endTime);
            Set<Long> gameRecordUser = gameRecordService.findGroupByUser(startTime, endTime);
            gameRecordGoldenFUser.addAll(gameRecordUser);
            return gameRecordGoldenFUser;
        }catch (Exception ex){
            log.error("统计三方游戏注单失败",ex);
        }
        return null;
    }

    // 计算毛利
    private HomePageReportVo getHomePageReportVo(HomePageReportVo homePageReportVo){
        // 毛利1需要修改:
        // 毛利1 = 平台输赢金额 - 洗码 - 代理抽点
        homePageReportVo.setGrossMargin1(homePageReportVo.getWinLossAmount().subtract(homePageReportVo.getWashCodeAmount()).subtract(homePageReportVo.getExtractPointsAmount()));
        homePageReportVo.setGrossMargin2(homePageReportVo.getGrossMargin1().subtract(homePageReportVo.getShareAmount()).subtract(homePageReportVo.getBonusAmount()).add(homePageReportVo.getServiceCharge()));
        homePageReportVo.setGrossMargin3(homePageReportVo.getGrossMargin2().subtract(homePageReportVo.getProxyProfit()));
        return homePageReportVo;
    }
}
