package com.qianyi.casinoweb.job;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.business.UserCodeNumBusiness;
import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.GameRecordEndTime;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.service.GameRecordEndTimeService;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.service.UserThirdService;
import com.qianyi.livewm.api.PublicWMApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//@Component
public class GameRecordJob {

    // 创建线程池
    ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 100, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));


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
    UserCodeNumBusiness subCodeNumBusiness;

    //每隔5分钟执行一次
    @Scheduled(fixedRate = 1000 * 60 * 5)
    public void testTasks() {
        try {
            GameRecordEndTime gameRecord = gameRecordEndTimeService.findFirstByEndTimeDesc();
            String startTime = null;
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            String endTime = format.format(new Date());
            if (gameRecord == null) {
                Date date = format.parse(endTime);
                Calendar now = Calendar.getInstance();
                now.setTime(date);
                now.add(Calendar.MINUTE, -5);
                Date afterFiveMin = now.getTime();
                startTime = format.format(afterFiveMin);
            } else {
                startTime = gameRecord.getEndTime();
            }
            String result = wmApi.getDateTimeReport(null, startTime, endTime, 0, 0, 2, null, null);
            if (ObjectUtils.isEmpty(result)) {
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
        //把大集成拆分成5个
        List<List<GameRecord>> lists = averageAssign(gameRecordList, 5);
        // 创建异步执行任务:
        for(List<GameRecord> list:lists){
            if (CollectionUtils.isEmpty(list)){
                continue;
            }
            CompletableFuture cf = CompletableFuture.runAsync(()->{
                for (GameRecord gameRecord : list) {
                    try {
                        //有数据会重复注单id唯一约束会报错，所以一条一条保存，避免影响后面的
                        gameRecordService.save(gameRecord);
                        //游戏记录保存成功后扣减打码量
                        UserThird account = userThirdService.findByAccount(gameRecord.getUser());
                        if (account == null) {
                            continue;
                        }
                        BigDecimal validbet = new BigDecimal(gameRecord.getValidbet());
                        subCodeNumBusiness.subCodeNum(validbet, account.getUserId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            },executor);
        }
    }

    /**
     * 将一组数据平均分成n组
     * @param source 要分组的数据源
     * @param n      平均分成n组
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> averageAssign(List<T> source, int n) {
        List<List<T>> result = new ArrayList<List<T>>();
        int remainder = source.size() % n;  //(先计算出余数)
        int number = source.size() / n;  //然后是商
        int offset = 0;//偏移量
        for (int i = 0; i < n; i++) {
            List<T> value = null;
            if (remainder > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remainder--;
                offset++;
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            result.add(value);
        }
        return result;
    }
}
