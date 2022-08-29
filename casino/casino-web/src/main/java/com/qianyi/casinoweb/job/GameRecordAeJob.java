package com.qianyi.casinoweb.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.business.UserMoneyBusiness;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinoweb.vo.GameRecordAeVo;
import com.qianyi.casinoweb.vo.GameRecordObtyDataVo;
import com.qianyi.casinoweb.vo.GameRecordObtyVo;
import com.qianyi.liveae.api.PublicAeApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 查询用户投注记录接口,查询最近一周的数据,每次最大100条
 */
@Component
@Slf4j
public class GameRecordAeJob {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    @Autowired
    private PublicAeApi aeApi;
    @Autowired
    private GameRecordAeService gameRecordAeService;
    @Autowired
    private GameRecordAeEndTimeService gameRecordAeEndTimeService;
    @Autowired
    private UserThirdService userThirdService;
    @Autowired
    private GameRecordObtyDetailService gameRecordObtyDetailService;
    @Autowired
    private UserService userService;
    @Autowired
    private PlatformConfigService platformConfigService;
    @Autowired
    private GameRecordAsyncOper gameRecordAsyncOper;
    @Autowired
    private UserMoneyBusiness userMoneyBusiness;
    @Autowired
    private AdGamesService adGamesService;
    @Autowired
    private PlatformGameService platformGameService;

    //每隔2分钟执行一次
//    @Scheduled(cron = "0 0/2 * * * ?")
    public void pullGameRecord() {
        PlatformGame platformGame = platformGameService.findByGamePlatformName(Constants.PLATFORM_AE);
        if (platformGame != null && platformGame.getGameStatus() == 2) {
            log.info("后台已关闭AE平台,无需拉单,platformGame={}", platformGame);
            return;
        }
        AdGame adGame = adGamesService.findByGamePlatformNameAndGameCode(Constants.PLATFORM_AE, Constants.PLATFORM_AE_HORSEBOOK);
        if (adGame != null && adGame.getGamesStatus() == 2) {
            log.info("后台已关闭HORSEBOOK,无需拉单,adGame={}", adGame);
        } else {
            log.info("定时器开始拉取HORSEBOOK注单记录");
            GameRecordAeEndTime gameRecordAeEndTime = gameRecordAeEndTimeService.findFirstByPlatformAndStatusOrderByEndTimeDesc(Constants.PLATFORM_AE_HORSEBOOK, Constants.yes);
            String time = gameRecordAeEndTime == null ? null : gameRecordAeEndTime.getEndTime();
            //获取查询游戏记录的时间范围
            String startTime = getStartTime(time, Constants.PLATFORM_AE_HORSEBOOK);
            pullGameRecord(startTime, Constants.PLATFORM_AE_HORSEBOOK);
            log.info("定时器拉取完成HORSEBOOK注单记录");
        }
        AdGame svGame = adGamesService.findByGamePlatformNameAndGameCode(Constants.PLATFORM_AE, Constants.PLATFORM_AE_SV388);
        if (svGame != null && svGame.getGamesStatus() == 2) {
            log.info("后台已关闭SV388,无需拉单,adGame={}", svGame);
        } else {
            log.info("定时器开始拉取SV388注单记录");
            GameRecordAeEndTime gameRecordAeEndTime = gameRecordAeEndTimeService.findFirstByPlatformAndStatusOrderByEndTimeDesc(Constants.PLATFORM_AE_SV388, Constants.yes);
            String time = gameRecordAeEndTime == null ? null : gameRecordAeEndTime.getEndTime();
            //获取查询游戏记录的时间范围
            String startTime = getStartTime(time, Constants.PLATFORM_AE_SV388);
            pullGameRecord(startTime, Constants.PLATFORM_AE_SV388);
            log.info("定时器拉取完成SV388注单记录");
        }
        AdGame e1Game = adGamesService.findByGamePlatformNameAndGameCode(Constants.PLATFORM_AE, Constants.PLATFORM_AE_E1SPORT);
        if (e1Game != null && e1Game.getGamesStatus() == 2) {
            log.info("后台已关闭E1SPORT,无需拉单,adGame={}", e1Game);
        } else {
            log.info("定时器开始拉取E1SPORT注单记录");
            GameRecordAeEndTime gameRecordAeEndTime = gameRecordAeEndTimeService.findFirstByPlatformAndStatusOrderByEndTimeDesc(Constants.PLATFORM_AE_E1SPORT, Constants.yes);
            String time = gameRecordAeEndTime == null ? null : gameRecordAeEndTime.getEndTime();
            //获取查询游戏记录的时间范围
            String startTime = getStartTime(time, Constants.PLATFORM_AE_E1SPORT);
            pullGameRecord(startTime, Constants.PLATFORM_AE_E1SPORT);
            log.info("定时器拉取完成E1SPORT注单记录");
        }
    }

