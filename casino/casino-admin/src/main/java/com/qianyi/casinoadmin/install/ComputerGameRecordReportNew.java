package com.qianyi.casinoadmin.install;

import com.qianyi.casinocore.service.GameRecordReportNewService;
import com.qianyi.casinocore.util.CommonUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Component
@Slf4j
@Order(10)
public class ComputerGameRecordReportNew implements CommandLineRunner {

    public final static String start = " 12:00:00";

    public final static String end = " 11:59:59";

    public final static String staticsTimesEnd = " 12";

    @Autowired
    private GameRecordReportNewService gameRecordReportService;

    @Override
    public void run(String... args) throws Exception {
        log.info("初始化代理游戏报表开始==============================================>");
        long startTime = System.currentTimeMillis();
//        Date startDate = null;
//        try {
//            startDate = DateUtil.getDate("2021-11-01");
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        Map<Integer, String> mapDate = CommonUtil.findDates("D", startDate, new Date());
//        mapDate.forEach((k,v)->{
//            try {
//                String today = v;
//                Calendar calendar = Calendar.getInstance();
//                Date todayDate = DateUtil.getSimpleDateFormat1().parse(today);
//                calendar.setTime(todayDate);
//                calendar.add(Calendar.DATE, 1);
//                String tomorrow = DateUtil.dateToPatten1(calendar.getTime());
//                gameRecordReportService.statisticsWashCode(Constants.PLATFORM_WM_BIG, Constants.PLATFORM_WM,today+staticsTimesEnd,today+start,tomorrow+end);
//                gameRecordReportService.statisticsWashCode(Constants.PLATFORM_PG, Constants.PLATFORM_PG,today+staticsTimesEnd,today+start,tomorrow+end);
//                gameRecordReportService.statisticsWashCode(Constants.PLATFORM_CQ9, Constants.PLATFORM_CQ9,today+staticsTimesEnd,today+start,tomorrow+end);
//                gameRecordReportService.statisticsWashCode(Constants.PLATFORM_OBDJ, Constants.PLATFORM_OBDJ,today+staticsTimesEnd,today+start,tomorrow+end);
//                gameRecordReportService.statisticsWashCode(Constants.PLATFORM_OBTY, Constants.PLATFORM_OBTY,today+staticsTimesEnd,today+start,tomorrow+end);
//                gameRecordReportService.statisticsWashCode(Constants.PLATFORM_SABASPORT, Constants.PLATFORM_SABASPORT,today+staticsTimesEnd,today+start,tomorrow+end);
//                gameRecordReportService.statisticsWashCode(Constants.PLATFORM_AE, Constants.PLATFORM_AE,today+staticsTimesEnd,today+start,tomorrow+end);
//            } catch (ParseException e) {
//                log.error("初始化代理游戏报表失败{}",v);
//            }
//
//        });
        log.info("初始化代理游戏报表结束耗时{}==============================================>",
            System.currentTimeMillis() - startTime);
    }
}
