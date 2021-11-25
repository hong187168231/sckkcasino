package com.qianyi.casinoadmin.task;

import com.qianyi.casinoadmin.model.HomePageReport;
import com.qianyi.casinoadmin.service.HomePageReportService;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.util.TaskConst;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HomePageReportTask {
    public final static String start = " 00:00:00";

    public final static String end = " 23:59:59";

    @Autowired
    private ChargeOrderService chargeOrderService;

    @Autowired
    private WithdrawOrderService withdrawOrderService;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private UserService userService;

    @Autowired
    private ShareProfitChangeService shareProfitChangeService;

    @Autowired
    private HomePageReportService homePageReportService;

    @Autowired
    private WashCodeChangeService washCodeChangeService;

    public final static List<String> list = new LinkedList<>();

    static {
        list.add("2021-11-02");
        list.add("2021-11-03");
        list.add("2021-11-04");
        list.add("2021-11-05");
        list.add("2021-11-06");
        list.add("2021-11-07");
        list.add("2021-11-08");
        list.add("2021-11-09");
        list.add("2021-11-10");
        list.add("2021-11-11");
        list.add("2021-11-12");
        list.add("2021-11-13");
        list.add("2021-11-14");
        list.add("2021-11-15");
        list.add("2021-11-16");
        list.add("2021-11-17");
        list.add("2021-11-18");
        list.add("2021-11-19");
        list.add("2021-11-20");
        list.add("2021-11-21");
        list.add("2021-11-22");
        list.add("2021-11-23");
        list.add("2021-11-24");
    }
    @Scheduled(cron = TaskConst.HOME_PAGE_REPORT)
    public void create(){
        log.info("每日首页报表统计开始start=============================================》");
        Calendar nowTime = Calendar.getInstance();
        nowTime.add(Calendar.DATE, -1);
        String format = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        for (String str:list){
            format = str;
            try {
                String startTime = format + start;
                String endTime = format + end;
                Date startDate = DateUtil.getSimpleDateFormat().parse(startTime);
                Date endDate = DateUtil.getSimpleDateFormat().parse(endTime);
                HomePageReport homePageReport = new HomePageReport();
                homePageReport.setStaticsTimes(format);
                homePageReport.setStaticsMonth(format.substring(CommonConst.NUMBER_0,CommonConst.NUMBER_7));
                homePageReport.setStaticsYear(format.substring(CommonConst.NUMBER_0,CommonConst.NUMBER_4));
                this.chargeOrder(startDate,endDate,homePageReport);
                this.withdrawOrder(startDate,endDate,homePageReport);
                this.gameRecord(startTime,endTime,homePageReport);
                this.shareProfitChange(startDate,endDate,homePageReport);
                this.getNewUsers(startDate,endDate,homePageReport);
                this.bonusAmount(startDate,endDate,homePageReport);
                this.washCodeAmount(startDate,endDate,homePageReport);
                homePageReportService.save(homePageReport);
                log.info("每日首页报表统计结束end=============================================》");
            }catch (Exception ex){
                log.error("首页报表统计失败",ex);
            }
        }

    }
    public void chargeOrder(Date startDate,Date endDate,HomePageReport homePageReport){
        try {
            ChargeOrder chargeOrder = new ChargeOrder();
            chargeOrder.setStatus(CommonConst.NUMBER_1);
            List<ChargeOrder> chargeOrders = chargeOrderService.findChargeOrders(chargeOrder, startDate, endDate);
            if (LoginUtil.checkNull(chargeOrders) || chargeOrders.size() == CommonConst.NUMBER_0){
                homePageReport.setChargeAmount(BigDecimal.ZERO);
                homePageReport.setChargeNums(CommonConst.NUMBER_0);
                return;
            }
            BigDecimal chargeAmount = chargeOrders.stream().map(ChargeOrder::getChargeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
//            BigDecimal serviceCharge = chargeOrders.stream().map(ChargeOrder::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
            homePageReport.setChargeAmount(chargeAmount);
            homePageReport.setChargeNums(chargeOrders.size());
//            homePageReport.setServiceCharge(serviceCharge);
            chargeOrders.clear();
        }catch (Exception ex){
            log.error("统计充值订单失败",ex);
        }
    }
    public void withdrawOrder(Date startDate,Date endDate,HomePageReport homePageReport){
        try {
            WithdrawOrder withdrawOrder = new WithdrawOrder();
            withdrawOrder.setStatus(CommonConst.NUMBER_1);
            List<WithdrawOrder> withdrawOrders = withdrawOrderService.findOrderList(withdrawOrder, startDate, endDate);
            if (LoginUtil.checkNull(withdrawOrders) || withdrawOrders.size() == CommonConst.NUMBER_0){
                homePageReport.setWithdrawMoney(BigDecimal.ZERO);
                homePageReport.setWithdrawNums(CommonConst.NUMBER_0);
                return;
            }
            BigDecimal withdrawMoney = withdrawOrders.stream().map(WithdrawOrder::getWithdrawMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
//            BigDecimal serviceCharge = withdrawOrders.stream().map(WithdrawOrder::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
            homePageReport.setWithdrawMoney(withdrawMoney);
            homePageReport.setWithdrawNums(withdrawOrders.size());
//            homePageReport.setServiceCharge(serviceCharge.add(homePageReport.getServiceCharge()));
            withdrawOrders.clear();
        }catch (Exception ex){
            log.error("统计提现订单失败",ex);
        }
    }
    public void gameRecord(String startTime,String endTime,HomePageReport homePageReport){
        try {
            GameRecord gameRecord = new GameRecord();
            List<GameRecord> gameRecords = gameRecordService.findGameRecords(gameRecord, startTime, endTime);
            if (LoginUtil.checkNull(gameRecord) || gameRecords.size() == CommonConst.NUMBER_0){
                homePageReport.setValidbetAmount(BigDecimal.ZERO);
                homePageReport.setWinLossAmount(BigDecimal.ZERO);
                homePageReport.setActiveUsers(CommonConst.NUMBER_0);
                return;
            }
            BigDecimal validbetAmount = BigDecimal.ZERO;
            BigDecimal winLoss = BigDecimal.ZERO;
            for (GameRecord g : gameRecords){
                validbetAmount = validbetAmount.add(new BigDecimal(g.getValidbet()));
                winLoss = winLoss.add(new BigDecimal(g.getWinLoss()));
            }
            homePageReport.setValidbetAmount(validbetAmount);
            homePageReport.setWinLossAmount(winLoss);
            gameRecords = gameRecords.stream().filter(CommonUtil.distinctByKey(GameRecord::getUser)).collect(Collectors.toList());
            homePageReport.setActiveUsers(gameRecords.size());
            gameRecords.clear();
        }catch (Exception ex){
            log.error("统计三方游戏注单失败",ex);
        }
    }
    public void shareProfitChange(Date startDate,Date endDate,HomePageReport homePageReport){
        try {
            List<ShareProfitChange> shareProfitChanges = shareProfitChangeService.findAll(null,null, startDate, endDate);
            if (LoginUtil.checkNull(shareProfitChanges) || shareProfitChanges.size() == CommonConst.NUMBER_0){
                homePageReport.setShareAmount(BigDecimal.ZERO);
                return;
            }
            BigDecimal contribution = shareProfitChanges.stream().map(ShareProfitChange::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            homePageReport.setShareAmount(contribution);
            shareProfitChanges.clear();
        }catch (Exception ex){
            log.error("统计人人代佣金失败",ex);
        }
    }
    public void getNewUsers(Date startDate,Date endDate,HomePageReport homePageReport){
        try {
            User user = new User();
            Long userCount = userService.findUserCount(user, startDate, endDate);
            homePageReport.setNewUsers(Math.toIntExact(userCount));
        }catch (Exception ex){
            log.error("统计新增用户失败",ex);
        }
    }
    public void bonusAmount(Date startDate,Date endDate,HomePageReport homePageReport){
        try {
            homePageReport.setBonusAmount(BigDecimal.ZERO);
        }catch (Exception ex){
            log.error("统计红利失败",ex);
        }
    }
    public void washCodeAmount(Date startDate,Date endDate,HomePageReport homePageReport){
        try {
            List<WashCodeChange> washCodeChanges = washCodeChangeService.findUserList(startDate, endDate);
            if (LoginUtil.checkNull(washCodeChanges) || washCodeChanges.size() == CommonConst.NUMBER_0){
                homePageReport.setWashCodeAmount(BigDecimal.ZERO);
                return;
            }
            BigDecimal amount = washCodeChanges.stream().map(WashCodeChange::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            homePageReport.setWashCodeAmount(amount);
            washCodeChanges.clear();
        }catch (Exception ex){
            log.error("统计洗码金额失败",ex);
        }
    }
}
