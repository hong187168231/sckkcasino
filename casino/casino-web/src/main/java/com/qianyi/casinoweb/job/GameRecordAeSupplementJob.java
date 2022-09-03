package com.qianyi.casinoweb.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.model.AdGame;
import com.qianyi.casinocore.model.PlatformGame;
import com.qianyi.casinocore.service.AdGamesService;
import com.qianyi.casinocore.service.GameRecordAeService;
import com.qianyi.casinocore.service.PlatformGameService;
import com.qianyi.casinocore.vo.GameRecordAeSummaryVo;
import com.qianyi.casinoweb.vo.GameRecordAeVo;
import com.qianyi.liveae.api.PublicAeApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * AE 对账 补单
 */
@Component
@Slf4j
public class GameRecordAeSupplementJob {

    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HHXXX");

    @Autowired
    private PublicAeApi aeApi;
    @Autowired
    private GameRecordAeService gameRecordAeService;
    @Autowired
    private AdGamesService adGamesService;
    @Autowired
    private PlatformGameService platformGameService;
    @Autowired
    private GameRecordAeJob gameRecordAeJob;

    //每小时20分钟拉取前一小时的
    @SneakyThrows
    @Scheduled(cron = "0 20 */1 * * ?")
    public void pullGameRecord() {
        PlatformGame platformGame = platformGameService.findByGamePlatformName(Constants.PLATFORM_AE);
        if (platformGame != null && platformGame.getGameStatus() == 2) {
            log.info("后台已关闭AE平台,无需对账,platformGame={}", platformGame);
            return;
        }
        Date startDate = DateUtil.beforeOrAfterHourToNowDate(new Date(), -2);
        Date endDate = DateUtil.beforeOrAfterHourToNowDate(new Date(), -1);
        String startTime = dateFormat.format(startDate);
        String endTime = dateFormat.format(endDate);
        AdGame adGame = adGamesService.findByGamePlatformNameAndGameCode(Constants.PLATFORM_AE, Constants.PLATFORM_AE_HORSEBOOK);
        if (adGame != null && adGame.getGamesStatus() == 2) {
            log.info("后台已关闭HORSEBOOK,无需对账,adGame={}", adGame);
        } else {
            log.info("定时器开始核对HORSEBOOK注单记录");
            pullGameRecord(startTime, endTime, Constants.PLATFORM_AE_HORSEBOOK);
            log.info("定时器核对完成HORSEBOOK注单记录");
        }
        Thread.sleep(2 * 1000);
        AdGame svGame = adGamesService.findByGamePlatformNameAndGameCode(Constants.PLATFORM_AE, Constants.PLATFORM_AE_SV388);
        if (svGame != null && svGame.getGamesStatus() == 2) {
            log.info("后台已关闭SV388,无需对账,adGame={}", svGame);
        } else {
            log.info("定时器开始核对SV388注单记录");
            pullGameRecord(startTime, endTime, Constants.PLATFORM_AE_SV388);
            log.info("定时器核对完成SV388注单记录");
        }
        Thread.sleep(2 * 1000);
        AdGame e1Game = adGamesService.findByGamePlatformNameAndGameCode(Constants.PLATFORM_AE, Constants.PLATFORM_AE_E1SPORT);
        if (e1Game != null && e1Game.getGamesStatus() == 2) {
            log.info("后台已关闭E1SPORT,无需对账,adGame={}", e1Game);
        } else {
            log.info("定时器开始核对E1SPORT注单记录");
            pullGameRecord(startTime, endTime, Constants.PLATFORM_AE_E1SPORT);
            log.info("定时器核对完成E1SPORT注单记录");
        }
    }

    public void pullGameRecord(String startTime, String endTime, String platform) {
        String time = startTime + "~" + endTime;
        try {
            log.info("开始核对{},{}的注单记录", platform, time);
            boolean sumDataResult = compareSumData(startTime, endTime, platform);
            log.info("注单核对结果,platform={},time={},result={}", platform, time, sumDataResult);
            //存在数据差异，补单
            if (sumDataResult) {
                Date startDateTime = dateFormat.parse(startTime);
                Date endDateTime = dateFormat.parse(endTime);
                String startTime1 = dateTimeFormat.format(startDateTime);
                String endTime1 = dateTimeFormat.format(endDateTime);
                log.info("开始补账,startTime={},endTime={},platform={}", startTime1, endTime1, platform);
                makeUp(startTime1, endTime1, platform);
                log.info("补账完成,startTime={},endTime={},platform={}", startTime1, endTime1, platform);
            }
            log.info("{},{}的注单记录核对完成", platform, time);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("{},{}的注单记录核对异常", platform, time);
        }
    }

