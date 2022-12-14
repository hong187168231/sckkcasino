package com.qianyi.casinoweb.job;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.GameRecordVNCVo;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 查询最近数据，最大两个月前的数据量
 * 一次查询最大范围30分钟
 */
@Component
@Slf4j
public class GameRecordVNCJob {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private PublicLotteryApi lotteryApi;
    @Autowired
    private GameRecordVNCService gameRecordVNCService;
    @Autowired
    private GameRecordVNCEndTimeService gameRecordVNCEndTimeService;
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

    //每隔5分钟30秒执行一次
    @Scheduled(cron = "30 0/4 * * * ?")
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
            GameRecordVNCEndTime gameRecordVNCEndTime = gameRecordVNCEndTimeService.findFirstByPlatformAndStatusOrderByEndTimeDesc(Constants.PLATFORM_VNC, Constants.yes);
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
            log.info("开始拉取{},{}到当前时间的游戏记录", platform, startTime);
            pullGameRecordByTime(startTime, platform);
            log.info("{},{}到当前时间的记录拉取完成", platform, startTime);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("{},{}到当前时间的记录拉取异常", platform, startTime);
        }
    }

    public void pullGameRecordByTime(String startTime, String platform) throws Exception {
        String endTime = setEndTime(startTime);

        String strResult = lotteryApi.getDateTimeReport(startTime, endTime, "KK");
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
        List<GameRecordVNCVo> gameRecords = JSON.parseArray(transactions, GameRecordVNCVo.class);
        gameRecords = gameRecords.stream().filter(ga -> ga.getSettleState() == true).collect(Collectors.toList());
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

    @SneakyThrows
    private String setEndTime(String startTime) {
        Date startDateTime = sdf.parse(startTime);
        Date endTime = DateUtil.addMinuteDate(startDateTime, 30);
        if(endTime.getTime() < new Date().getTime()){
            return sdf.format(endTime);
        }
        return sdf.format(new Date());
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
//        Date startBeforeTime = sdf.parse(startTime);
//        long diffTime = endTime.getTime() - startBeforeTime.getTime();
//        //三方最多能查当前时间24小时前的，提前5分钟，防止请求三方时时间超过24小时
//        if (diffTime > 24 * 60 * 60 * 1000) {
//            String startTimeNew = getBeforeTime(endTime, -(24 * 60 - 5));
//            log.error("{}注单拉取时间范围超过24小时,开始时间缩短至当前时间24小时前，{}~{}时间范围数据丢失", platform, startTime, startTimeNew);
//            startTime = startTimeNew;
//        }
        return startTime;
    }


    public static String getBeforeTime(Date date, int num) {
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        now.add(Calendar.MINUTE, num);
        Date before = now.getTime();
        return sdf.format(before);
    }

    @SneakyThrows
    public static void main(String[] args) {
        SimpleDateFormat df = DateUtil.getSimpleDateFormat();
        Date parse = df.parse("2022-09-01 12:00:00");
        String format = sdf.format(parse);
        System.out.println(format);
    }

    public void saveAll(String platform, List<GameRecordVNCVo> gameRecordVNCList) {
        if (CollectionUtils.isEmpty(gameRecordVNCList)) {
            return;
        }
        log.info("开始处理{}游戏记录数据", platform);
        //查询最小清0打码量
        PlatformConfig platformConfig = platformConfigService.findFirst();
        for (GameRecordVNCVo gameRecordVNC : gameRecordVNCList) {
            GameRecordVNC gameRecord = null;
            try {
                gameRecord = save(gameRecordVNC);
                if (gameRecord == null) {
                    continue;
                }
                //业务处理
//                business(Constants.PLATFORM_VNC, gameRecord, platformConfig);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("保存{}游戏记录时报错,message={}", platform,e.getMessage());
            }
        }
    }

    public void business(String platform, GameRecordVNC gameRecordVNC, PlatformConfig platformConfig) {
        //计算用户账号实时余额
        Integer isAdd = gameRecordVNC.getIsAdd();
        if (isAdd == 1) {
            gameRecordAsyncOper.changeUserBalance(gameRecordVNC.getUserId(), gameRecordVNC.getBetMoney(), gameRecordVNC.getWinMoney());
        }
        //组装gameRecord
        GameRecord record = combineGameRecord(gameRecordVNC);
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
            //等级流水
            gameRecordAsyncOper.levelWater(platform, record);
        }
    }

    @SneakyThrows
    public GameRecordVNC save(GameRecordVNCVo gameRecordVNC) {
        UserThird account = userThirdService.findByVncAccount(gameRecordVNC.getUserName());
        if (account == null || account.getUserId() == null) {
            log.error("同步游戏记录时，UserThird查询结果为null,account={}", gameRecordVNC.getUserName());
            return null;
        }
        GameRecordVNC gameRecord = gameRecordVNCService.findByMerchantCodeAndBetOrder(gameRecordVNC.getMerchantCode(), gameRecordVNC.getBetOrder());
        if (gameRecord == null) {
            gameRecord = new GameRecordVNC();
            gameRecord.setIsAdd(1);//新增
        }
        //有效投注
        BigDecimal oldTurnover = gameRecord.getRealMoney();
        //用户输赢
        BigDecimal oldRealWinAmount = gameRecordVNC.getWinMoney().subtract(gameRecordVNC.getRealMoney());
        //时间转成标准时间格式
        if (!ObjectUtils.isEmpty(gameRecordVNC.getBetTime())) {
            String txTimeStr = DateUtil.getSimpleDateFormat().format(gameRecordVNC.getBetTime());
            gameRecordVNC.setBetTimeStr(txTimeStr);
        }
        if (!ObjectUtils.isEmpty(gameRecordVNC.getSettleTime()) && gameRecordVNC.getSettleState()) {
            String updateTimeStr = DateUtil.getSimpleDateFormat().format(gameRecordVNC.getSettleTime());
            gameRecordVNC.setSettleTimeStr(updateTimeStr);
        }

        BeanUtils.copyProperties(gameRecordVNC, gameRecord);
        gameRecord.setUserId(account.getUserId());
        gameRecord.setAccount(gameRecordVNC.getUserName());
        BigDecimal validbet = ObjectUtils.isEmpty(gameRecord.getRealMoney()) ? BigDecimal.ZERO : gameRecord.getRealMoney();
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
        GameRecordVNC record = gameRecordVNCService.save(gameRecord);
        //旧输赢和有效投注差值
        record.setOldTurnover(oldTurnover);
        record.setOldRealWinAmount(oldRealWinAmount);
        return record;
    }

    public GameRecord combineGameRecord(GameRecordVNC item) {
        GameRecord gameRecord = new GameRecord();
        gameRecord.setBetId(item.getBetOrder());
        gameRecord.setUserId(item.getUserId());
        gameRecord.setGameCode("VNC");
        gameRecord.setGname(Constants.PLATFORM_VNC);
        gameRecord.setBetTime(item.getBetTimeStr());
        gameRecord.setId(item.getId());
        gameRecord.setFirstProxy(item.getFirstProxy());
        gameRecord.setSecondProxy(item.getSecondProxy());
        gameRecord.setThirdProxy(item.getThirdProxy());
        gameRecord.setIsAdd(item.getIsAdd());
        if (item.getIsAdd() == 1) {
            if (item.getRealMoney() != null) {
                gameRecord.setValidbet(item.getRealMoney().toString());
            }
            if (!ObjectUtils.isEmpty(item.getBetMoney())) {
                gameRecord.setBet(item.getBetMoney().toString());
            }
            if (item.getWinMoney() != null) {
                BigDecimal winLoss = item.getWinMoney();
                gameRecord.setWinLoss(winLoss.toString());
            }
        } else {
            gameRecord.setBet("0");//经过三方确认下注金额不会更新
            if (item.getBetMoney() != null && item.getOldTurnover() != null) {
                BigDecimal turnover = item.getBetMoney().subtract(item.getOldTurnover());
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

    private void saveGameRecordVNCEndTime(String startTime, String endTime, String platform, Integer status) {
        GameRecordVNCEndTime gameRecordVNCEndTime = new GameRecordVNCEndTime();
        gameRecordVNCEndTime.setStartTime(startTime);
        gameRecordVNCEndTime.setEndTime(endTime);
        gameRecordVNCEndTime.setStatus(status);
        gameRecordVNCEndTime.setPlatform(platform);
        gameRecordVNCEndTimeService.save(gameRecordVNCEndTime);
    }

}
