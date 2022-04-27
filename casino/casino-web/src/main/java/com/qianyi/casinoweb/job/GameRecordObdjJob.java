package com.qianyi.casinoweb.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.business.UserMoneyBusiness;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.liveob.api.PublicObdjApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OB电竞定时任务拉取注单记录
 */
@Component
@Slf4j
public class GameRecordObdjJob {

    @Autowired
    private PublicObdjApi obdjApi;
    @Autowired
    private GameRecordObdjService gameRecordObdjService;
    @Autowired
    private GameRecordObEndTimeService gameRecordObEndTimeService;
    @Autowired
    private GameRecordObdjTournamentService gameRecordObdjTournamentService;
    @Autowired
    private UserThirdService userThirdService;
    @Autowired
    private GameRecordObdjDetailService gameRecordObdjDetailService;
    @Autowired
    private UserService userService;
    @Autowired
    private PlatformConfigService platformConfigService;
    @Autowired
    private GameRecordAsyncOper gameRecordAsyncOper;
    @Autowired
    private UserMoneyBusiness userMoneyBusiness;

    //每隔6分钟执行一次
    @Scheduled(cron = "0 0/6 * * * ?")
    public void pullGameRecord() {
        log.info("定时器开始拉取OB电竞注单记录");
        GameRecordObEndTime gameRecordObEndTime = gameRecordObEndTimeService.findFirstByVendorCodeAndStatusOrderByEndTimeDesc(Constants.PLATFORM_OBDJ, Constants.yes);
        Long time = gameRecordObEndTime == null ? null : gameRecordObEndTime.getEndTime();
        //获取查询游戏记录的时间范围
        List<StartTimeAndEndTime> timeList = getStartTimeAndEndTime(time);
        pullGameRecord(timeList);
        log.info("定时器拉取完成OB电竞注单记录");
    }

    public void pullGameRecord(List<StartTimeAndEndTime> timeList) {
        if (CollectionUtils.isEmpty(timeList)) {
            return;
        }
        for (StartTimeAndEndTime startTimeAndEndTime : timeList) {
            Long startTime = startTimeAndEndTime.getStartTime();
            Long endTime = startTimeAndEndTime.getEndTime();
            log.info("开始拉取{}到{}的OB电竞游戏记录", startTime, endTime);
            pullGameRecordByTime(startTime, endTime, 0L);
            log.info("{}到{}的OB电竞游戏记录拉取完成", startTime, endTime);
        }
    }

    public void pullGameRecordByTime(Long startTime, Long endTime, Long lasterOrderId) {
        String result = obdjApi.queryScroll(startTime, endTime, lasterOrderId, 1000, "false");
        if (ObjectUtils.isEmpty(result)) {
            log.error("拉取OB电竞注单时远程请求错误,startTime={},endTime={},lasterOrderId={}", startTime, endTime, lasterOrderId);
            saveGameRecordObEndTime(startTime, endTime, Constants.no);
            return;
        }
        //将下划线转为驼峰
        String jsonResult = underlineToHump(result);
        JSONObject object = JSONObject.parseObject(jsonResult);
        if (PublicObdjApi.STATUS_FALSE.equals(object.getString("status"))) {
            log.error("拉取OB电竞注单时出错,startTime={},endTime={},lasterOrderId={},msg={}", startTime, endTime, lasterOrderId, object.getString("data"));
            saveGameRecordObEndTime(startTime, endTime, Constants.no);
            return;
        }
        //保存数据
        saveAll(object);
        Long lastOrderIDNew = object.getLong("lastOrderID");
        //当前时间范围数据没拉取完继续拉取
        if (!ObjectUtils.isEmpty(lastOrderIDNew) && lastOrderIDNew > 0) {
            pullGameRecordByTime(startTime, endTime, lastOrderIDNew);
        }
        //保存时间区间
        saveGameRecordObEndTime(startTime, endTime, Constants.yes);
    }

