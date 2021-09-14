package com.qianyi.casinoweb.job;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.livewm.api.PublicWMApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class GameRecordJob {

    @Autowired
    PublicWMApi wmApi;
    @Autowired
    GameRecordService gameRecordService;


    //每隔5分钟执行一次
    @Scheduled(fixedRate = 1000 * 60 * 5)
    public void testTasks() {
        try {
            String startTime = gameRecordService.findEndTime();
            if (ObjectUtils.isEmpty(startTime)) {
                startTime = "20210914000000";
            }
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            Date date = format.parse(startTime);
            Calendar now = Calendar.getInstance();
            now.setTime(date);
            now.add(Calendar.MINUTE, 5);
            Date afterFiveMin = now.getTime();
            String endTime = format.format(afterFiveMin);
            String result = wmApi.getDateTimeReport(null, startTime, null, 0, 0, 2, null, null);
            if (ObjectUtils.isEmpty(result)) {
                return;
            }
            List<GameRecord> gameRecords = JSON.parseArray(result, GameRecord.class);
            if (!CollectionUtils.isEmpty(gameRecords)) {
                gameRecords.forEach(d -> {
                    d.setEndTime(endTime);
                    d.setCreateTime(new Date());
                });
                gameRecordService.saveAll(gameRecords);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
