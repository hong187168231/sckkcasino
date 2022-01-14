package com.qianyi.casinoadmin.task;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.GameRecordReport;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.service.GameRecordReportService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.ReportService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.TaskConst;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Slf4j
//@Component
public class GameRecordTask {
    public final static String start = ":00:00";

    public final static String end = ":59:59";

    @Autowired
    private GameRecordReportService gameRecordReportService;

    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private ReportService reportService;

    @Scheduled(cron = TaskConst.GAMERECORD_TASK)
    public void create(){
        log.info("每小时报表统计开始start=============================================》");
        Calendar nowTime = Calendar.getInstance();
        nowTime.add(Calendar.HOUR, -1);
        String format = DateUtil.getSimpleDateFormat2().format(nowTime.getTime());
        List<GameRecordReport> byStaticsTimes = gameRecordReportService.findByStaticsTimes(format);
        if (!LoginUtil.checkNull(byStaticsTimes) && byStaticsTimes.size() > CommonConst.NUMBER_0) {
            byStaticsTimes.clear();
            return;
        }
        String startTime = format + start;
        String endTime = format + end;
        //统计公司
        Map<String,Object> companResult = reportService.queryReportByCompany(startTime,endTime);
        this.create(CommonConst.LONG_0,CommonConst.LONG_0,CommonConst.LONG_0,format,companResult);
        //统计代理
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        proxyUser.setProxyRole(CommonConst.NUMBER_3);
        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
        if (LoginUtil.checkNull(proxyUserList) || proxyUserList.size() == CommonConst.NUMBER_0){
            return;
        }
        proxyUserList.forEach(proxy -> {
            Map<String,Object> reportResult = reportService.queryReportByThird(proxyUser.getId(),startTime,endTime);
            this.create(proxy.getFirstProxy(),proxy.getSecondProxy(),proxy.getId(),format,reportResult);
        });
        proxyUserList.clear();
        log.info("每小时报表统计结束end=============================================》");
    }
    private void create(Long firstProxy,Long secondProxy,Long thirdProxy,String format,Map<String,Object> reportResult){
        try {
            if (LoginUtil.checkNull(reportResult) || reportResult.size() == CommonConst.NUMBER_0){
                return;
            }
            if (LoginUtil.checkNull(reportResult.get("num")) || Integer.parseInt(reportResult.get("num").toString()) == CommonConst.NUMBER_0){
                return;
            }
            GameRecordReport gameRecordReport = new GameRecordReport();
            gameRecordReport.setStaticsTimes(format);
            gameRecordReport.setAmount(new BigDecimal(reportResult.get("wash_amount").toString()));
            gameRecordReport.setBetAmount(new BigDecimal(reportResult.get("bet_amount").toString()));
            gameRecordReport.setValidAmount(new BigDecimal(reportResult.get("validbet").toString()));
            gameRecordReport.setWinLossAmount(new BigDecimal(reportResult.get("win_loss").toString()));
            gameRecordReport.setBettingNumber(Integer.parseInt(reportResult.get("num").toString()));
            gameRecordReport.setFirstProxy(firstProxy);
            gameRecordReport.setSecondProxy(secondProxy);
            gameRecordReport.setThirdProxy(thirdProxy);
            gameRecordReportService.save(gameRecordReport);
        }catch (Exception ex){
            log.error("每小时报表统计失败",ex);
        }
    }
}
