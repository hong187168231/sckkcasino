package com.qianyi.casinoadmin.task;

import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.ShareProfitChange;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.casinocore.service.ShareProfitChangeService;
import com.qianyi.casinocore.service.UserRunningWaterService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.TaskConst;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UserRunningWaterTask {
    public final static String start = ":00:00";

    public final static String end = ":59:59";

    public final static Integer startHour = 0;

    public final static Integer endHour = 23;

    @Autowired
    private UserRunningWaterService userRunningWaterService;

    @Autowired
    private ShareProfitChangeService shareProfitChangeService;

    @Autowired
    private GameRecordService gameRecordService;

    @Scheduled(cron = TaskConst.USER_RUNNING_WATER)
    public void create(){
        log.info("每日会员流水报表统计开始start=============================================》");
        Calendar nowTime = Calendar.getInstance();
        nowTime.add(Calendar.DATE, -1);
        String format = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        try {
            for (int i = startHour;i <= endHour;i++){
                String s = i < CommonConst.NUMBER_10? " 0"+i:" "+i;
                String startTime = format + s + start;
                String endTime = format + s + end;
                System.out.println(startTime);
                System.out.println(endTime);
                Date startDate = DateUtil.getSimpleDateFormat().parse(startTime);
                Date endDate = DateUtil.getSimpleDateFormat().parse(endTime);
                this.gameRecord(startTime,endTime,format);
                this.shareProfitChange(format,startDate,endDate);
            }
            log.info("每日会员流水报表统计结束end=============================================》");
        }catch (Exception ex){
            log.error("每日会员流水报表统计失败",ex);
        }
    }

    public void gameRecord(String startTime, String endTime, String format){
        try {
            GameRecord gameRecord = new GameRecord();
            List<GameRecord> gameRecords = gameRecordService.findGameRecords(gameRecord, startTime, endTime);
            if (gameRecord  == null || gameRecords.size() == CommonConst.NUMBER_0){
                return;
            }
            Map<Long, List<GameRecord>> userIdMap = gameRecords.stream().collect(Collectors.groupingBy(GameRecord::getUserId));
            userIdMap.forEach((key,value)->{
                List<GameRecord> valueList = value;
                BigDecimal validbetAmount = BigDecimal.ZERO;
                for (GameRecord g : valueList){
                    validbetAmount = validbetAmount.add(new BigDecimal(g.getValidbet()));
                }
                userRunningWaterService.updateKey(key,format,validbetAmount,BigDecimal.ZERO);
            });
        }catch (Exception ex){
            log.error("用户流水统计三方游戏注单失败{}",ex);
        }
    }
    public void shareProfitChange(String format,Date startDate,Date endDate){
        try {
            List<ShareProfitChange> shareProfitChanges = shareProfitChangeService.findAll(null,null, startDate, endDate);
            if (shareProfitChanges  == null || shareProfitChanges.size() == CommonConst.NUMBER_0){
                return;
            }
            Map<Long, List<ShareProfitChange>> fromUserIdMap = shareProfitChanges.stream().collect(Collectors.groupingBy(ShareProfitChange::getFromUserId));
            fromUserIdMap.forEach((key,value)->{
                List<ShareProfitChange> valueList = value;
                BigDecimal contribution = valueList.stream().map(ShareProfitChange::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                userRunningWaterService.updateKey(key,format,BigDecimal.ZERO,contribution);
            });
        }catch (Exception ex){
            log.error("用户流水统计人人代佣金失败{}",ex);
        }
    }
}