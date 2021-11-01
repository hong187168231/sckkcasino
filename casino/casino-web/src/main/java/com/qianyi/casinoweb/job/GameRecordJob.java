package com.qianyi.casinoweb.job;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.livewm.api.PublicWMApi;
import com.qianyi.modulecommon.Constants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

@Component
@Slf4j
public class GameRecordJob {

    @Qualifier("asyncExecutor")
    @Autowired
    private Executor executor;

    @Autowired
    PublicWMApi wmApi;
    @Autowired
    GameRecordService gameRecordService;
    @Autowired
    GameRecordEndTimeService gameRecordEndTimeService;
    @Autowired
    UserThirdService userThirdService;
    @Autowired
    UserService userService;
    @Autowired
    PlatformConfigService platformConfigService;
    @Autowired
    GameRecordAsyncOper gameRecordAsyncOper;

    //每隔5分钟执行一次
    @Scheduled(cron = "0 0/5 * * * ?")
    public void testTasks() {
        try {
            log.info("开始拉取wm游戏记录");
            GameRecordEndTime gameRecord = gameRecordEndTimeService.findFirstByEndTimeDesc();
            String time = gameRecord == null ? null : gameRecord.getEndTime();
            //获取查询游戏记录的时间范围
            StartTimeAndEndTime startTimeAndEndTime = getStartTimeAndEndTime(time);
            String startTime = startTimeAndEndTime.getStartTime();
            String endTime = startTimeAndEndTime.getEndTime();
            //查询时间范围内的所有游戏记录，（以结算时间为条件）
            String result = wmApi.getDateTimeReport(null, startTime, endTime, 0, 1, 2, null, null);
            //远程请求异常
            if (ObjectUtils.isEmpty(result)) {
                return;
            }
            //查询结果无记录
            if ("notData".equals(result)) {
                updateEndTime(endTime, gameRecord);
                return;
            }
            List<GameRecord> gameRecords = JSON.parseArray(result, GameRecord.class);
            if (!CollectionUtils.isEmpty(gameRecords)) {
                saveAll(gameRecords);
            }
            updateEndTime(endTime, gameRecord);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取拉取游戏的开始时间和结束时间
     *
     * @param time
     * @return
     * @throws ParseException
     */
    private StartTimeAndEndTime getStartTimeAndEndTime(String time) throws ParseException {
        String startTime = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date nowDate = new Date();
        String endTime = format.format(nowDate);
        //第一次拉取数据取当前时间前5分钟为开始时间，之后以上次拉取数据的结束时间为开始时间
        if (ObjectUtils.isEmpty(time)) {
            Date date = format.parse(endTime);
            Calendar now = Calendar.getInstance();
            now.setTime(date);
            now.add(Calendar.MINUTE, -5);
            Date afterFiveMin = now.getTime();
            startTime = format.format(afterFiveMin);
        } else {
            startTime = time;
        }
        Date startDate = format.parse(startTime);
        long startTimeNum = startDate.getTime();
        long endTimeNum = nowDate.getTime();
        //第三方要求拉取数据时间范围不能大于1天，大于1天，取开始时间的后一天为结束时间
        if ((endTimeNum - startTimeNum) > 60 * 60 * 24) {
            Calendar now = Calendar.getInstance();
            now.setTime(startDate);
            now.add(Calendar.DAY_OF_MONTH, 1);
            Date afterFiveMin = now.getTime();
            endTime = format.format(afterFiveMin);
        }
        StartTimeAndEndTime startTimeAndEndTime = new StartTimeAndEndTime();
        startTimeAndEndTime.setStartTime(startTime);
        startTimeAndEndTime.setEndTime(endTime);
        return startTimeAndEndTime;
    }

    /**
     * 更新游戏表结束时间
     *
     * @param endTime
     */
    public void updateEndTime(String endTime, GameRecordEndTime gameRecordEndTime) {
        if (gameRecordEndTime == null) {
            gameRecordEndTime = new GameRecordEndTime();
        }
        gameRecordEndTime.setEndTime(endTime);
        gameRecordEndTimeService.save(gameRecordEndTime);
    }

    public void saveAll(List<GameRecord> gameRecordList) {
        log.info("开始处理游戏记录数据");
        //查询最小清0打码量
        PlatformConfig platformConfig = platformConfigService.findFirst();
        for (GameRecord gameRecord : gameRecordList) {
            try {
                UserThird account = userThirdService.findByAccount(gameRecord.getUser());
                if (account == null || account.getUserId() == null) {
                    log.error("同步游戏记录时，UserThird查询结果为null,account={}", gameRecord.getUser());
                    continue;
                }
                gameRecord.setUserId(account.getUserId());
                BigDecimal validbet = ObjectUtils.isEmpty(gameRecord.getValidbet()) ? BigDecimal.ZERO : new BigDecimal(gameRecord.getValidbet());
                //有效投注额为0不参与洗码,打码,分润
                if (validbet.compareTo(BigDecimal.ZERO) == 0) {
                    gameRecord.setWashCodeStatus(Constants.yes);
                    gameRecord.setCodeNumStatus(Constants.yes);
                    gameRecord.setShareProfitStatus(Constants.yes);
                }
                //有数据会重复注单id唯一约束会报错，所以一条一条保存，避免影响后面的
                GameRecord record = save(gameRecord);
                if (validbet.compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }
                //洗码
                gameRecordAsyncOper.washCode(Constants.PLATFORM, record);
                //扣减打码量
                gameRecordAsyncOper.subCodeNum(platformConfig, record);
                //代理分润
                gameRecordAsyncOper.shareProfit(record);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("保存游戏记录时报错,message={}", e.getMessage());
            }
        }
    }

    public GameRecord save(GameRecord gameRecord) throws Exception{
        User user = userService.findById(gameRecord.getUserId());
        if (user != null) {
            gameRecord.setFirstProxy(user.getFirstProxy());
            gameRecord.setSecondProxy(user.getSecondProxy());
            gameRecord.setThirdProxy(user.getThirdProxy());
        }
        GameRecord record = gameRecordService.save(gameRecord);
        return record;
    }

    @Data
    class StartTimeAndEndTime {
        private String startTime;
        private String endTime;
    }
}