    /**
     * 查询时间说明：
     * 1. 只能查询当前时间30天前至当前时间区间
     * 2. 查询截至时间为当前时间5分钟前
     * 3. 每次查询时间区间为30分钟以内
     *
     * @param startTime
     * @return
     * @throws ParseException
     */
    public List<StartTimeAndEndTime> getStartTimeAndEndTime(Long startTime) {
        List<StartTimeAndEndTime> list = new ArrayList<>();
        //查询截至时间为当前时间5分钟前
        Long endTime = System.currentTimeMillis() / 1000 - 5 * 60;
        //第一次拉取数据取当前时间前10分钟为开始时间，之后以上次拉取数据的结束时间为开始时间
        if (ObjectUtils.isEmpty(startTime)) {
            startTime = endTime - 10 * 60;
        }
        if (startTime >= endTime) {
            return list;
        }
        BigDecimal startBig = new BigDecimal(startTime);
        BigDecimal endBig = new BigDecimal(endTime);
        //时间范围重叠2分钟
        int num = endBig.subtract(startBig).divide(new BigDecimal((30 - 2) * 60), 0, BigDecimal.ROUND_UP).intValue();
        //第三方要求拉取数据时间范围不能大于30分钟，大于30分钟，取开始时间的后30分钟为结束时间
        for (int i = 0; i < num; i++) {
            //开始时间前移2分钟，时间范围重叠两分钟
            startTime = startTime - 2 * 60;
            StartTimeAndEndTime startTimeAndEndTime = new StartTimeAndEndTime();
            Long tmpEndTime = startTime + 30 * 60;
            if (tmpEndTime > endTime) {
                tmpEndTime = endTime;
            }
            startTimeAndEndTime.setStartTime(startTime);
            startTimeAndEndTime.setEndTime(tmpEndTime);
            list.add(startTimeAndEndTime);
            startTime = tmpEndTime;
        }
        return list;
    }


    public void saveAll(JSONObject data) {
        String bet = data.getString("bet");
        List<GameRecordObdj> gameRecordList = JSON.parseArray(bet, GameRecordObdj.class);
        log.info("开始处理OB电竞游戏记录数据");
        //查询最小清0打码量
        PlatformConfig platformConfig = platformConfigService.findFirst();
        for (GameRecordObdj gameRecord : gameRecordList) {
            gameRecord.setBetId(gameRecord.getId());
            gameRecord.setId(null);
            try {
                UserThird account = userThirdService.findByObAccount(gameRecord.getMemberAccount());
                if (account == null || account.getUserId() == null) {
                    log.error("同步游戏记录时，UserThird查询结果为null,account={}", gameRecord.getMemberAccount());
                    continue;
                }
                gameRecord.setUserId(account.getUserId());
                BigDecimal validbet = ObjectUtils.isEmpty(gameRecord.getBetAmount()) ? BigDecimal.ZERO : gameRecord.getBetAmount();
                //有效投注额为0不参与洗码,打码,分润,抽點
                if (validbet.compareTo(BigDecimal.ZERO) == 0) {
                    gameRecord.setWashCodeStatus(Constants.yes);
                    gameRecord.setCodeNumStatus(Constants.yes);
                    gameRecord.setShareProfitStatus(Constants.yes);
                    gameRecord.setExtractStatus(Constants.yes);
                    gameRecord.setRebateStatus(Constants.yes);
                }
                //有数据会重复注单id唯一约束会报错，所以一条一条保存，避免影响后面的
                save(gameRecord);
                //保存明细数据
                saveBatchDetail(data, gameRecord.getBetId());
                //保存赛事信息
                saveTournament(data, gameRecord.getTournamentId());
                //计算用户账号实时余额
                changeUserBalance(gameRecord);
                //业务处理
                business(Constants.PLATFORM_OBDJ, gameRecord, platformConfig);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("保存游戏记录时报错,message={},gameRecord={}", e.getMessage(), gameRecord);
            }
        }
    }

