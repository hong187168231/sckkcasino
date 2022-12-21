package com.qianyi.casinoweb.job;

import cn.hutool.core.util.ObjectUtil;
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
import java.util.LinkedList;
import java.util.List;

/**
 * 查询用户投注记录接口,查询最近一周的数据,每次最大100条
 */
@Component
@Slf4j
public class GameRecordObzrJob {

    @Autowired
    private PublicObzrApi publicObzrApi;
    @Autowired
    private GameRecordObtyService gameRecordObtyService;
    @Autowired
    private GameRecordObzrTimeService gameRecordObzrTimeService;
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

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

    //每隔7分钟执行一次
    @Scheduled(cron = "0 0/7 * * * ?")
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
        int mins = +30;
        int startTimePlusSeconds = -40;
        int endTimePlusSeconds = -40;

        LocalDateTime lastEndTime = LocalDateTime.parse(lastTime, DATETIME_FORMAT);
        LocalDateTime startTime = lastEndTime.plusSeconds(startTimePlusSeconds);
        LocalDateTime endTime = startTime.plusMinutes(mins);

        LocalDateTime now = LocalDateTime.now().plusSeconds(endTimePlusSeconds);
        if (endTime.isAfter(now)) {
            endTime = now;
        }

        boolean flag = true;
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
                // todo ruku
                int count = 0;
                log.info("商户第{}页/{}页，已入库，条数：{}，拉单时间：{}", pageIndex, data.getTotalPage(), count, e - s);
            } else {

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
                            // todo ruku
                            // 全量replace into到库
                            int count = 0;
                            log.info("商户第{}页/{}页，已入库，条数：{}，拉单时间：{}", i, data.getTotalPage(), count, e1 - s1);
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


    public void saveAll(List<GameRecordObtyVo> gameRecordList) {
        if (CollectionUtils.isEmpty(gameRecordList)) {
            return;
        }
        log.info("开始处理OB真人游戏记录数据");
        //查询最小清0打码量
        PlatformConfig platformConfig = platformConfigService.findFirst();
        for (GameRecordObtyVo gameRecordObtyVo : gameRecordList) {
            GameRecordObty gameRecord = null;
            try {
                gameRecord = save(gameRecordObtyVo);
                if (gameRecord == null) {
                    continue;
                }
                //业务处理
                business(Constants.PLATFORM_OBZR, gameRecord, platformConfig);
                //保存明细数据
                saveBatchDetail(gameRecordObtyVo.getDetailList());
            } catch (Exception e) {
                e.printStackTrace();
                log.error("保存OB真人游戏记录时报错,message={}", e.getMessage());
            }
        }
    }

    public void business(String platform, GameRecordObty gameRecordObty, PlatformConfig platformConfig) {
        //计算用户账号实时余额
        changeUserBalance(gameRecordObty);
        //组装gameRecord
        GameRecord record = combineGameRecord(gameRecordObty);
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

    /**
     * 改变用户实时余额
     */
    private void changeUserBalance(GameRecordObty gameRecordObty) {
        try {
            BigDecimal betAmount = gameRecordObty.getOrderAmount();
            BigDecimal winAmount = gameRecordObty.getProfitAmount();
            if (betAmount == null || winAmount == null) {
                return;
            }
            Long userId = gameRecordObty.getUserId();
            //下注金额大于0，扣减
            if (betAmount.compareTo(BigDecimal.ZERO) == 1) {
                userMoneyBusiness.subBalance(userId, betAmount);
            }
            //派彩金额大于0，增加
            if (winAmount.compareTo(BigDecimal.ZERO) == 1) {
                userMoneyBusiness.addBalance(userId, winAmount);
            }
        } catch (Exception e) {
            log.error("改变用户实时余额时报错，msg={}", e.getMessage());
        }
    }

    public GameRecordObty save(GameRecordObtyVo gameRecordObtyVo) {
        UserThird account = userThirdService.findByObtyAccount(gameRecordObtyVo.getUserName());
        if (account == null || account.getUserId() == null) {
            log.error("同步游戏记录时，UserThird查询结果为null,account={}", gameRecordObtyVo.getUserName());
            return null;
        }
        gameRecordObtyVo.setBetTime(gameRecordObtyVo.getCreateTime());
        SimpleDateFormat format = DateUtil.getSimpleDateFormat();
        //投注时间转yyyy-MM-dd
        if (!ObjectUtils.isEmpty(gameRecordObtyVo.getBetTime()) && gameRecordObtyVo.getBetTime() != 0) {
            gameRecordObtyVo.setBetStrTime(format.format(gameRecordObtyVo.getBetTime()));
        }
        //结算时间转yyyy-MM-dd
        if (!ObjectUtils.isEmpty(gameRecordObtyVo.getSettleTime()) && gameRecordObtyVo.getSettleTime() != 0) {
            gameRecordObtyVo.setSettleStrTime(format.format(gameRecordObtyVo.getSettleTime()));
        }
        GameRecordObty gameRecord = new GameRecordObty();
        BeanUtils.copyProperties(gameRecordObtyVo, gameRecord);
        gameRecord.setUserId(account.getUserId());
        BigDecimal validbet = ObjectUtils.isEmpty(gameRecord.getOrderAmount()) ? BigDecimal.ZERO : gameRecord.getOrderAmount();
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

        GameRecordObty record = gameRecordObtyService.save(gameRecord);
        return record;
    }

    private void saveBatchDetail(List<GameRecordObtyDetail> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        for (GameRecordObtyDetail detail : detailList) {
            try {
                gameRecordObtyDetailService.save(detail);
            } catch (DataIntegrityViolationException e) {
                log.info("保存OB真人游戏记录时报错GameRecordObtyDetail唯一索引异常，GameRecordObtyDetail={}", detail.toString());
            } catch (Exception e) {
                log.error("保存OB真人注单明细时出错，msg={},GameRecordObtyDetail={}", e.getMessage(), detail.toString());
            }
        }
    }

    private GameRecord combineGameRecord(GameRecordObty item) {
        GameRecord gameRecord = new GameRecord();
        gameRecord.setBetId(item.getOrderNo());
        gameRecord.setValidbet(item.getOrderAmount().toString());
        gameRecord.setUserId(item.getUserId());
        gameRecord.setGameCode(Constants.PLATFORM_OBTY);
        gameRecord.setGname("OB真人");
        gameRecord.setBetTime(item.getSettleStrTime());
        gameRecord.setId(item.getId());
        gameRecord.setFirstProxy(item.getFirstProxy());
        gameRecord.setSecondProxy(item.getSecondProxy());
        gameRecord.setThirdProxy(item.getThirdProxy());
        if (!ObjectUtils.isEmpty(item.getOrderAmount())) {
            gameRecord.setBet(item.getOrderAmount().toString());
        }
        if (item.getProfitAmount() != null) {
            gameRecord.setWinLoss(item.getProfitAmount().toString());
        }
        return gameRecord;
    }



    @Data
    public static class StartTimeAndEndTime {
        private Long startTime;
        private Long endTime;
    }
}