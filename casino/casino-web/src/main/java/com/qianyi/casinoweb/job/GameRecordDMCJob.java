package com.qianyi.casinoweb.job;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.DMCTradeReportVo;
import com.qianyi.casinocore.vo.TicketSlaves;
import com.qianyi.lottery.api.LotteryDmcApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class GameRecordDMCJob {


    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private LotteryDmcApi publicLottoApi;
    @Autowired
    private GameRecordDMCService gameRecordDMCService;
    @Autowired
    private GameRecordDMCEndTimeService gameRecordDMCEndTimeService;
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

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public static final long ORDER_RXPIRE_TIME = 7 * 24l;

    //每隔2分钟执行一次
    @Scheduled(cron = "50 0/2 * * * ?")
    public void pullGameRecord() {
        PlatformGame platformGame = platformGameService.findByGamePlatformName(Constants.PLATFORM_DMC);
        //平台关闭，但是拉单还是要继续进行
        if (platformGame != null && platformGame.getGameStatus() == 2) {
            log.info("后台已关闭DMC平台,无需拉单,platformGame={}", platformGame);
            return;
        }
        AdGame adGame = adGamesService.findByGamePlatformNameAndGameCode(Constants.PLATFORM_DMC, Constants.PLATFORM_DMC);
        if (adGame != null && adGame.getGamesStatus() == 2) {
            log.info("后台已关闭DMC,无需拉单,adGame={}", adGame);
        } else {
            log.info("定时器开始拉取DMC电竞注单记录");
            GameRecordDMCEndTime gameRecordDMCEndTime = gameRecordDMCEndTimeService.findFirstByPlatformAndStatusOrderByEndTimeDesc(Constants.PLATFORM_DMC, Constants.yes);
            String time = gameRecordDMCEndTime == null ? null : gameRecordDMCEndTime.getEndTime();
            //获取查询游戏记录的时间范围
            String startTime = getStartTime(time, Constants.PLATFORM_DMC);
            pullGameRecord(startTime, Constants.PLATFORM_DMC);
            log.info("定时器拉取完成VNC注单记录");

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

        //注单数据结果
        String resultData = publicLottoApi.getDateTimeReport(startTime, endTime);
        if (StringUtils.isBlank(resultData)) {
            log.info("拉取{}注单当前时间无记录,startTime={},result={}", platform, startTime, resultData);
            saveGameRecordDMCEndTime(startTime, endTime, platform, Constants.yes);
            return;
        }
        List<DMCTradeReportVo> gameRecords = JSON.parseArray(resultData, DMCTradeReportVo.class);
        if (gameRecords.isEmpty()) {
            log.info("拉取{}注单当前时间无记录,startTime={},result={}", platform, startTime, resultData);
            saveGameRecordDMCEndTime(startTime, endTime, platform, Constants.yes);
            return;
        }
        //保存数据
        saveAll(platform, gameRecords);
        //保存时间区间
        saveGameRecordDMCEndTime(startTime, endTime, platform, Constants.yes);
    }


    private void saveGameRecordDMCEndTime(String startTime, String endTime, String platform, Integer status) {
        GameRecordDMCEndTime gameRecordDMCEndTime = new GameRecordDMCEndTime();
        gameRecordDMCEndTime.setStartTime(startTime);
        gameRecordDMCEndTime.setEndTime(endTime);
        gameRecordDMCEndTime.setStatus(status);
        gameRecordDMCEndTime.setPlatform(platform);
        gameRecordDMCEndTimeService.save(gameRecordDMCEndTime);
    }


    public void saveAll(String platform, List<DMCTradeReportVo> gameRecordDMCVoList) {
        if (CollectionUtils.isEmpty(gameRecordDMCVoList)) {
            return;
        }
        log.info("开始处理{}游戏记录数据", platform);
        //查询最小清0打码量
        PlatformConfig platformConfig = platformConfigService.findFirst();
        for (DMCTradeReportVo gameRecordDMCVo : gameRecordDMCVoList) {

            if(!StringUtils.equals(gameRecordDMCVo.getTicket_status(), "SETTLED")){
                continue;
            }
            //封装数据
            GameParentRecordDMC gameParentRecordDMC = getGameParentRecordDMC(gameRecordDMCVo);
            //判断注单是否已经入库
            String redisKey = "DMC:" + gameParentRecordDMC.getTicketNo();
            int existNum = existBetRecord(gameParentRecordDMC, redisKey);
//            if(existNum == 0){//无变化
//                continue;
//            }
            boolean resultFlag = false;
            GameRecordDMC gameRecord;
            try {
                List<TicketSlaves> ticketSlavesList = JSON.parseArray(gameRecordDMCVo.getTicket_slaves(), TicketSlaves.class);
                for (TicketSlaves ticketSlaves : ticketSlavesList) {
                    gameRecord = save(gameRecordDMCVo, ticketSlaves);
                    if (gameRecord == null) {
                        continue;
                    }
                    //业务处理
                    business(Constants.PLATFORM_DMC, gameRecord, platformConfig);
                }
                resultFlag = true;
            } catch (Exception e) {
                e.printStackTrace();
                log.error("保存{}游戏记录时报错,message={}", platform, e.getMessage());
            }finally {
                if(resultFlag){
                    ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
                    opsForValue.set(redisKey, gameParentRecordDMC.getMd5(), ORDER_RXPIRE_TIME, TimeUnit.HOURS);
                    log.info("set gameParentRecordDMC bill no to redisKey：【{}】", redisKey);
                }
            }
        }
    }

    private GameParentRecordDMC getGameParentRecordDMC(DMCTradeReportVo gameRecordDMCVo) {
        GameParentRecordDMC recordDMC = new GameParentRecordDMC();
        recordDMC.setMemberId(gameRecordDMCVo.getMember_id());
        recordDMC.setMerchantId(gameRecordDMCVo.getMerchant_id());
        recordDMC.setBetNumber(gameRecordDMCVo.getBet_number());
        recordDMC.setGamePlayId(gameRecordDMCVo.getGame_play_id());
        recordDMC.setBetType(gameRecordDMCVo.getBet_type());
        recordDMC.setTotalAmount(gameRecordDMCVo.getTotal_amount());
        recordDMC.setNetAmount(gameRecordDMCVo.getNet_amount());
        recordDMC.setRebateAmount(gameRecordDMCVo.getWinning_amount());
        recordDMC.setRebatePercentage(gameRecordDMCVo.getRebate_percentage());
        recordDMC.setBettingDate(gameRecordDMCVo.getBetting_date());
        recordDMC.setDrawDate(gameRecordDMCVo.getDraw_date());
        recordDMC.setDrawNumber(gameRecordDMCVo.getDraw_number());
        recordDMC.setTicketStatus(gameRecordDMCVo.getTicket_status());
        recordDMC.setProgressStatus(gameRecordDMCVo.getProgress_status());
        recordDMC.setCreatedAt(recordDMC.getCreatedAt());
        recordDMC.setUpdatedAt(gameRecordDMCVo.getUpdated_at());
        recordDMC.setCustomerId(gameRecordDMCVo.getCustomer_id());
        recordDMC.setCustomerName(gameRecordDMCVo.getCustomer_name());
        recordDMC.setTicketNo(gameRecordDMCVo.getTicket_no());
        return recordDMC;
    }

    private int existBetRecord(GameParentRecordDMC recordDMC, String redisKey){
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
        String rowMd5 = opsForValue.get(redisKey);
        if(StringUtils.isBlank(rowMd5)){
            return -1; //不存在
        }
        if(!rowMd5.equalsIgnoreCase(recordDMC.getMd5())){//有变化
            return -2; //有变化
        }
        return 0; //无变化
    }


    @SneakyThrows
    public GameRecordDMC save(DMCTradeReportVo gameRecordDMCVo, TicketSlaves ticketSlaves) {
        GameRecordDMC gameRecordDMC = new GameRecordDMC();
        gameRecordDMC.setUserName(gameRecordDMCVo.getCustomer_name());
        gameRecordDMC.setCustomerId(gameRecordDMCVo.getCustomer_id());
        gameRecordDMC.setBetOrderNo(ticketSlaves.getChild_ticket_no());
        gameRecordDMC.setParentBetOrderNo(gameRecordDMCVo.getTicket_no());
        gameRecordDMC.setBetTime(ticketSlaves.getCreated_at());
        gameRecordDMC.setSettleTime(gameRecordDMCVo.getUpdated_at());
        gameRecordDMC.setDrawDate(gameRecordDMCVo.getDraw_date());
        gameRecordDMC.setBetType(gameRecordDMCVo.getBet_type() + "");
        gameRecordDMC.setBackWaterMoney(new BigDecimal(ObjectUtil.isNull(ticketSlaves.getBig_bet_amount()) ? "0" : ticketSlaves.getBig_bet_amount()));
        gameRecordDMC.setBigBetAmount(new BigDecimal(ObjectUtil.isNull(ticketSlaves.getBig_bet_amount()) ? "0" : ticketSlaves.getBig_bet_amount()));
        gameRecordDMC.setSmallBetAmount(new BigDecimal(ObjectUtil.isNull(ticketSlaves.getSmall_bet_amount()) ? "0" : ticketSlaves.getSmall_bet_amount()));
        gameRecordDMC.setSlaveAmount(ObjectUtil.isNull(ticketSlaves.getBet_amount()) ? BigDecimal.ZERO : ticketSlaves.getBet_amount());
        gameRecordDMC.setWinMoney(ObjectUtil.isNull(ticketSlaves.getWinning_amount()) ? BigDecimal.ZERO : ticketSlaves.getWinning_amount());
        gameRecordDMC.setPrizeType(ticketSlaves.getPrize_type());
        gameRecordDMC.setLotteryNumber(ticketSlaves.getLottery_number());
        gameRecordDMC.setCurrencyCode(gameRecordDMCVo.getCurrency_code());
        gameRecordDMC.setBetMoney(ticketSlaves.getBet_amount());
        gameRecordDMC.setGameName(GAME_NAME.get(ticketSlaves.getGame_play_id()));
        gameRecordDMC.setRealMoney(ticketSlaves.getBet_net_amount());
        UserThird account = userThirdService.findByDmcAccount(gameRecordDMC.getUserName());
        if (account == null || account.getUserId() == null) {
            log.error("同步游戏记录时，UserThird查询结果为null,account={}", gameRecordDMC.getUserName());
            return null;
        }
        GameRecordDMC gameRecord = gameRecordDMCService.findByEnterpriseIdAndBetOrderNo(gameRecordDMC.getEnterpriseId(), gameRecordDMC.getBetOrderNo());
        if (gameRecord == null) {
            gameRecord = new GameRecordDMC();
            gameRecordDMC.setIsAdd(1);//新增
        }
        BeanUtils.copyProperties(gameRecordDMC, gameRecord);
        gameRecord.setUserId(account.getUserId());
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
        GameRecordDMC record = gameRecordDMCService.save(gameRecord);
        gameRecord.setId(record.getId());
        return gameRecord;
    }

    public static final Map<String, String> GAME_NAME = ImmutableMap.<String, String>builder()
            .put("1","Magnum")
            .put("2","Damacai")
            .put("3","Toto")
            .build();

    public void business(String platform, GameRecordDMC gameRecordDMC, PlatformConfig platformConfig) {
        //计算用户账号实时余额
        Integer isAdd = gameRecordDMC.getIsAdd();
        if (isAdd == 1) {
            gameRecordAsyncOper.changeUserBalance(gameRecordDMC.getUserId(), gameRecordDMC.getBetMoney(), gameRecordDMC.getWinMoney());
        }
        //组装gameRecord
        GameRecord record = combineGameRecord(gameRecordDMC);
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

    public GameRecord combineGameRecord(GameRecordDMC item) {
        GameRecord gameRecord = new GameRecord();
        gameRecord.setBetId(item.getBetOrderNo());
        gameRecord.setUserId(item.getUserId());
        gameRecord.setGameCode("DMC");
        gameRecord.setGname(Constants.PLATFORM_DMC);
        gameRecord.setBetTime(item.getBetTime());
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

    @Data
    public static class StartTimeAndEndTime {
        private Long startTime;
        private Long endTime;
    }

}
