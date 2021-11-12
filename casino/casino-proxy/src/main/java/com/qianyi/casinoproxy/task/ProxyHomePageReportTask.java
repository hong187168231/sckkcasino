package com.qianyi.casinoproxy.task;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.TaskConst;
import com.qianyi.casinoproxy.model.ProxyHomePageReport;
import com.qianyi.casinoproxy.service.ProxyHomePageReportService;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProxyHomePageReportTask {
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
    private ProxyUserService proxyUserService;

    @Autowired
    private ProxyHomePageReportService proxyHomePageReportService;
    @Scheduled(cron = TaskConst.PROXY_HOME_PAGE_REPORT)
    public void create(){
        log.info("每日代理首页报表统计开始start=============================================》");
        Calendar nowTime = Calendar.getInstance();
        nowTime.add(Calendar.DATE, -1);
        String format = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        try {
            String startTime = format + start;
            String endTime = format + end;
            Date startDate = DateUtil.getSimpleDateFormat().parse(startTime);
            Date endDate = DateUtil.getSimpleDateFormat().parse(endTime);
            ProxyUser proxyUser = new ProxyUser();
            proxyUser.setIsDelete(CommonConst.NUMBER_1);
            proxyUser.setUserFlag(CommonConst.NUMBER_1);
            List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
            if (CasinoProxyUtil.checkNull(proxyUserList) || proxyUserList.size() == CommonConst.NUMBER_0){
                return;
            }
            proxyUserList.forEach(proxy -> {
                new Thread(()->this.create(proxy,startTime,endTime,startDate,endDate,format)).start();
            });
            log.info("每日代理首页报表统计结束end=============================================》");
        }catch (Exception ex){
            log.error("代理首页报表统计失败",ex);
        }
    }
    public void create(ProxyUser proxyUser,String startTime,String endTime,Date startDate,Date endDate,String format){
        ProxyHomePageReport proxyHomePageReport = new ProxyHomePageReport();
        proxyHomePageReport.setStaticsTimes(format);
        this.chargeOrder(proxyUser,startDate,endDate,proxyHomePageReport);
        this.withdrawOrder(proxyUser,startDate,endDate,proxyHomePageReport);
        this.gameRecord(proxyUser,startTime,endTime,proxyHomePageReport);
        this.getNewUsers(proxyUser,startDate,endDate,proxyHomePageReport);
        this.proxyAmount(proxyUser,startDate,endDate,proxyHomePageReport);
        if (proxyUser.getProxyRole() == CommonConst.NUMBER_3){
            proxyHomePageReport.setFirstProxy(proxyUser.getFirstProxy());
            proxyHomePageReport.setSecondProxy(proxyUser.getSecondProxy());
            proxyHomePageReport.setThirdProxy(proxyUser.getId());
        }
        if (proxyUser.getProxyRole() == CommonConst.NUMBER_2){
            proxyHomePageReport.setFirstProxy(proxyUser.getFirstProxy());
            proxyHomePageReport.setSecondProxy(proxyUser.getId());
            proxyHomePageReport.setThirdProxy(CommonConst.LONG_0);
            this.getNewThirdProxys(proxyUser,startDate,endDate,proxyHomePageReport);
        }
        if (proxyUser.getProxyRole() == CommonConst.NUMBER_1){
            proxyHomePageReport.setFirstProxy(proxyUser.getId());
            proxyHomePageReport.setSecondProxy(CommonConst.LONG_0);
            proxyHomePageReport.setThirdProxy(CommonConst.LONG_0);
            this.getNewThirdProxys(proxyUser,startDate,endDate,proxyHomePageReport);
            this.getNewSecondProxys(proxyUser,startDate,endDate,proxyHomePageReport);
        }
        proxyHomePageReportService.save(proxyHomePageReport);
    }
    public void chargeOrder(ProxyUser proxyUser,Date startDate,Date endDate,ProxyHomePageReport proxyHomePageReport){
        try {
            ChargeOrder chargeOrder = new ChargeOrder();
            chargeOrder.setStatus(CommonConst.NUMBER_1);
            if (CasinoProxyUtil.setParameter(chargeOrder,proxyUser)){
                return;
            }
            List<ChargeOrder> chargeOrders = chargeOrderService.findChargeOrders(chargeOrder, startDate, endDate);
            if (CasinoProxyUtil.checkNull(chargeOrders) || chargeOrders.size() == CommonConst.NUMBER_0){
                proxyHomePageReport.setChargeAmount(BigDecimal.ZERO);
                proxyHomePageReport.setChargeNums(CommonConst.NUMBER_0);
                return;
            }
            BigDecimal chargeAmount = chargeOrders.stream().map(ChargeOrder::getChargeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            proxyHomePageReport.setChargeAmount(chargeAmount);
            proxyHomePageReport.setChargeNums(chargeOrders.size());
        }catch (Exception ex){
            log.error("统计代理{}充值订单失败{}",proxyUser.getUserName(),ex);
        }
    }
    public void withdrawOrder(ProxyUser proxyUser,Date startDate,Date endDate,ProxyHomePageReport proxyHomePageReport){
        try {
            WithdrawOrder withdrawOrder = new WithdrawOrder();
            withdrawOrder.setStatus(CommonConst.NUMBER_1);
            if (CasinoProxyUtil.setParameter(withdrawOrder,proxyUser)){
                return;
            }
            List<WithdrawOrder> withdrawOrders = withdrawOrderService.findOrderList(withdrawOrder, startDate, endDate);
            if (CasinoProxyUtil.checkNull(withdrawOrders) || withdrawOrders.size() == CommonConst.NUMBER_0){
                proxyHomePageReport.setWithdrawMoney(BigDecimal.ZERO);
                proxyHomePageReport.setWithdrawNums(CommonConst.NUMBER_0);
                return;
            }
            BigDecimal withdrawMoney = withdrawOrders.stream().map(WithdrawOrder::getWithdrawMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            proxyHomePageReport.setWithdrawMoney(withdrawMoney);
            proxyHomePageReport.setWithdrawNums(withdrawOrders.size());
        }catch (Exception ex){
            log.error("统计代理{}提现订单失败{}",proxyUser.getUserName(),ex);
        }
    }
    public void gameRecord(ProxyUser proxyUser,String startTime,String endTime,ProxyHomePageReport proxyHomePageReport){
        try {
            GameRecord gameRecord = new GameRecord();
            if (CasinoProxyUtil.setParameter(gameRecord,proxyUser)){
                return;
            }
            List<GameRecord> gameRecords = gameRecordService.findGameRecords(gameRecord, startTime, endTime);
            if (CasinoProxyUtil.checkNull(gameRecord) || gameRecords.size() == CommonConst.NUMBER_0){
                proxyHomePageReport.setValidbetAmount(BigDecimal.ZERO);
                proxyHomePageReport.setWinLossAmount(BigDecimal.ZERO);
                proxyHomePageReport.setActiveUsers(CommonConst.NUMBER_0);
                return;
            }
            BigDecimal validbetAmount = BigDecimal.ZERO;
            BigDecimal winLoss = BigDecimal.ZERO;
            for (GameRecord g : gameRecords){
                validbetAmount = validbetAmount.add(new BigDecimal(g.getValidbet()));
                winLoss = winLoss.add(new BigDecimal(g.getWinLoss()));
            }
            proxyHomePageReport.setValidbetAmount(validbetAmount);
            proxyHomePageReport.setWinLossAmount(winLoss);
            gameRecords = gameRecords.stream().filter(CommonUtil.distinctByKey(GameRecord::getUser)).collect(Collectors.toList());
            proxyHomePageReport.setActiveUsers(gameRecords.size());
        }catch (Exception ex){
            log.error("统计代理{}三方游戏注单失败{}",proxyUser.getUserName(),ex);
        }
    }
    public void getNewUsers(ProxyUser proxyUser,Date startDate,Date endDate,ProxyHomePageReport proxyHomePageReport){
        try {
            User user = new User();
            if (CasinoProxyUtil.setParameter(user,proxyUser)){
                return;
            }
            List<User> userList = userService.findUserList(user, startDate, endDate);
            proxyHomePageReport.setNewUsers(userList==null ? CommonConst.NUMBER_0 : userList.size());
        }catch (Exception ex){
            log.error("统计代理{}新增用户失败{}",proxyUser.getUserName(),ex);
        }
    }
    public void getNewThirdProxys(ProxyUser proxyUser,Date startDate,Date endDate,ProxyHomePageReport proxyHomePageReport){
        try {
            ProxyUser proxy = new ProxyUser();
            proxy.setProxyRole(CommonConst.NUMBER_3);
            if (CasinoProxyUtil.setParameter(proxy,proxyUser)){
                return;
            }
            List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser,startDate,endDate);
            proxyHomePageReport.setNewThirdProxys(proxyUserList==null ? CommonConst.NUMBER_0 : proxyUserList.size());
        }catch (Exception ex){
            log.error("统计代理{}新增基层代理失败{}",proxyUser.getUserName(),ex);
        }
    }
    public void getNewSecondProxys(ProxyUser proxyUser,Date startDate,Date endDate,ProxyHomePageReport proxyHomePageReport){
        try {
            ProxyUser proxy = new ProxyUser();
            proxy.setProxyRole(CommonConst.NUMBER_2);
            if (CasinoProxyUtil.setParameter(proxy,proxyUser)){
                return;
            }
            List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser,startDate,endDate);
            proxyHomePageReport.setNewSecondProxys(proxyUserList==null ? CommonConst.NUMBER_0 : proxyUserList.size());
        }catch (Exception ex){
            log.error("统计代理{}新增区域代理失败{}",proxyUser.getUserName(),ex);
        }
    }
    public void proxyAmount(ProxyUser proxyUser,Date startDate,Date endDate,ProxyHomePageReport proxyHomePageReport){
        try {
            proxyHomePageReport.setGroupTotalprofit(BigDecimal.ZERO);
            proxyHomePageReport.setTotalprofit(BigDecimal.ZERO);
        }catch (Exception ex){
            log.error("统计代理{}充值订单失败{}",proxyUser.getUserName(),ex);
        }
    }
}
