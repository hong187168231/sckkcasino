package com.qianyi.casinoadmin.task;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.GameRecordReport;
import com.qianyi.casinocore.service.GameRecordReportService;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.TaskConst;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class GameRecordTaskTest4 {
    public final static String start = ":00:00";

    public final static String end = ":59:59";

    @Autowired
    private GameRecordReportService gameRecordReportService;

    @Autowired
    private GameRecordService gameRecordService;

    private static List<String> list = new LinkedList<>();
    static {
        list.add(" 00");
        list.add(" 01");
        list.add(" 02");
        list.add(" 03");
        list.add(" 04");
        list.add(" 05");
        list.add(" 06");
        list.add(" 07");
        list.add(" 08");
        list.add(" 09");
        list.add(" 10");
        list.add(" 11");
        list.add(" 12");
        list.add(" 13");
        list.add(" 14");
        list.add(" 15");
        list.add(" 16");
        list.add(" 17");
        list.add(" 18");
        list.add(" 19");
        list.add(" 20");
        list.add(" 21");
        list.add(" 22");
        list.add(" 23");
    }
    @Scheduled(cron = TaskConst.GAMERECORD_TASK)
    public void create() throws ParseException {
        log.info("每小时报表统计开始start=============================================》");
        Date startDate = DateUtil.getSimpleDateFormat1().parse("2021-11-01");
        Date endDate = DateUtil.getSimpleDateFormat1().parse("2022-01-14");
        Map<Integer,String> mapDate = this.findDates("D", startDate, endDate);
//        Calendar nowTime = Calendar.getInstance();
//        nowTime.add(Calendar.HOUR, -1);
//        String format = DateUtil.getSimpleDateFormat2().format(nowTime.getTime());
        mapDate.forEach((k,v)->{
            String s = v;
            list.forEach(str->{
                String format = s+str;
                List<GameRecordReport> byStaticsTimes = gameRecordReportService.findByStaticsTimes(format);
                if (!LoginUtil.checkNull(byStaticsTimes) && byStaticsTimes.size() > CommonConst.NUMBER_0) {
                    byStaticsTimes.clear();
                    return;
                }
                String startTime = format + start;
                String endTime = format + end;
                List<Map<String, Object>> companResult = gameRecordService.queryGameRecords(startTime, endTime);
                this.create(format,companResult);
            });
        });

        log.info("每小时报表统计结束end=============================================》");
    }
    private void create(String format,List<Map<String, Object>> reportResult){
        try {
            if (LoginUtil.checkNull(reportResult) || reportResult.size() == CommonConst.NUMBER_0){
                return;
            }
            if (LoginUtil.checkNull(reportResult.get(0).get("num")) || Integer.parseInt(reportResult.get(0).get("num").toString()) == CommonConst.NUMBER_0){
                return;
            }
            for (Map<String, Object> map:reportResult){
                GameRecordReport gameRecordReport = new GameRecordReport();
                gameRecordReport.setStaticsTimes(format);
                gameRecordReport.setAmount(new BigDecimal(map.get("amount").toString()));
                gameRecordReport.setBetAmount(new BigDecimal(map.get("bet").toString()));
                gameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                gameRecordReport.setWinLossAmount(new BigDecimal(map.get("win_loss").toString()));
                gameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                gameRecordReport.setGid(Integer.parseInt(map.get("gid").toString()));
                gameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
                gameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
                gameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
                gameRecordReportService.save(gameRecordReport);
            }
        }catch (Exception ex){
            log.error("每小时报表统计失败",ex);
        }
    }

    public static Map<Integer,String> findDates(String dateType, Date dBegin, Date dEnd){
        Map<Integer,String> mapDate = new HashMap<>();
        Calendar calBegin = Calendar.getInstance();
        calBegin.setTime(dBegin);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(dEnd);
        Integer count = CommonConst.NUMBER_0;
        while (calEnd.after(calBegin)) {
            count++;
            if (calEnd.after(calBegin))
                mapDate.put(count,new SimpleDateFormat("yyyy-MM-dd").format(calBegin.getTime()));
            else
                mapDate.put(count,new SimpleDateFormat("yyyy-MM-dd").format(calBegin.getTime()));
            switch (dateType) {
                case "M":
                    calBegin.add(Calendar.MONTH, 1);
                    break;
                case "D":
                    calBegin.add(Calendar.DAY_OF_YEAR, 1);break;
                case "H":
                    calBegin.add(Calendar.HOUR, 1);break;
                case "N":
                    calBegin.add(Calendar.SECOND, 1);break;
            }
        }
        return mapDate;
    }
}
