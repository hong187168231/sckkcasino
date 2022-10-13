package com.qianyi.casinoweb.job;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.RptBetInfoDetailVo;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.lottery.api.PublicLotteryApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
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

    @Autowired
    private UserThirdService userThirdService;

    @Autowired
    private UserService userService;

    @Autowired
    private PlatformConfigService platformConfigService;
    @Autowired
    private GameRecordAsyncOper gameRecordAsyncOper;


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
        List<RptBetInfoDetailVo> gameRecords = JSON.parseArray(transactions, RptBetInfoDetailVo.class);
        gameRecords = gameRecords.stream().filter(ga -> ga.getSettleState() == true && ga.getIsCanceled() == false).collect(Collectors.toList());
        if (gameRecords.isEmpty()) {
            log.info("拉取{}注单当前时间无记录,startTime={},result={}", platform, startTime, transactions);
            saveGameRecordVNCEndTime(startTime, endTime, platform, Constants.yes);
            return;
        }
        //保存数据
        saveAll(platform, gameRecords);
        //保存时间区间
        saveGameRecordVNCEndTime(startTime, endTime, platform, Constants.yes);
    }

    public void saveAll(String platform, List<RptBetInfoDetailVo> rptBetInfoDetailList) {
        if (CollectionUtils.isEmpty(rptBetInfoDetailList)) {
            return;
        }
        log.info("开始处理{}游戏记录数据", platform);
        //查询最小清0打码量
        PlatformConfig platformConfig = platformConfigService.findFirst();
        for (RptBetInfoDetailVo rptBetInfoDetailVo : rptBetInfoDetailList) {
            try {
                RptBetInfoDetail gameRecord = save(rptBetInfoDetailVo);
                if (gameRecord == null) {
                    continue;
                }
                //业务处理
                business(Constants.PLATFORM_VNC, gameRecord, platformConfig);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("保存{}游戏记录时报错,message={}", platform,e.getMessage());
            }
        }
    }

    private void business(String platform, RptBetInfoDetail gameRecord, PlatformConfig platformConfig) {

        //计算用户账号实时余额
        Integer isAdd = gameRecord.getIsAdd();
        if (isAdd == 1) {
            gameRecordAsyncOper.changeUserBalance(gameRecord.getUserKkId(), gameRecord.getRealMoney(), gameRecord.getWinMoney());
        }
        //组装gameRecord
        GameRecord record = combineGameRecord(gameRecord);
        //发送注单消息到MQ后台要统计数据
        if (isAdd == 1 || (isAdd == 0 && (BigDecimal.ZERO.compareTo(new BigDecimal(record.getValidbet())) != 0 || BigDecimal.ZERO.compareTo(new BigDecimal(record.getBet())) != 0 || BigDecimal.ZERO.compareTo(new BigDecimal(record.getWinLoss())) != 0))) {
            gameRecordAsyncOper.proxyGameRecordReport(platform, record);
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
        }
    }

    private GameRecord combineGameRecord(RptBetInfoDetail rptBetInfoDetail) {

        GameRecord gameRecord = new GameRecord();
        gameRecord.setBetId(rptBetInfoDetail.getBetDetailOrder());
        gameRecord.setUserId(rptBetInfoDetail.getUserId());
        gameRecord.setGameCode("VNC");
        gameRecord.setGname(Constants.PLATFORM_VNC);
        gameRecord.setBetTime(rptBetInfoDetail.getBetTimeStr());
        gameRecord.setFirstProxy(rptBetInfoDetail.getFirstProxy());
        gameRecord.setSecondProxy(rptBetInfoDetail.getSecondProxy());
        gameRecord.setThirdProxy(rptBetInfoDetail.getThirdProxy());
        gameRecord.setIsAdd(rptBetInfoDetail.getIsAdd());
        if (rptBetInfoDetail.getIsAdd() == 1) {
            if (rptBetInfoDetail.getRealMoney() != null) {
                gameRecord.setValidbet(rptBetInfoDetail.getRealMoney().toString());
            }
            if (!ObjectUtils.isEmpty(rptBetInfoDetail.getBetMoney())) {
                gameRecord.setBet(rptBetInfoDetail.getBetMoney().toString());
            }
            if (rptBetInfoDetail.getWinMoney() != null) {
                BigDecimal winLoss = rptBetInfoDetail.getWinMoney();
                gameRecord.setWinLoss(winLoss.toString());
            }
        } else {
            gameRecord.setBet("0");//经过三方确认下注金额不会更新
            if (rptBetInfoDetail.getBetMoney() != null && rptBetInfoDetail.getOldTurnover() != null) {
                BigDecimal turnover = rptBetInfoDetail.getBetMoney().subtract(rptBetInfoDetail.getOldTurnover());
                gameRecord.setValidbet(turnover.toString());
            }
            if (rptBetInfoDetail.getWinMoney() != null && rptBetInfoDetail.getOldRealWinAmount() != null) {
                BigDecimal newWinLoss = rptBetInfoDetail.getWinMoney();
                BigDecimal oldWinLoss = rptBetInfoDetail.getOldRealWinAmount();
                BigDecimal winLoss = newWinLoss.subtract(oldWinLoss);
                gameRecord.setWinLoss(winLoss.toString());
            }
        }
        return gameRecord;
    }

    private RptBetInfoDetail save(RptBetInfoDetailVo rptBetInfoDetailVo) {

        UserThird account = userThirdService.findByVncAccount(rptBetInfoDetailVo.getUserName());
        if (account == null || account.getUserId() == null) {
            log.error("同步游戏记录时，UserThird查询结果为null,account={}", rptBetInfoDetailVo.getUserName());
            return null;
        }
        RptBetInfoDetail rptBetInfoDetail = rptBetInfoDetailService.findByMerchantCodeAndBetDetailOrder(rptBetInfoDetailVo.getMerchantCode(), rptBetInfoDetailVo.getBetDetailOrder());
        if (rptBetInfoDetail == null) {
            rptBetInfoDetail = new RptBetInfoDetail();
            rptBetInfoDetail.setIsAdd(1);//新增
        }
        //有效投注
        BigDecimal oldTurnover = rptBetInfoDetail.getRealMoney();
        //用户输赢
        BigDecimal oldRealWinAmount = rptBetInfoDetailVo.getWinMoney().subtract(rptBetInfoDetailVo.getRealMoney());
        //时间转成标准时间格式
        if (!ObjectUtils.isEmpty(rptBetInfoDetailVo.getBetTime())) {
            String txTimeStr = DateUtil.getSimpleDateFormat().format(rptBetInfoDetailVo.getBetTime());
            rptBetInfoDetailVo.setBetTimeStr(txTimeStr);
        }
        if (!ObjectUtils.isEmpty(rptBetInfoDetailVo.getSettleTime()) && rptBetInfoDetailVo.getSettleState()) {
            String updateTimeStr = DateUtil.getSimpleDateFormat().format(rptBetInfoDetailVo.getSettleTime());
            rptBetInfoDetailVo.setSettleTimeStr(updateTimeStr);
        }

        BeanUtils.copyProperties(rptBetInfoDetailVo, rptBetInfoDetail);
        rptBetInfoDetail.setUserId(account.getUserId());
        rptBetInfoDetail.setAccount(rptBetInfoDetailVo.getUserName());
        BigDecimal validbet = ObjectUtils.isEmpty(rptBetInfoDetail.getRealMoney()) ? BigDecimal.ZERO : rptBetInfoDetail.getRealMoney();
        //有效投注额为0不参与洗码,打码,分润,抽點
        if (validbet.compareTo(BigDecimal.ZERO) == 0) {
            rptBetInfoDetail.setWashCodeStatus(Constants.yes);
            rptBetInfoDetail.setCodeNumStatus(Constants.yes);
            rptBetInfoDetail.setShareProfitStatus(Constants.yes);
            rptBetInfoDetail.setExtractStatus(Constants.yes);
            rptBetInfoDetail.setRebateStatus(Constants.yes);
        }
        rptBetInfoDetail.setUserKkId(account.getUserId());
        //查询3级代理
        User user = userService.findById(account.getUserId());
        if (user != null) {
            rptBetInfoDetail.setFirstProxy(user.getFirstProxy());
            rptBetInfoDetail.setSecondProxy(user.getSecondProxy());
            rptBetInfoDetail.setThirdProxy(user.getThirdProxy());
        }
        RptBetInfoDetail record = rptBetInfoDetailService.save(rptBetInfoDetail);
        //旧输赢和有效投注差值
        record.setOldTurnover(oldTurnover);
        record.setOldRealWinAmount(oldRealWinAmount);
        return record;
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
