package com.qianyi.casinoweb.job;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.business.UserMoneyBusiness;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinoweb.vo.GameRecordObtyDataVo;
import com.qianyi.casinoweb.vo.GameRecordObtyVo;
import com.qianyi.liveob.api.PublicObtyApi;
import com.qianyi.liveob.api.PublicObzrApi;
import com.qianyi.liveob.dto.GameRecordQueryRespDTO;
import com.qianyi.liveob.dto.PageRespDTO;
import com.qianyi.liveob.dto.PullMerchantDto;
import com.qianyi.liveob.dto.ResultDTO;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.Data;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


@Component
@Slf4j
public class GameRecordObzrJob {

    @Autowired
    private PublicObzrApi publicObzrApi;
    @Autowired
    private GameRecordObzrService gameRecordObzrService;
    @Autowired
    private GameRecordObzrTimeService gameRecordObzrTimeService;
    @Autowired
    private UserThirdService userThirdService;
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


    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern(DateUtil.patten);


    //每隔7分钟执行一次
//    @Scheduled(fixedDelay = 500000)
    public void pullGameRecord() {
        PlatformGame platformGame = platformGameService.findByGamePlatformName(Constants.PLATFORM_OB);
        if (platformGame != null && platformGame.getGameStatus() == 2) {
            log.info("后台已关闭OB平台,无需拉单,platformGame={}", platformGame);
            return;
        }
        AdGame adGame = adGamesService.findByGamePlatformNameAndGameCode(Constants.PLATFORM_OB, Constants.PLATFORM_OBZR);
        if (adGame != null && adGame.getGamesStatus() == 2) {
            log.info("后台已关闭OB真人,无需拉单,adGame={}", adGame);
            return;
        }
        log.info("定时器开始拉取OB真人注单记录");
        String endTime = gameRecordObzrTimeService.findLastEndTime();
        pullGameRecord(endTime);
        log.info("定时器拉取完成OB真人注单记录");
    }

    public void pullGameRecord(String lastTime) {
        // 每次只粒取30分钟的闻磨(根据年小时单量情况来定，如果是并单可以调整到30分钟一次，如果是正常拉单没有必要)
        int mins = +5;
        int startTimePlusSeconds = -40;
        int endTimePlusSeconds = -40;
        if(StrUtil.isBlank(lastTime)){
            lastTime = DateUtil.dateToPatten(new Date());
        }
        LocalDateTime lastEndTime = LocalDateTime.parse(lastTime, DATETIME_FORMAT);
        LocalDateTime startTime = lastEndTime.plusSeconds(startTimePlusSeconds);
        LocalDateTime endTime = startTime.plusMinutes(mins);

        LocalDateTime now = LocalDateTime.now().plusSeconds(endTimePlusSeconds);
        if (endTime.isAfter(now)) {
            endTime = now;
        }
        boolean flag = true;
//        String start = "2022-12-23 20:39:10";
//        String end = "2022-12-23 21:05:10";
        String start = startTime.format(DATETIME_FORMAT);
        String end = endTime.format(DATETIME_FORMAT);
        int pageIndex = 1;

        long s = System.currentTimeMillis();
        ResultDTO resultDTO = publicObzrApi.betHistoryRecord(start, end, pageIndex);
        if (ObjectUtil.isNull(resultDTO)) {
            return;
        }
        long e = System.currentTimeMillis();
        if (resultDTO.getCode().equals("200")) {
            PageRespDTO data = resultDTO.getData();
            if (data.getTotalRecord() > 0) {
                List<GameRecordQueryRespDTO> list = data.getRecord();
                //保存数据
                saveAll(list);
                log.info("商户第{}页/{}页，已入库，条数：{}，拉单时间：{}", pageIndex, data.getTotalPage(), list.size(), e - s);
            } else {
                log.info("商户没有获取到数据,但需要更新时间戳");
            }
            if (data.getTotalPage() > 1) {
                for (int i = 2; i < data.getTotalPage(); i++) {
                    long s1 = System.currentTimeMillis();
                    ResultDTO resultDTO2 = publicObzrApi.betHistoryRecord(start, end, i);
                    long e1 = System.currentTimeMillis();
                    if (resultDTO2.getCode().equals("200")) {
                        data = resultDTO2.getData();
                        if (data.getTotalRecord() > 0) {
                            List<GameRecordQueryRespDTO> list = data.getRecord();
                            //保存数据
                            saveAll(list);
                            log.info("商户第{}页/{}页，已入库，条数：{}，拉单时间：{}", i, data.getTotalPage(), list.size(), e1 - s1);
                        } else {
                            log.info("商户没有获取到数据，但需要更新时间戳");
                        }
                    } else {
                        // 注意 ，异常情况，不用更新时间戳
                        flag = false;
                        break;
                    }
                }
            }
        } else {
            // 注意 ，异常情况，不用更新时间戳
            flag = false;
        }
        if (flag == true) {
            log.info("商户已经更新最后时间戳：{}", "");
            gameRecordObzrTimeService.save(DATETIME_FORMAT.format(endTime));
        }

    }


