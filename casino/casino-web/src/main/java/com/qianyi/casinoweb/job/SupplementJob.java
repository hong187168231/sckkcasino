package com.qianyi.casinoweb.job;

import com.qianyi.casinoweb.controller.SupplementController;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SupplementJob {

    @Autowired
    private SupplementController supplementController;

    //每天晚上12点30自动补单前一天的注单数据
    @Scheduled(cron = "0 30 0 * * ?")
    public void pullGameRecord() {
        String startTime = DateUtil.getStartTime(-1);
        String endTime = DateUtil.getStartTime(0);
        String time = startTime + "~" + endTime;
        try {
            log.info("WM开始补单，time={}", time);
            supplementController.supplementByPlatformCommon(Constants.PLATFORM_WM_BIG, startTime, endTime);
            log.info("WM补单完成，time={}", time);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("定时器自动补WM单异常，time={},msg={}", time, e.getMessage());
        }
        try {
            log.info("PG开始补单，time={}", time);
            supplementController.supplementByPlatformCommon(Constants.PLATFORM_PG, startTime, endTime);
            log.info("PG开始补单，time={}", time);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("定时器自动补PG单异常，time={},msg={}", time, e.getMessage());
        }
        try {
            log.info("CQ9开始补单，time={}", time);
            supplementController.supplementByPlatformCommon(Constants.PLATFORM_CQ9, startTime, endTime);
            log.info("CQ9开始补单，time={}", time);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("定时器自动补CQ9单异常，time={},msg={}", time, e.getMessage());
        }
        try {
            log.info("SABASPORT开始补单，time={}", time);
            supplementController.supplementByPlatformCommon(Constants.PLATFORM_SABASPORT, startTime, endTime);
            log.info("SABASPORT开始补单，time={}", time);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("定时器自动补SABASPORT单异常，time={},msg={}", time, e.getMessage());
        }
        log.info("补单完成，time={}", time);
    }

}
