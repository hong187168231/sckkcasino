package com.qianyi.casinoweb.job;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.model.GameRecordGoldenfEndTime;
import com.qianyi.casinocore.service.GameRecordGoldenfEndTimeService;
import com.qianyi.casinoweb.vo.GameRecordObj;
import com.qianyi.livegoldenf.api.PublicGoldenFApi;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GameRecordGoldenFJobTest {

    @Autowired
    PublicGoldenFApi publicGoldenFApi;

    @Autowired
    GameRecordGoldenFJob gameRecordGoldenFJob;

    @Autowired
    GameRecordGoldenfEndTimeService gameRecordGoldenfEndTimeService;

    @Test
    public void should_request_back() {
        PublicGoldenFApi.ResponseEntity responseEntity = publicGoldenFApi.getPlayerGameRecord(1644588300000l, 1644588600000l, "PG", 1, 1000);

        GameRecordObj gameRecordObj =  JSON.parseObject(responseEntity.getData(), GameRecordObj.class);

        log.info("{}", responseEntity);

        log.info("{}", gameRecordObj);
    }

    @Test
    public void should_request_error(){
        gameRecordGoldenFJob.pullGoldenF();
    }

    @Test
    public void should_request_data(){
        GameRecordGoldenfEndTime gameRecordGoldenfEndTime = gameRecordGoldenfEndTimeService.findFirstByVendorCodeOrderByEndTimeDesc("PG");
        log.info("{}",gameRecordGoldenfEndTime);
        if(gameRecordGoldenfEndTime == null){
            GameRecordGoldenfEndTime goldenfEndTime = new GameRecordGoldenfEndTime();
            goldenfEndTime.setEndTime(next5MinuteTime());
            goldenfEndTime.setStartTime(0l);
            goldenfEndTime.setVendorCode("PG");
//            gameRecordGoldenfEndTimeService.save(goldenfEndTime);
        }

    }

    private static long next5MinuteTime(){
        long now = System.currentTimeMillis();
        return (now - now % (1000*60*5) + (100*60*5))/1000;

    }

}