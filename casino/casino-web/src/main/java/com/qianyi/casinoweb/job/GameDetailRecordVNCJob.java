package com.qianyi.casinoweb.job;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.AdGamesService;
import com.qianyi.casinocore.service.GameDetailVncEndTimeService;
import com.qianyi.casinocore.service.PlatformGameService;
import com.qianyi.casinocore.service.RptBetInfoDetailService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.lottery.api.PublicLotteryApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 查询最近数据，最大两个月前的注单详情数据
 * 一次查询最大范围30分钟
 */
@Component
@Slf4j
public class GameDetailRecordVNCJob {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private RptBetInfoDetailService rptBetInfoDetailService;

    @Autowired
    private PlatformGameService platformGameService;

    @Autowired
    private AdGamesService adGamesService;

    @Autowired
    private GameDetailVncEndTimeService gameDetailVncEndTimeService;

    @Autowired
    private PublicLotteryApi lotteryApi;

    //每隔5分钟30秒执行一次
    @Scheduled(cron = "50 0/2 * * * ?")
    public void pullGameRecord() {
        //日志打印traceId，同一次请求的traceId相同，方便定位日志
        ThreadContext.put("traceId", UUID.randomUUID().toString().replaceAll("-",""));

        PlatformGame platformGame = platformGameService.findByGamePlatformName(Constants.PLATFORM_VNC);
        if (platformGame != null && platformGame.getGameStatus() == 2) {
            log.info("后台已关闭VNC平台,无需拉单,platformGame={}", platformGame);
            return;
        }

        AdGame adGame = adGamesService.findByGamePlatformNameAndGameCode(Constants.PLATFORM_VNC, Constants.PLATFORM_VNC);
        if (adGame != null && adGame.getGamesStatus() == 2) {
            log.info("后台已关闭VNC,无需拉单,adGame={}", adGame);
        } else {
            log.info("定时器开始拉取VNC注单记录");
            GameDetailVncEndTime gameRecordVNCEndTime = gameDetailVncEndTimeService.findFirstByPlatformAndStatusOrderByEndTimeDesc(Constants.PLATFORM_VNC, Constants.yes);
            String time = gameRecordVNCEndTime == null ? null : gameRecordVNCEndTime.getEndTime();
            //获取查询游戏记录的时间范围
            String startTime = getStartTime(time, Constants.PLATFORM_VNC);
            pullGameRecord(startTime, Constants.PLATFORM_VNC);
            log.info("定时器拉取完成VNC注单记录");
        }

    }

    public void pullGameRecord(String startTime, String platform) {
        try {
            if (ObjectUtils.isEmpty(startTime)) {
                return;
            }
            log.info("VNC注单详情开始拉取{},{}到当前时间的游戏记录", platform, startTime);
            pullGameRecordByTime(startTime, platform);
            log.info("{},{}到当前时间的记录VNC注单详情拉取完成", platform, startTime);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("{},{}到当前时间的记录拉取异常", platform, startTime);
        }
    }

    private void pullGameRecordByTime(String startTime, String platform) throws Exception {
        String endTime = setEndTime(startTime);

        String strResult = lotteryApi.getDateTimeDetailReport(startTime, endTime, "KK");
        if (CasinoWebUtil.checkNull(strResult)) {
            log.error("拉取{}注单时远程请求错误,startTime={}", platform, startTime);
            saveGameRecordVNCEndTime(startTime, endTime, platform, Constants.no);
            return;
        }
        PublicLotteryApi.ResponseEntity entity = lotteryApi.entity(strResult);
        if (!"0".equals(entity.getErrorCode())) {
            log.error("拉取{}注单时报错,startTime={},result={}", platform, startTime, strResult);
            saveGameRecordVNCEndTime(startTime, endTime, platform, Constants.no);
            return;
        }
        String transactions = entity.getData();

        if (StringUtils.equals("notData", transactions) || StringUtils.isBlank(transactions)) {
            log.info("拉取{}注单当前时间无记录,startTime={},result={}", platform, startTime, transactions);
            saveGameRecordVNCEndTime(startTime, endTime, platform, Constants.yes);
            return;
        }
        List<RptBetInfoDetail> gameRecords = JSON.parseArray(transactions, RptBetInfoDetail.class);
        gameRecords = gameRecords.stream().filter(ga -> ga.getSettleState() == true && ga.getIsCanceled() == false).collect(Collectors.toList());
        if (gameRecords.isEmpty()) {
            log.info("拉取{}注单当前时间无记录,startTime={},result={}", platform, startTime, transactions);
            saveGameRecordVNCEndTime(startTime, endTime, platform, Constants.yes);
            return;
        }
        //保存数据
        saveAll(platform, gameRecords);
    }

    public void saveAll(String platform, List<RptBetInfoDetail> rptBetInfoDetailList) {
        if (CollectionUtils.isEmpty(rptBetInfoDetailList)) {
            return;
        }
        log.info("开始处理{}游戏记录数据", platform);
        //查询最小清0打码量

        for (RptBetInfoDetail rptBetInfoDetail : rptBetInfoDetailList) {
            try {
                save(rptBetInfoDetail);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("保存{}游戏记录时报错,message={}", platform,e.getMessage());
            }
        }
    }

    private void save(RptBetInfoDetail rptBetInfoDetail) {

        rptBetInfoDetailService.save(rptBetInfoDetail);
    }


    private void saveGameRecordVNCEndTime(String startTime, String endTime, String platform, Integer status) {
        GameDetailVncEndTime gameRecordVNCEndTime = new GameDetailVncEndTime();
        gameRecordVNCEndTime.setStartTime(startTime);
        gameRecordVNCEndTime.setEndTime(endTime);
        gameRecordVNCEndTime.setStatus(status);
        gameRecordVNCEndTime.setPlatform(platform);
        gameDetailVncEndTimeService.save(gameRecordVNCEndTime);
    }

    @SneakyThrows
    private String setEndTime(String startTime) {
        Date startDateTime = sdf.parse(startTime);
        Date endTime = DateUtil.addMinuteDate(startDateTime, 30);
        if(endTime.getTime() < new Date().getTime()){
            return sdf.format(endTime);
        }
        return sdf.format(new Date());
    }

    @SneakyThrows
    public String getStartTime(String startTime, String platform) {
        Date endTime = new Date();
//        //第一次拉取数据取当前时间前10分钟为开始时间，之后以上次拉取数据的结束时间为开始时间
        if (ObjectUtils.isEmpty(startTime)) {
            startTime = getBeforeTime(endTime, -10);
        }
        //时间范围重叠两分钟
        Date startDateTime = sdf.parse(startTime);
        startTime = getBeforeTime(startDateTime, -2);
        return startTime;
    }

    public static String getBeforeTime(Date date, int num) {
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        now.add(Calendar.MINUTE, num);
        Date before = now.getTime();
        return sdf.format(before);
    }


}
