package com.qianyi.casinoweb.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.DGTradeReportVo;
import com.qianyi.livedg.api.DgApi;
import com.qianyi.modulecommon.Constants;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class GameRecordDGJob {


    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private DgApi dgApi;
    @Autowired
    private GameRecordDGService gameRecordDGService;
    @Autowired
    private GameRecordDGEndTimeService gameRecordDGEndTimeService;
    @Autowired
    private UserThirdService userThirdService;
    @Autowired
    private UserService userService;
    @Autowired
    private PlatformConfigService platformConfigService;
    @Autowired
    private GameRecordAsyncOper gameRecordAsyncOper;
    @Autowired
    private AdGamesService adGamesService;
    @Autowired
    private PlatformGameService platformGameService;

    //每隔4分钟执行一次
    @Scheduled(fixedDelay = 20000)
    public void pullGameRecord() {
        PlatformGame platformGame = platformGameService.findByGamePlatformName(Constants.PLATFORM_DG);
        //平台关闭，但是拉单还是要继续进行
        if (platformGame != null && platformGame.getGameStatus() == 2) {
            log.info("后台已关闭DG平台,无需拉单,platformGame={}", platformGame);
            return;
        }
        AdGame adGame = adGamesService.findByGamePlatformNameAndGameCode(Constants.PLATFORM_DG, Constants.PLATFORM_DG);
        if (adGame != null && adGame.getGamesStatus() == 2) {
            log.info("后台已关闭DG,无需拉单,adGame={}", adGame);
        } else {
            log.info("定时器开始拉取DG电竞注单记录");
            GameRecordDGEndTime gameRecordDGEndTime = gameRecordDGEndTimeService.findFirstByPlatformAndStatusOrderByEndTimeDesc(Constants.PLATFORM_DG, Constants.yes);
            String time = gameRecordDGEndTime == null ? null : gameRecordDGEndTime.getEndTime();
            //获取查询游戏记录的时间范围
//            String startTime = getStartTime(time, Constants.PLATFORM_DG);
            pullGameRecord(time, Constants.PLATFORM_DG);
            log.info("定时器拉取完成DG注单记录");

        }
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
        //三方最多能查当前时间24小时前的，提前5分钟，防止请求三方时时间超过24小时
        if (diffTime > 24 * 60 * 60 * 1000) {
            String startTimeNew = getBeforeTime(endTime, -(24 * 60 - 5));
            log.error("{}注单拉取时间范围超过24小时,开始时间缩短至当前时间24小时前，{}~{}时间范围数据丢失", platform, startTime, startTimeNew);
            startTime = startTimeNew;
        }
        return startTime;
    }

    public static String getBeforeTime(Date date, int num) {
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        now.add(Calendar.MINUTE, num);
        Date before = now.getTime();
        return sdf.format(before);
    }

    public void pullGameRecord(String startTime, String platform) {
        try {
            if (ObjectUtils.isEmpty(startTime)) {
                return;
            }
            log.info("开始拉取{},{}到当前时间的游戏记录", platform, startTime);
            pullGameRecordByTime(startTime, platform);
            log.info("{},{}到当前时间的记录拉取完成", platform, startTime);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("{},{}到当前时间的记录拉取异常", platform, startTime);
        }
    }

    public void pullGameRecordByTime(String startTime, String platform) throws Exception {
        String endTime = sdf.format(new Date());
//        String transactions = dgApi.getDateTimeReport(startTime, endTime);
        JSONObject apiResponseData = dgApi.getReport();
        if (null != apiResponseData && "0".equals(apiResponseData.getString("codeId"))){
            String transactions = apiResponseData.getString("list");
            if(null!=transactions&&!"".equals(transactions)){
                List<DGTradeReportVo> gameRecords = JSON.parseArray(transactions, DGTradeReportVo.class);
                if (gameRecords.isEmpty()) {
                    log.info("拉取{}注单当前时间无记录,startTime={},result={}", platform, startTime, transactions);
                    saveGameRecordDGEndTime(startTime, endTime, platform, Constants.yes);
                    return;
                }
                //保存数据
                List<Long> idList = saveAll(platform, gameRecords);
                //保存时间区间
                saveGameRecordDGEndTime(startTime, endTime, platform, Constants.yes);
                //标记已抓取注单
                dgApi.markReport(idList);
            }else {
                log.info("拉取{}注单当前时间无记录,startTime={},result={}", platform, startTime, apiResponseData);
                saveGameRecordDGEndTime(startTime, endTime, platform, Constants.yes);
            }
        }else {
            log.info("拉取{}注单当前时间无记录,startTime={},result={}", platform, startTime, apiResponseData);
            saveGameRecordDGEndTime(startTime, endTime, platform, Constants.yes);
        }
    }


    private void saveGameRecordDGEndTime(String startTime, String endTime, String platform, Integer status) {
        GameRecordDGEndTime gameRecordDGEndTime = new GameRecordDGEndTime();
        gameRecordDGEndTime.setStartTime(startTime);
        gameRecordDGEndTime.setEndTime(endTime);
        gameRecordDGEndTime.setStatus(status);
        gameRecordDGEndTime.setPlatform(platform);
        gameRecordDGEndTimeService.save(gameRecordDGEndTime);
    }


    public List<Long> saveAll(String platform, List<DGTradeReportVo> gameRecordDGVoList) {
        if (CollectionUtils.isEmpty(gameRecordDGVoList)) {
            return null;
        }
        log.info("开始处理{}游戏记录数据", platform);
        //查询最小清0打码量
        PlatformConfig platformConfig = platformConfigService.findFirst();
        List<Long> idList = new ArrayList<>();
        for (DGTradeReportVo gameRecordDGVo : gameRecordDGVoList) {

            GameRecordDG gameRecord = save(gameRecordDGVo);
            if ("1".equals(gameRecord.getIsRevocation())){//是否结算：1：已结算 2:撤销
                business(Constants.PLATFORM_DG, gameRecord, platformConfig);
            }
            idList.add(gameRecordDGVo.getId());
        }
        return idList;
    }


    @SneakyThrows
    public GameRecordDG save(DGTradeReportVo gameRecordDGVo) {
        GameRecordDG gameRecordDG = new GameRecordDG();
        gameRecordDG.setBetOrderNo(String.valueOf(gameRecordDGVo.getId()));
        BeanUtils.copyProperties(gameRecordDGVo, gameRecordDG);
        GameRecordDG gameRecord = gameRecordDGService.findByBetOrderNo(String.valueOf(gameRecordDG.getBetOrderNo()));
        if (gameRecord == null) {
            gameRecord = new GameRecordDG();
            BeanUtils.copyProperties(gameRecordDG, gameRecord);
            gameRecord.setIsAdd(1);//新增
        }else {
            BeanUtils.copyProperties(gameRecordDG, gameRecord);
            gameRecord.setIsAdd(0);//0.修改
        }
        UserThird account = userThirdService.findByDgAccount(gameRecord.getUserName());
        if (account == null || account.getUserId() == null) {
            log.error("同步游戏记录时，UserThird查询结果为null,account={}", gameRecord.getUserName());
            return null;
        }
        gameRecord.setUserId(account.getUserId());
        BigDecimal validbet = ObjectUtils.isEmpty(gameRecord.getAvailableBet()) ? BigDecimal.ZERO : gameRecord.getAvailableBet();
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
        //实付金额 派彩金额减去有效下注金额
        if(gameRecord.getWinOrLoss().compareTo(BigDecimal.ZERO.stripTrailingZeros()) == 0){
            gameRecord.setRealMoney(gameRecord.getBetPoints());
        }else {
            //派彩金额减去下注金额
            gameRecord.setWinMoney(gameRecord.getWinOrLoss().subtract(gameRecord.getBetPoints()));
        }
        GameRecordDG record = gameRecordDGService.save(gameRecord);
        return record;
    }


    public void business(String platform, GameRecordDG gameRecordDG, PlatformConfig platformConfig) {
        //计算用户账号实时余额
        Integer isAdd = gameRecordDG.getIsAdd();
        if (isAdd == 1) {
            gameRecordAsyncOper.changeUserBalance(gameRecordDG.getUserId(), gameRecordDG.getBetPoints(), gameRecordDG.getWinMoney());
        }
        //组装gameRecord
        GameRecord record = combineGameRecord(gameRecordDG);
        //发送注单消息到MQ后台要统计数据
        if (isAdd == 1) {
            gameRecordAsyncOper.proxyGameRecordReport(platform, record);
        } else if (isAdd == 0) {
            if (StringUtils.isBlank(record.getBet())) {
                record.setBet("0");
            }
            if (StringUtils.isBlank(record.getWinLoss())) {
                record.setWinLoss("0");
            }
            if (BigDecimal.ZERO.compareTo(new BigDecimal(record.getBet())) != 0 || BigDecimal.ZERO.compareTo(new BigDecimal(record.getWinLoss())) != 0) {
                gameRecordAsyncOper.proxyGameRecordReport(platform, record);
            }
        }

        String validbet = record.getValidbet();
        if (record.getIsAdd() != 1 || ObjectUtils.isEmpty(validbet) || new BigDecimal(validbet).compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        if (isAdd == 1) {
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
            //等级流水
            gameRecordAsyncOper.levelWater(platform, record);
        }
    }

    public GameRecord combineGameRecord(GameRecordDG item) {
        GameRecord gameRecord = new GameRecord();
        gameRecord.setBetId(String.valueOf(item.getBetOrderNo()));
        gameRecord.setUserId(item.getUserId());
        gameRecord.setGameCode("DG");
        gameRecord.setGname(Constants.PLATFORM_DG);
        gameRecord.setBetTime(item.getBetTime());
        gameRecord.setId(item.getId());
        gameRecord.setFirstProxy(item.getFirstProxy());
        gameRecord.setSecondProxy(item.getSecondProxy());
        gameRecord.setThirdProxy(item.getThirdProxy());
        gameRecord.setIsAdd(item.getIsAdd());
        if (item.getIsAdd() == 1) {
            if (item.getAvailableBet() != null) {
                gameRecord.setValidbet(item.getAvailableBet().toString());
            }
            if (!ObjectUtils.isEmpty(item.getBetPoints())) {
                gameRecord.setBet(item.getBetPoints().toString());
            }
            if (item.getWinMoney() != null) {
                BigDecimal winLoss = item.getWinMoney();
                gameRecord.setWinLoss(winLoss.toString());
            }
        } else {
            gameRecord.setBet("0");//经过三方确认下注金额不会更新
            if (item.getBetPoints() != null && item.getOldTurnover() != null) {
                BigDecimal turnover = item.getBetPoints().subtract(item.getOldTurnover());
                gameRecord.setValidbet(turnover.toString());
            }
            if (item.getWinMoney() != null && item.getOldRealWinAmount() != null) {
                BigDecimal newWinLoss = item.getWinMoney();
                BigDecimal oldWinLoss = item.getOldRealWinAmount();
                BigDecimal winLoss = newWinLoss.subtract(oldWinLoss);
                gameRecord.setWinLoss(winLoss.toString());
            }
        }
        return gameRecord;
    }

    @Data
    public static class StartTimeAndEndTime {
        private Long startTime;
        private Long endTime;
    }

}
