package com.qianyi.casinoreport.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class CompanyProxyTask {

    //每天凌晨05分进行启动
    @Scheduled(cron = "0 5 * * * ?")
    public void processTask(){
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dayTime = df.format(LocalDateTime.now());
        dayTime = dayTime.substring(0,10);
        log.info(dayTime);
    }
}
