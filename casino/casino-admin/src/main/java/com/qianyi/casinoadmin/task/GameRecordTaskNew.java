package com.qianyi.casinoadmin.task;

import com.qianyi.casinocore.service.GameRecordReportNewService;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.TaskConst;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Calendar;

@Slf4j
@Component
public class GameRecordTaskNew {

    @Autowired
    private GameRecordReportNewService gameRecordReportService;

    public final static String start = " 12:00:00";

    public final static String end = " 11:59:59";

    public final static String staticsTimesEnd = " 12";

    @Autowired
    private GameRecordService gameRecordService;

    @Scheduled(cron = TaskConst.GAMERECORD_TASK_NEW)
    public void create(){
        log.info("每小时报表统计开始start=============================================》");
        gameRecordReportService.saveGameRecordReportWM();
        gameRecordReportService.saveGameRecordReportPG();
        gameRecordReportService.saveGameRecordReportCQ9();
        gameRecordReportService.saveGameRecordReportOBDJ();
        gameRecordReportService.saveGameRecordReportOBTY();
        gameRecordReportService.saveGameRecordReportSABASPORT();
        gameRecordReportService.saveGameRecordReportAE();
        this.statisticsWashCode();
        log.info("每小时报表统计结束end=============================================》");
    }

    private void statisticsWashCode(){
        try {
            Calendar nowTime = Calendar.getInstance();
            int hour = nowTime.get(Calendar.HOUR_OF_DAY);
            String today = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
            nowTime.add(Calendar.DATE, -1);
            String yesterday = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
            this.statisticsWashCode(yesterday+staticsTimesEnd,yesterday+start,today+end);
            if (hour >= CommonConst.NUMBER_12) {
                nowTime.add(Calendar.DATE, 2);
                String tomorrow = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
                this.statisticsWashCode(today+staticsTimesEnd,today+start,tomorrow+end);
            }
        }catch (Exception ex){
            log.error("每日小时报表统计,统计洗码失败=============================================》",ex);
        }
    }

    private void statisticsWashCode(String staticsTimes,String startTime,String endTime){
        gameRecordReportService.statisticsWashCode(Constants.PLATFORM_WM_BIG,Constants.PLATFORM_WM,staticsTimes,startTime,endTime);
        gameRecordReportService.statisticsWashCode(Constants.PLATFORM_PG,Constants.PLATFORM_PG,staticsTimes,startTime,endTime);
        gameRecordReportService.statisticsWashCode(Constants.PLATFORM_CQ9,Constants.PLATFORM_CQ9,staticsTimes,startTime,endTime);
        gameRecordReportService.statisticsWashCode(Constants.PLATFORM_OBDJ,Constants.PLATFORM_OBDJ,staticsTimes,startTime,endTime);
        gameRecordReportService.statisticsWashCode(Constants.PLATFORM_OBTY,Constants.PLATFORM_OBTY,staticsTimes,startTime,endTime);
        gameRecordReportService.statisticsWashCode(Constants.PLATFORM_SABASPORT,Constants.PLATFORM_SABASPORT,staticsTimes,startTime,endTime);
        gameRecordReportService.statisticsWashCode(Constants.PLATFORM_AE,Constants.PLATFORM_AE,staticsTimes,startTime,endTime);
    }
}
