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

import javax.annotation.Resource;
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
    @Resource
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
    @Scheduled(fixedDelay = 500)
    public void pullGameRecord() {
        log.info("开始处理OB真人游戏记录数据");
        //查询最小清0打码量
        PlatformConfig platformConfig = platformConfigService.findFirst();

        GameRecordQueryRespDTO   gameRecordQueryRespDTO  = JSONObject.parseObject("{\"adddec1\":1.0,\"adddec2\":0.0,\"adddec3\":0.0,\"addstr1\":\"庄:♠A♠4;闲:♣K♥3♠K;\",\"addstr2\":\"庄:5;闲:3;\",\"agentCode\":\"65Q25\",\"agentId\":6293,\"agentName\":\"kkcasinoob\",\"beforeAmount\":447.7143,\"betAmount\":2.8571,\"betFlag\":0,\"betPointId\":3001,\"betPointName\":\"庄\",\"betStatus\":1,\"bootNo\":\"B0C1222C31026P-GP299\",\"createdAt\":1672497944000,\"currency\":\"USD\",\"dealerName\":\"Arvi1\",\"deviceId\":\"1672495466033646582\",\"deviceType\":1,\"gameMode\":1,\"gameTypeId\":2002,\"gameTypeName\":\"极速百家乐\",\"id\":817181046262333441,\"judgeResult\":\"3:15;49:10:51\",\"loginIp\":\"103.139.16.132\",\"netAmount\":2.7143,\"netAt\":1672497967000,\"nickName\":\"PLAYER97113741312\",\"payAmount\":5.5714,\"platformId\":3,\"platformName\":\"亚太厅\",\"playerId\":31751502,\"playerName\":\"65q25_ceshigezi\",\"recalcuAt\":0,\"recordType\":1,\"roundNo\":\"GC1222C3192B\",\"signature\":\"-\",\"startid\":817181144558178304,\"tableCode\":\"C12\",\"tableName\":\"极速C12\",\"updatedAt\":1672497967000,\"validBetAmount\":2.7143}",GameRecordQueryRespDTO.class);
        GameRecordObzr gameRecord = null;
        try {
            gameRecord = save(gameRecordQueryRespDTO);
            if (gameRecord == null) {
                return;
            }
            //业务处理
            business(Constants.PLATFORM_OBZR, gameRecord, platformConfig);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("保存OB真人游戏记录时报错,message={}", e.getMessage());
        }
    }

    public void pullGameRecord(String lastTime) {
        lastTime  = "2022-12-31 22:18:20";
        // 每次只粒取30分钟的闻磨(根据年小时单量情况来定，如果是并单可以调整到30分钟一次，如果是正常拉单没有必要)
        int mins = +5;
        int startTimePlusSeconds = -40;
        int endTimePlusSeconds = -40;
        if (StrUtil.isBlank(lastTime)) {
            lastTime = DateUtil.dateToPatten(new Date());
        }
        // 获取最后更新时间
        LocalDateTime lastEndTime = LocalDateTime.parse(lastTime, DATETIME_FORMAT);
        // 开始时间
        LocalDateTime startTime = lastEndTime.plusSeconds(startTimePlusSeconds);
        // 结束时间
        LocalDateTime endTime = startTime.plusMinutes(mins);

        LocalDateTime now = LocalDateTime.now().plusSeconds(endTimePlusSeconds);
        if (endTime.isAfter(now)) {
            endTime = now;
        }
        boolean flag = true;
//        String start = "2022-12-26 19:25:53";
//        String end = "2022-12-26 19:55:53";
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
        if (isAdd == 1 || (isAdd == 0 && (BigDecimal.ZERO.compareTo(new BigDecimal(record.getValidbet())) != 0 || BigDecimal.ZERO.compareTo(new BigDecimal(record.getBet())) != 0 || BigDecimal.ZERO.compareTo(new BigDecimal(record.getWinLoss())) != 0))) {
            gameRecordAsyncOper.proxyGameRecordReport(platform, record);
        }
        String validbet = record.getValidbet();
        if (ObjectUtils.isEmpty(validbet) || new BigDecimal(validbet).compareTo(BigDecimal.ZERO) == 0) {
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


    public GameRecordObzr save(GameRecordQueryRespDTO gameRecordQueryRespDTO) {
        UserThird account = userThirdService.findByObzrAccount(gameRecordQueryRespDTO.getPlayerName());
        if (account == null || account.getUserId() == null) {
            log.error("同步游戏记录时，UserThird查询结果为null,account={}", gameRecordQueryRespDTO.getPlayerName());
            return null;
        }
        gameRecordQueryRespDTO.setOrderNo(gameRecordQueryRespDTO.getId() + "");
        GameRecordObzr gameRecord = gameRecordObzrService.findByBetOrderNo(gameRecordQueryRespDTO.getOrderNo());
        if (gameRecord == null) {
            gameRecord = new GameRecordObzr();
            gameRecord.setIsAdd(1);//新增
        }
        Long gameRecordId = gameRecord.getId();
        int isAdd = gameRecord.getIsAdd();
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
        gameRecord.setPayoutAmount(gameRecord.getPayAmount());
        gameRecord.setId(gameRecordId);
        gameRecord.setUserId(account.getUserId());
        gameRecord.setIsAdd(isAdd);
        gameRecord.setBetTime(new Date(gameRecordQueryRespDTO.getBetTime()));
        gameRecord.setSettleTime(new Date(gameRecordQueryRespDTO.getSettleTime()));
        gameRecord.setRecalcuAt(new Date(gameRecordQueryRespDTO.getRecalcuAt()));

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