package com.qianyi.casinoreport.task;

import com.qianyi.casinoreport.business.company.CompanyProxyDailyBusiness;
import com.qianyi.casinoreport.business.company.CompanyProxyMonthBusiness;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class CompanyProxyTask {

    @Autowired
    private CompanyProxyDailyBusiness companyProxyDailyBusiness;

    @Autowired
    private CompanyProxyMonthBusiness companyProxyMonthBusiness;

    //每天凌晨05分进行启动
    @Scheduled(cron = "0 5 0 * * ?")
    public void processTask(){
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dayTime = df.format(LocalDateTime.now());
        dayTime = dayTime.substring(0,10);
        log.info(dayTime);
        companyProxyDailyBusiness.processDailyReport(dayTime);
    }

    //每天凌晨05分进行启动
    @Scheduled(cron = "0/30 * * * * ?")
    public void processMonthTask(){
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dayTime = df.format(LocalDateTime.now());
        dayTime = dayTime.substring(0,10);
        log.info(dayTime);
        companyProxyMonthBusiness.processMonthReport(dayTime);
    }
}
