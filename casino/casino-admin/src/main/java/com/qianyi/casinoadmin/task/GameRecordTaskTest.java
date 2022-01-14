//package com.qianyi.casinoadmin.task;
//
//import com.qianyi.casinoadmin.util.LoginUtil;
//import com.qianyi.casinocore.model.GameRecordReport;
//import com.qianyi.casinocore.model.ProxyUser;
//import com.qianyi.casinocore.service.GameRecordReportService;
//import com.qianyi.casinocore.service.ProxyUserService;
//import com.qianyi.casinocore.service.ReportService;
//import com.qianyi.casinocore.util.CommonConst;
//import com.qianyi.casinocore.util.TaskConst;
//import com.qianyi.modulecommon.util.DateUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//@Slf4j
//@Component
//public class GameRecordTaskTest {
//    public final static String start = ":00:00";
//
//    public final static String end = ":59:59";
//
//    @Autowired
//    private GameRecordReportService gameRecordReportService;
//
//    @Autowired
//    private ProxyUserService proxyUserService;
//
//    @Autowired
//    private ReportService reportService;
//
//    private static List<String> list = new LinkedList<>();
//    static {
//        list.add(" 00");
//        list.add(" 01");
//        list.add(" 02");
//        list.add(" 03");
//        list.add(" 04");
//        list.add(" 05");
//        list.add(" 06");
//        list.add(" 07");
//        list.add(" 08");
//        list.add(" 09");
//        list.add(" 10");
//        list.add(" 11");
//        list.add(" 12");
//        list.add(" 13");
//        list.add(" 14");
//        list.add(" 15");
//        list.add(" 16");
//        list.add(" 17");
//        list.add(" 18");
//        list.add(" 19");
//        list.add(" 20");
//        list.add(" 21");
//        list.add(" 22");
//        list.add(" 23");
//    }
//    @Scheduled(cron = TaskConst.GAMERECORD_TASK)
//    public void create() throws ParseException {
//        log.info("每小时报表统计开始start=============================================》");
//        Date startDate = DateUtil.getSimpleDateFormat1().parse("2021-11-01");
//        Date endDate = DateUtil.getSimpleDateFormat1().parse("2022-01-14");
//        Map<Integer,String> mapDate = this.findDates("D", startDate, endDate);
////        Calendar nowTime = Calendar.getInstance();
////        nowTime.add(Calendar.HOUR, -1);
////        String format = DateUtil.getSimpleDateFormat2().format(nowTime.getTime());
//        mapDate.forEach((k,v)->{
//            String s = v;
//            list.forEach(str->{
//                String format = s+str;
//                List<GameRecordReport> byStaticsTimes = gameRecordReportService.findByStaticsTimes(format);
//                if (!LoginUtil.checkNull(byStaticsTimes) && byStaticsTimes.size() > CommonConst.NUMBER_0) {
//                    byStaticsTimes.clear();
//                    return;
//                }
//                String startTime = format + start;
//                String endTime = format + end;
//                //统计公司
//                Map<String,Object> companResult = reportService.queryReportByCompany(startTime,endTime);
//                this.create(CommonConst.LONG_0,CommonConst.LONG_0,CommonConst.LONG_0,format,companResult);
//                //统计代理
//                ProxyUser proxyUser = new ProxyUser();
////                proxyUser.setIsDelete(CommonConst.NUMBER_1);
//                proxyUser.setProxyRole(CommonConst.NUMBER_3);
//                List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
//                if (LoginUtil.checkNull(proxyUserList) || proxyUserList.size() == CommonConst.NUMBER_0){
//                    return;
//                }
//                proxyUserList.forEach(proxy -> {
//                    Map<String,Object> reportResult = reportService.queryReportByThird(proxy.getId(),startTime,endTime);
//                    this.create(proxy.getFirstProxy(),proxy.getSecondProxy(),proxy.getId(),format,reportResult);
//                });
//                proxyUserList.clear();
//            });
//        });
//
//        log.info("每小时报表统计结束end=============================================》");
//    }
//    private void create(Long firstProxy,Long secondProxy,Long thirdProxy,String format,Map<String,Object> reportResult){
//        try {
//            if (LoginUtil.checkNull(reportResult) || reportResult.size() == CommonConst.NUMBER_0){
//                return;
//            }
//            if (LoginUtil.checkNull(reportResult.get("num")) || Integer.parseInt(reportResult.get("num").toString()) == CommonConst.NUMBER_0){
//                return;
//            }
//            GameRecordReport gameRecordReport = new GameRecordReport();
//            gameRecordReport.setStaticsTimes(format);
//            gameRecordReport.setAmount(new BigDecimal(reportResult.get("wash_amount").toString()));
//            gameRecordReport.setBetAmount(new BigDecimal(reportResult.get("bet_amount").toString()));
//            gameRecordReport.setValidAmount(new BigDecimal(reportResult.get("validbet").toString()));
//            gameRecordReport.setWinLossAmount(new BigDecimal(reportResult.get("win_loss").toString()));
//            gameRecordReport.setBettingNumber(Integer.parseInt(reportResult.get("num").toString()));
//            gameRecordReport.setFirstProxy(firstProxy);
//            gameRecordReport.setSecondProxy(secondProxy);
//            gameRecordReport.setThirdProxy(thirdProxy);
//            gameRecordReportService.save(gameRecordReport);
//        }catch (Exception ex){
//            log.error("每小时报表统计失败",ex);
//        }
//    }
//
//    public static Map<Integer,String> findDates(String dateType, Date dBegin, Date dEnd){
//        Map<Integer,String> mapDate = new HashMap<>();
//        Calendar calBegin = Calendar.getInstance();
//        calBegin.setTime(dBegin);
//        Calendar calEnd = Calendar.getInstance();
//        calEnd.setTime(dEnd);
//        Integer count = CommonConst.NUMBER_0;
//        while (calEnd.after(calBegin)) {
//            count++;
//            if (calEnd.after(calBegin))
//                mapDate.put(count,new SimpleDateFormat("yyyy-MM-dd").format(calBegin.getTime()));
//            else
//                mapDate.put(count,new SimpleDateFormat("yyyy-MM-dd").format(calBegin.getTime()));
//            switch (dateType) {
//                case "M":
//                    calBegin.add(Calendar.MONTH, 1);
//                    break;
//                case "D":
//                    calBegin.add(Calendar.DAY_OF_YEAR, 1);break;
//                case "H":
//                    calBegin.add(Calendar.HOUR, 1);break;
//                case "N":
//                    calBegin.add(Calendar.SECOND, 1);break;
//            }
//        }
//        return mapDate;
//    }
//}