    public void business(String platform, GameRecordObdj gameRecordObdj, PlatformConfig platformConfig) {
        GameRecord record = combineGameRecord(gameRecordObdj);
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

    /**
     * 改变用户实时余额
     */
    private void changeUserBalance(GameRecordObdj gameRecordObdj) {
        BigDecimal betAmount = gameRecordObdj.getBetAmount();
        BigDecimal winAmount = gameRecordObdj.getWinAmount();
        if (betAmount == null || winAmount == null) {
            return;
        }
        Long userId = gameRecordObdj.getUserId();
        //下注金额大于0，扣减
        if (betAmount.compareTo(BigDecimal.ZERO) == 1) {
            userMoneyBusiness.subBalance(userId, betAmount);
        }
        //派彩金额大于0，增加
        if (winAmount.compareTo(BigDecimal.ZERO) == 1) {
            userMoneyBusiness.addBalance(userId, winAmount);
        }
    }

    public GameRecordObdj save(GameRecordObdj gameRecord) {
        SimpleDateFormat format = DateUtil.getSimpleDateFormat();
        //投注时间是毫秒
        if (!ObjectUtils.isEmpty(gameRecord.getBetTime())) {
            gameRecord.setBetStrTime(format.format(gameRecord.getBetTime()));
        }
        //结算时间是秒
        if (!ObjectUtils.isEmpty(gameRecord.getSettleTime())) {
            gameRecord.setSetStrTime(format.format(gameRecord.getSettleTime()));
        }
        User user = userService.findById(gameRecord.getUserId());
        if (user != null) {
            gameRecord.setFirstProxy(user.getFirstProxy());
            gameRecord.setSecondProxy(user.getSecondProxy());
            gameRecord.setThirdProxy(user.getThirdProxy());
        }
        GameRecordObdj record = gameRecordObdjService.save(gameRecord);
        return record;
    }

    private void saveBatchDetail(JSONObject data, Long betId) {
        if (ObjectUtils.isEmpty(betId)) {
            return;
        }
        String detailStr = data.getString("detail");
        if (ObjectUtils.isEmpty(detailStr)) {
            return;
        }
        JSONObject jsonObject = JSONObject.parseObject(detailStr);
        if (ObjectUtils.isEmpty(jsonObject)) {
            return;
        }
        String datailList = jsonObject.getString(betId.toString());
        if (ObjectUtils.isEmpty(datailList)) {
            return;
        }
        List<GameRecordObdjDetail> gameRecordList = JSON.parseArray(datailList, GameRecordObdjDetail.class);
        if (CollectionUtils.isEmpty(gameRecordList)) {
            return;
        }
        for (GameRecordObdjDetail detail : gameRecordList) {
            try {
                detail.setBetDetailId(detail.getId());
                detail.setId(null);
                gameRecordObdjDetailService.save(detail);
            } catch (Exception e) {
                log.error("保存OB电竞注单明细时出错，msg={},GameRecordObdjDetail={}", e.getMessage(), detail.toString());
            }
        }
    }

    private void saveTournament(JSONObject data, Long tournamentId) {
        if (ObjectUtils.isEmpty(tournamentId)) {
            return;
        }
        String tournamentStr = data.getString("tournament");
        if (ObjectUtils.isEmpty(tournamentStr)) {
            return;
        }
        JSONObject jsonObject = JSONObject.parseObject(tournamentStr);
        String tournamentName = jsonObject.getString(tournamentId.toString());
        GameRecordObdjTournament gameRecordObdjTournament = new GameRecordObdjTournament();
        gameRecordObdjTournament.setTournamentId(tournamentId);
        gameRecordObdjTournament.setTournamentName(tournamentName);
        gameRecordObdjTournamentService.save(gameRecordObdjTournament);
    }

    private GameRecord combineGameRecord(GameRecordObdj item) {
        GameRecord gameRecord = new GameRecord();
        gameRecord.setBetId(item.getBetId().toString());
        gameRecord.setValidbet(item.getBetAmount().toString());
        gameRecord.setUserId(item.getUserId());
        if (!ObjectUtils.isEmpty(item.getGameId())) {
            gameRecord.setGameCode(item.getGameId().toString());
        }
        gameRecord.setGname("OB电竞");
        gameRecord.setBetTime(item.getBetStrTime());
        gameRecord.setId(item.getId());
        gameRecord.setFirstProxy(item.getFirstProxy());
        gameRecord.setSecondProxy(item.getSecondProxy());
        gameRecord.setThirdProxy(item.getThirdProxy());
        if (!ObjectUtils.isEmpty(item.getBetAmount())) {
            gameRecord.setBet(item.getBetAmount().toString());
        }
        if (item.getWinAmount() != null && item.getBetAmount() != null) {
            BigDecimal winLoss = item.getWinAmount().subtract(item.getBetAmount());
            gameRecord.setWinLoss(winLoss.toString());
        }
        return gameRecord;
    }

    private void saveGameRecordObEndTime(Long startTime, Long endTime, Integer status) {
        GameRecordObEndTime gameRecordObEndTime = new GameRecordObEndTime();
        gameRecordObEndTime.setStartTime(startTime);
        gameRecordObEndTime.setEndTime(endTime);
        gameRecordObEndTime.setStatus(status);
        gameRecordObEndTime.setVendorCode(Constants.PLATFORM_OBDJ);
        gameRecordObEndTimeService.save(gameRecordObEndTime);
    }

    /**
     * 下划线转驼峰
     *
     * @param str
     * @return
     */
    public String underlineToHump(String str) {
        //正则匹配下划线及后一个字符，删除下划线并将匹配的字符转成大写
        Matcher matcher = Pattern.compile("_([a-z])").matcher(str);
        StringBuffer sb = new StringBuffer(str);
        if (matcher.find()) {
            sb = new StringBuffer();
            //将当前匹配的子串替换成指定字符串，并且将替换后的子串及之前到上次匹配的子串之后的字符串添加到StringBuffer对象中
            //正则之前的字符和被替换的字符
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
            //把之后的字符串也添加到StringBuffer对象中
            matcher.appendTail(sb);
        } else {
            //去除除字母之外的前面带的下划线
            return sb.toString().replaceAll("_", "");
        }
        return underlineToHump(sb.toString());
    }

    @Data
    public static class StartTimeAndEndTime {
        private Long startTime;
        private Long endTime;
    }
}