    public void saveAll(List<GameRecordQueryRespDTO> gameRecordList) {
        if (CollectionUtils.isEmpty(gameRecordList)) {
            return;
        }
        log.info("开始处理OB真人游戏记录数据");
        //查询最小清0打码量
        PlatformConfig platformConfig = platformConfigService.findFirst();
        for (GameRecordQueryRespDTO gameRecordQueryRespDTO : gameRecordList) {
            GameRecordObzr gameRecord = null;
            try {
                gameRecord = save(gameRecordQueryRespDTO);
                if (gameRecord == null) {
                    continue;
                }
                //业务处理
                business(Constants.PLATFORM_OBZR, gameRecord, platformConfig);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("保存OB真人游戏记录时报错,message={}", e.getMessage());
            }
        }
    }

    public void business(String platform, GameRecordObzr gameRecordObzr, PlatformConfig platformConfig) {
        //计算用户账号实时余额
        Integer isAdd = gameRecordObzr.getIsAdd();
        if (isAdd == 1) {
            gameRecordAsyncOper.changeUserBalance(gameRecordObzr.getUserId(), gameRecordObzr.getBetAmount(), gameRecordObzr.getNetAmount());
        }
        //组装gameRecord
        GameRecord record = combineGameRecord(gameRecordObzr);
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
        //等级流水
        gameRecordAsyncOper.levelWater(platform, record);
    }


    public GameRecordObzr save(GameRecordQueryRespDTO gameRecordQueryRespDTO) {
        UserThird account = userThirdService.findByObtyAccount(gameRecordQueryRespDTO.getPlayerName());
        if (account == null || account.getUserId() == null) {
            log.error("同步游戏记录时，UserThird查询结果为null,account={}", gameRecordQueryRespDTO.getPlayerName());
            return null;
        }
        gameRecordQueryRespDTO.setOrderNo(gameRecordQueryRespDTO.getId()+"");
        GameRecordObzr gameRecord = gameRecordObzrService.findByBetOrderNo(gameRecordQueryRespDTO.getOrderNo());
        if (gameRecord == null) {
            gameRecord = new GameRecordObzr();
            gameRecord.setIsAdd(1);//新增
        }
        //有效投注
        BigDecimal oldTurnover = gameRecordQueryRespDTO.getValidBetAmount();
        //用户输赢
        BigDecimal oldRealWinAmount = gameRecordQueryRespDTO.getNetAmount();

        gameRecordQueryRespDTO.setBetTime(gameRecordQueryRespDTO.getCreatedAt());
        SimpleDateFormat format = DateUtil.getSimpleDateFormat();
        //投注时间转yyyy-MM-dd
        if (!ObjectUtils.isEmpty(gameRecordQueryRespDTO.getBetTime()) && gameRecordQueryRespDTO.getBetTime() != 0) {
            gameRecordQueryRespDTO.setBetStrTime(format.format(gameRecordQueryRespDTO.getBetTime()));
        }
        gameRecordQueryRespDTO.setSettleTime(gameRecordQueryRespDTO.getNetAt());
        //结算时间转yyyy-MM-dd
        if (!ObjectUtils.isEmpty(gameRecordQueryRespDTO.getSettleTime()) && gameRecordQueryRespDTO.getSettleTime() != 0) {
            gameRecordQueryRespDTO.setSettleStrTime(format.format(gameRecordQueryRespDTO.getSettleTime()));
        }
        BeanUtils.copyProperties(gameRecordQueryRespDTO, gameRecord);
        gameRecord.setUserId(account.getUserId());
        BigDecimal validbet = ObjectUtils.isEmpty(gameRecord.getValidBetAmount()) ? BigDecimal.ZERO : gameRecord.getValidBetAmount();
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
        GameRecordObzr record = gameRecordObzrService.save(gameRecord);
        //旧输赢和有效投注差值
        record.setOldTurnover(oldTurnover);
        record.setOldRealWinAmount(oldRealWinAmount);
        return record;
    }


    private GameRecord combineGameRecord(GameRecordObzr item) {
        GameRecord gameRecord = new GameRecord();
        gameRecord.setBetId(item.getOrderNo());
        gameRecord.setValidbet(item.getValidBetAmount().toString());
        gameRecord.setUserId(item.getUserId());
        gameRecord.setGameCode(Constants.PLATFORM_OBZR);
        gameRecord.setGname("OB真人");
        gameRecord.setBetTime(item.getBetStrTime());
        gameRecord.setId(item.getId());
        gameRecord.setFirstProxy(item.getFirstProxy());
        gameRecord.setSecondProxy(item.getSecondProxy());
        gameRecord.setThirdProxy(item.getThirdProxy());
        if (!ObjectUtils.isEmpty(item.getBetAmount())) {
            gameRecord.setBet(item.getBetAmount().toString());
        }
        if (item.getNetAmount() != null) {
            gameRecord.setWinLoss(item.getNetAmount().toString());
        }
        return gameRecord;
    }


}