    public void pullGameRecord(String startTime, String platform) {
        if (ObjectUtils.isEmpty(startTime)) {
            return;
        }
        log.info("开始拉取{},{}到当前时间的OB体育游戏记录", platform, startTime);
        pullGameRecordByTime(startTime, platform);
        log.info("{},{}到当前时间的OB体育游戏记录拉取完成", platform, startTime);
    }

    public static void main(String[] args) throws ParseException {
        //当前时间转IS0 8601
        String nowAsISO = sdf.format(new Date());
        Date parse = sdf.parse("2021-03-26T12:00:00+08:00");
        System.out.println(nowAsISO);
        String format = DateUtil.getSimpleDateFormat().format(parse);
        System.out.println(parse.getTime());
        System.out.println(format);
    }

    public void pullGameRecordByTime(String startTime, String platform) {
        //每次最多拉2000条,未拉完请接续上次拉账最后一笔「交易更新时间」为搜寻起始时间
        String endTime = sdf.format(new Date());
        JSONObject result = aeApi.getTransactionByUpdateDate(startTime, platform, null, null, null, null);
        if (result == null) {
            log.error("拉取{}注单时远程请求错误,startTime={}", platform, startTime);
            saveGameRecordAeEndTime(startTime, endTime, platform, Constants.no);
            return;
        }
        String status = result.getString("status");
        if (!PublicAeApi.SUCCESS_CODE.equals(status)) {
            log.error("拉取{}注单时报错,startTime={},result={}", platform, startTime, result);
            saveGameRecordAeEndTime(startTime, endTime, platform, Constants.no);
            return;
        }
        String transactions = result.getString("transactions");
        List<GameRecordAeVo> gameRecords = JSON.parseArray(transactions, GameRecordAeVo.class);
        if (CollectionUtils.isEmpty(gameRecords)) {
            log.info("拉取{}注单当前时间无记录,startTime={},result={}", platform, startTime, result);
            return;
        }
        //保存数据
        saveAll(platform, gameRecords);
        int size = gameRecords.size();
        //当前时间范围数据没拉取完继续拉取
        if (size == 2000) {
            GameRecordAeVo gameRecordAeVo = gameRecords.get(size - 1);
            String updateTime = gameRecordAeVo.getUpdateTime();
            pullGameRecordByTime(updateTime, platform);
        }
        //保存时间区间
        saveGameRecordAeEndTime(startTime, endTime, platform, Constants.yes);
    }