    @SneakyThrows
    public boolean compareSumData(String startTime, String endTime, String platform) {
        JSONObject result = aeApi.getSummaryByTxTimeHour(platform, startTime, endTime, null, null);
        if (result == null) {
            log.error("取得区间内交易摘要时远程请求错误,startTime={},endTime={},platform={}", startTime, endTime, platform);
            return false;
        }
        String status = result.getString("status");
        if (!PublicAeApi.SUCCESS_CODE.equals(status)) {
            log.error("取得区间内交易摘要时错误,startTime={},endTime={},platform={},result={}", startTime, endTime, platform, result);
            return false;
        }
        String transactions = result.getString("transactions");
        List<GameRecordAeSummaryVo> gameRecords = JSON.parseArray(transactions, GameRecordAeSummaryVo.class);
        if (CollectionUtils.isEmpty(gameRecords)) {
            log.info("取得区间内交易摘要时无记录,startTime={},endTime={},platform={}", startTime, endTime, platform);
            return false;
        }
        GameRecordAeSummaryVo aeSummaryVo = gameRecords.get(0);
        //查询本地数据库时间区间内数据汇总
        Date startDateTime = dateFormat.parse(startTime);
        Date endDateTime = dateFormat.parse(endTime);
        SimpleDateFormat sdf = DateUtil.getSimpleDateFormat();
        String startTime1 = sdf.format(startDateTime);
        String endTime1 = sdf.format(endDateTime);
        Map<String, Object> summaryVo = gameRecordAeService.findSumByPlatformAndTime(platform, startTime1, endTime1);
        if (CollectionUtils.isEmpty(summaryVo)) {
            return true;
        }
        BigDecimal turnover = new BigDecimal(summaryVo.get("turnover").toString());
        if (aeSummaryVo.getTurnover().compareTo(turnover) != 0) {
            return true;
        }
        Integer betCount = Integer.parseInt(summaryVo.get("betCount").toString());
        if (aeSummaryVo.getBetCount() != betCount) {
            return true;
        }
        BigDecimal betAmount = new BigDecimal(summaryVo.get("betAmount").toString());
        if (aeSummaryVo.getBetAmount().compareTo(betAmount) != 0) {
            return true;
        }
        BigDecimal winAmount = new BigDecimal(summaryVo.get("winAmount").toString());
        if (aeSummaryVo.getWinAmount().compareTo(winAmount) != 0) {
            return true;
        }
        BigDecimal realWinAmount = new BigDecimal(summaryVo.get("realWinAmount").toString());
        if (aeSummaryVo.getRealWinAmount().compareTo(realWinAmount) != 0) {
            return true;
        }
        BigDecimal realBetAmount = new BigDecimal(summaryVo.get("realBetAmount").toString());
        if (aeSummaryVo.getRealBetAmount().compareTo(realBetAmount) != 0) {
            return true;
        }
        BigDecimal jackpotBetAmount = new BigDecimal(summaryVo.get("jackpotBetAmount").toString());
        if (aeSummaryVo.getJackpotBetAmount().compareTo(jackpotBetAmount) != 0) {
            return true;
        }
        BigDecimal jackpotWinAmount = new BigDecimal(summaryVo.get("jackpotWinAmount").toString());
        if (aeSummaryVo.getJackpotWinAmount().compareTo(jackpotWinAmount) != 0) {
            return true;
        }
        return false;
    }

    /**
     * 补账
     *
     * @param startTime
     * @param endTime
     * @param platform
     */
    @SneakyThrows
    public void makeUp(String startTime, String endTime, String platform) {
        JSONObject result = aeApi.getTransactionByTxTime(startTime, endTime, platform, null, null, null, null, null);
        if (result == null) {
            log.error("补账时远程请求错误,startTime={},endTime={},platform={}", startTime, endTime, platform);
            return;
        }
        String status = result.getString("status");
        if (!PublicAeApi.SUCCESS_CODE.equals(status)) {
            log.error("补账时错误,startTime={},endTime={},platform={},result={}", startTime, endTime, platform, result);
            return;
        }
        String transactions = result.getString("transactions");
        List<GameRecordAeVo> gameRecords = JSON.parseArray(transactions, GameRecordAeVo.class);
        if (CollectionUtils.isEmpty(gameRecords)) {
            log.info("补账时当前时间无记录,startTime={},endTime={},platform={},result={}", startTime, endTime, platform, result);
            return;
        }
        //保存数据
        gameRecordAeJob.saveAll(platform, gameRecords);
        int size = gameRecords.size();
        //当前时间范围数据没拉取完继续拉取
        if (size == 2000) {
            GameRecordAeVo gameRecordAeVo = gameRecords.get(size - 1);
            String updateTime = gameRecordAeVo.getUpdateTime();
            makeUp(updateTime, endTime, platform);
        }
    }
}