    /**
     * @param startTime
     * @return
     * @throws ParseException
     */
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
        Date startBeforeTime = sdf.parse(startTime);
        long diffTime = endTime.getTime() - startBeforeTime.getTime();
        //三方最多能查当前时间24小时前的，提前一分钟，防止请求三方时时间超过24小时
        if (diffTime > 24 * 59 * 60 * 1000) {
            String startTimeNew = getBeforeTime(endTime, -24 * 59 * 60);
            log.error("{}注单拉取时间范围超过24小时,开始时间缩短至当前时间24小时前，{}~{}时间范围数据丢失", platform, startTime, startTimeNew);
            startTime = startTimeNew;
        }
        return startTime;
    }


    public String getBeforeTime(Date date, int num) {
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        now.add(Calendar.MINUTE, num);
        Date before = now.getTime();
        return sdf.format(before);
    }


    public void saveAll(String platform, List<GameRecordAeVo> gameRecordList) {
        if (CollectionUtils.isEmpty(gameRecordList)) {
            return;
        }
        log.info("开始处理{}游戏记录数据", platform);
        //查询最小清0打码量
        PlatformConfig platformConfig = platformConfigService.findFirst();
        for (GameRecordAeVo gameRecordAeVo : gameRecordList) {
            GameRecordAe gameRecord = null;
            try {
                gameRecord = save(gameRecordAeVo);
                if (gameRecord == null) {
                    continue;
                }
                //业务处理
                business(Constants.PLATFORM_AE, gameRecord, platformConfig);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("保存OB体育游戏记录时报错,message={}", e.getMessage());
            }
        }
    }

    public void business(String platform, GameRecordAe gameRecordAe, PlatformConfig platformConfig) {
        //计算用户账号实时余额
        gameRecordAsyncOper.changeUserBalance(gameRecordAe.getUserId(), gameRecordAe.getRealBetAmount(), gameRecordAe.getRealWinAmount());
        //组装gameRecord
        GameRecord record = combineGameRecord(gameRecordAe);
        //发送注单消息到MQ后台要统计数据
        gameRecordAsyncOper.proxyGameRecordReport(platform, record);
        String validbet = record.getValidbet();
        if (ObjectUtils.isEmpty(validbet) || new BigDecimal(validbet).compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        //洗码
        gameRecordAsyncOper.washCode(platform, record);
        // 抽点
        gameRecordAsyncOper.extractPoints(platform, record);
        //扣减打码量
        gameRecordAsyncOper.subCodeNum(platform, platformConfig, record);
        //代理分润
        gameRecordAsyncOper.shareProfit(platform, record);
        //返利
        gameRecordAsyncOper.rebate(platform, record);
    }

    @SneakyThrows
    public GameRecordAe save(GameRecordAeVo gameRecordAeVo) {
        UserThird account = userThirdService.findByObtyAccount(gameRecordAeVo.getUserId());
        if (account == null || account.getUserId() == null) {
            log.error("同步游戏记录时，UserThird查询结果为null,account={}", gameRecordAeVo.getUserId());
            return null;
        }
        GameRecordAe gameRecord = gameRecordAeService.findByPlatformAndPlatformTxId(gameRecordAeVo.getPlatform(), gameRecordAeVo.getPlatformTxId());
        if (gameRecord == null) {
            gameRecord = new GameRecordAe();
        }
        //时间转成标准时间格式
        if (!ObjectUtils.isEmpty(gameRecordAeVo.getTxTime())) {
            Date txTime = sdf.parse(gameRecordAeVo.getTxTime());
            String txTimeStr = DateUtil.getSimpleDateFormat().format(txTime);
            gameRecordAeVo.setTxTime(txTimeStr);
        }
        if (!ObjectUtils.isEmpty(gameRecordAeVo.getUpdateTime())) {
            Date updateTime = sdf.parse(gameRecordAeVo.getUpdateTime());
            String updateTimeStr = DateUtil.getSimpleDateFormat().format(updateTime);
            gameRecordAeVo.setUpdateTime(updateTimeStr);
        }
        if (!ObjectUtils.isEmpty(gameRecordAeVo.getBetTime())) {
            Date betTime = sdf.parse(gameRecordAeVo.getBetTime());
            String betTimeStr = DateUtil.getSimpleDateFormat().format(betTime);
            gameRecordAeVo.setBetTime(betTimeStr);
        }
        BeanUtils.copyProperties(gameRecordAeVo, gameRecord);
        gameRecord.setUserId(account.getUserId());
        gameRecord.setAccount(gameRecordAeVo.getUserId());
        gameRecord.setUpdateTimeStr(gameRecordAeVo.getUpdateTime());
        BigDecimal validbet = ObjectUtils.isEmpty(gameRecord.getTurnover()) ? BigDecimal.ZERO : gameRecord.getTurnover();
        //有效投注额为0不参与洗码,打码,分润,抽點
        if (validbet.compareTo(BigDecimal.ZERO) == 0) {
            gameRecord.setWashCodeStatus(Constants.yes);
            gameRecord.setCodeNumStatus(Constants.yes);
            gameRecord.setShareProfitStatus(Constants.yes);
            gameRecord.setExtractStatus(Constants.yes);
            gameRecord.setRebateStatus(Constants.yes);
        }
        //查询3级代理
        User user = userService.findById(gameRecord.getUserId());
        if (user != null) {
            gameRecord.setFirstProxy(user.getFirstProxy());
            gameRecord.setSecondProxy(user.getSecondProxy());
            gameRecord.setThirdProxy(user.getThirdProxy());
        }
        GameRecordAe record = gameRecordAeService.save(gameRecord);
        return record;
    }

    private GameRecord combineGameRecord(GameRecordAe item) {
        GameRecord gameRecord = new GameRecord();
        gameRecord.setBetId(item.getPlatformTxId());
        gameRecord.setUserId(item.getUserId());
        gameRecord.setGameCode(item.getPlatform());
        gameRecord.setGname(Constants.PLATFORM_AE);
        gameRecord.setBetTime(item.getBetTime());
        gameRecord.setId(item.getId());
        gameRecord.setFirstProxy(item.getFirstProxy());
        gameRecord.setSecondProxy(item.getSecondProxy());
        gameRecord.setThirdProxy(item.getThirdProxy());
        if (item.getTurnover() != null) {
            gameRecord.setValidbet(item.getTurnover().toString());
        }
        if (!ObjectUtils.isEmpty(item.getRealBetAmount())) {
            gameRecord.setBet(item.getRealBetAmount().toString());
        }
        if (item.getRealWinAmount() != null) {
            gameRecord.setWinLoss(item.getRealWinAmount().toString());
        }
        return gameRecord;
    }

    private void saveGameRecordAeEndTime(String startTime, String endTime, String platform, Integer status) {
        GameRecordAeEndTime gameRecordAeEndTime = new GameRecordAeEndTime();
        gameRecordAeEndTime.setStartTime(startTime);
        gameRecordAeEndTime.setEndTime(endTime);
        gameRecordAeEndTime.setStatus(status);
        gameRecordAeEndTime.setPlatform(platform);
        gameRecordAeEndTimeService.save(gameRecordAeEndTime);
    }
}
