package com.qianyi.casinoreport.job;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.business.UserMoneyBusiness;
import com.qianyi.casinocore.constant.GoldenFConstant;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.RedisKeyUtil;
import com.qianyi.casinoreport.util.DateUtil;
import com.qianyi.casinoreport.vo.GameRecordObj;
import com.qianyi.casinoreport.vo.GoldenFTimeVO;
import com.qianyi.livegoldenf.api.PublicGoldenFApi;
import com.qianyi.modulecommon.Constants;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class GameRecordGoldenFJob {

    @Autowired
    private PublicGoldenFApi publicGoldenFApi;

    @Autowired
    private UserThirdService userThirdService;
    @Autowired
    private UserService userService;

    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    private GameRecordGoldenfEndTimeService gameRecordGoldenfEndTimeService;

    @Autowired
    private UserMoneyBusiness userMoneyBusiness;

    @Autowired
    private PlatformConfigService platformConfigService;

    @Autowired
    private AdGamesService adGamesService;

    @Autowired
    private GameRecordAsyncOper gameRecordAsyncOper;

    @Autowired
    private PlatformGameService platformGameService;

    @Autowired
    private RedisKeyUtil redisKeyUtil;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");



    @Scheduled(initialDelay = 10000, fixedDelay = 1000 * 60 * 2)
    public void pullGoldenF_PG() {
        PlatformGame pgPlatformGame = platformGameService.findByGamePlatformName(Constants.PLATFORM_PG);
        if (pgPlatformGame != null && pgPlatformGame.getGameStatus() == 2) {
            log.info("后台已关闭PG,无需拉单,platformGame={}", pgPlatformGame);
        } else {
            pullGameRecord(Constants.PLATFORM_PG);
        }
    }

    private void pullGameRecord(String vendorCode) {
        try {
            // 从数据库获取最近的拉单时间和平台
            List<GoldenFTimeVO> timeVOS = getTimes(vendorCode);
            if (vendorCode.equals(Constants.PLATFORM_PG)) {
                if (timeVOS.size() == 1) {
                    excutePull(true, vendorCode, timeVOS.get(0).getStartTime(), timeVOS.get(0).getEndTime(), 1);
                }
            }
        } catch (Exception e) {
            log.error("拉取注单时报错,vendorCode={},msg={}", vendorCode, e.getMessage());
        }
    }

    @Scheduled(initialDelay = 3000, fixedDelay = 1000 * 60 * 4)
    public void pullGoldenF_PGBD() {
        PlatformGame pgPlatformGame = platformGameService.findByGamePlatformName(Constants.PLATFORM_PG);
        if (pgPlatformGame != null && pgPlatformGame.getGameStatus() == 2) {
            log.info("后台已关闭PG,无需拉单,platformGame={}", pgPlatformGame);
        } else {
            pullGameRecordPGBD(Constants.PLATFORM_PG);
        }
    }

    private void pullGameRecordPGBD(String vendorCode) {
        try {
            // 从数据库获取最近的拉单时间和平台
            List<GoldenFTimeVO> timeVOS = getTimes(vendorCode);
            if (CollectionUtil.isNotEmpty(timeVOS) && timeVOS.size() > 1) {
                log.error("PG补单当前时间==【{}】 当前条数==> [{}]", timeVOS.get(0).getStartTime(), timeVOS.size());
                for (GoldenFTimeVO item : timeVOS) {
                    log.error("{},PG1开始补单{}到{}的注单数据", Constants.PLATFORM_PG, item.getStartTime(),
                        item.getEndTime());
                    excutePull(true, Constants.PLATFORM_PG, item.getStartTime(), item.getEndTime(), 2);
                    log.error("{},{}PG1到{}数据补单完成", Constants.PLATFORM_PG, item.getStartTime(),
                        item.getEndTime());
                }
                log.warn("PG数据补单完成补单结果条数 ===>> {}", timeVOS.size());
            }
        } catch (Exception e) {
            log.error("PG补取注单时报错,vendorCode={},msg={}", vendorCode, e.getMessage());
        }
    }

    /**
     * 补单
     *
     * @param vendorCode
     * @param timeVOS
     */
    public void supplementPullGameRecord(String vendorCode, List<GoldenFTimeVO> timeVOS) {
        timeVOS.forEach(item -> {
            log.info("{},开始补单{}到{}的注单数据", vendorCode, item.getStartTime(), item.getEndTime());
            excutePull(false, vendorCode, item.getStartTime(), item.getEndTime(), 3);
            log.info("{},{}到{}数据补单完成", vendorCode, item.getStartTime(), item.getEndTime());
        });
    }

    private Long getGoldenStartTime(GameRecordGoldenfEndTime gameRecordGoldenfEndTime) {
        Long startTime = 0l;
        if (gameRecordGoldenfEndTime == null) {
            startTime = DateUtil.next5MinuteTime();
        } else
            startTime = gameRecordGoldenfEndTime.getEndTime();
        return startTime * 1000;
    }

    public List<GoldenFTimeVO> getTimes(String vendor) {
        List<GoldenFTimeVO> timeVOS = new ArrayList<>();
        GameRecordGoldenfEndTime gameRecordGoldenfEndTime =
            gameRecordGoldenfEndTimeService.findFirstByVendorCodeOrderByEndTimeDesc(vendor);
        Long startTime = getGoldenStartTime(gameRecordGoldenfEndTime);
        Long endTime = System.currentTimeMillis() - (60 * 1000);
        log.info("{},{}", startTime, endTime);
        Long range = endTime - startTime;
        if (range <= 0)
            return timeVOS;
        log.info("{}", range);
        Long num = range / (4 * 60 * 1000);
        log.info("num is {}", num);
        for (int i = 0; i <= num; i++) {
            startTime = startTime - 60 * 1000;// 每次拉取重叠一分钟
            GoldenFTimeVO goldenFTimeVO = new GoldenFTimeVO();
            Long tempEndTime = startTime + (5 * 60 * 1000);
            goldenFTimeVO.setStartTime(startTime);
            goldenFTimeVO.setEndTime(tempEndTime > endTime ? endTime : tempEndTime);
            timeVOS.add(goldenFTimeVO);
            startTime = tempEndTime;
        }
        log.info("{}", timeVOS);
        return timeVOS;
    }

    private void excutePull(boolean pull, String vendorCode, Long startTime, Long endTime, Integer pullType) {
        log.info("startime is {}  endtime is {}", startTime, endTime);
        Integer failCount = 0;

        int page = 1;

        int pageSize = 2000;
        Boolean successRequestFlag = true;
        while (true) {
            // 获取数据
            PublicGoldenFApi.ResponseEntity responseEntity =
                publicGoldenFApi.getPlayerGameRecord(startTime, endTime, vendorCode, page, pageSize);

            if (responseEntity == null || checkRequestFail(responseEntity)) {
                processFaildRequest(startTime, endTime, vendorCode, responseEntity);
                if (failCount >= 2) {
                    successRequestFlag = false;
                    break;
                }
                failCount++;
                continue;
            }
            if (saveData(responseEntity)) {
                break;
            } else {

            }

            page++;
        }
        if (pull && successRequestFlag) {
            processSuccessRequest(startTime, endTime, vendorCode, pullType);
        }
    }

    private void processSuccessRequest(Long startTime, Long endTime, String vendorCode, Integer pullType) {
        GameRecordGoldenfEndTime gameRecordGoldenfEndTime = new GameRecordGoldenfEndTime();
        gameRecordGoldenfEndTime.setStartTime(startTime / 1000);
        gameRecordGoldenfEndTime.setEndTime(endTime / 1000);
        gameRecordGoldenfEndTime.setVendorCode(vendorCode);
        gameRecordGoldenfEndTime.setPullType(pullType);
        gameRecordGoldenfEndTimeService.save(gameRecordGoldenfEndTime);
    }

    private void processFaildRequest(Long startTime, Long endTime, String vendorCode,
        PublicGoldenFApi.ResponseEntity responseEntity) {
        log.error("注单拉取失败startTime{},endTime{},vendorCode{},responseEntity{}", startTime, endTime, vendorCode,
            responseEntity);
    }

    private boolean checkRequestFail(PublicGoldenFApi.ResponseEntity responseEntity) {
        return responseEntity.getErrorCode() != null;
    }

    private Boolean saveData(PublicGoldenFApi.ResponseEntity responseEntity) {
        try {
            log.info("reponseEntity is {}", responseEntity);
            GameRecordObj gameRecordObj = JSON.parseObject(responseEntity.getData(), GameRecordObj.class);
            List<GameRecordGoldenF> recordGoldenFS = gameRecordObj.getBetlogs();
            processRecords(recordGoldenFS);
            return gameRecordObj.getPage() >= gameRecordObj.getPageCount();
        } catch (Exception ex) {
            log.error("处理结果集异常", ex);
            return false;
        }
    }

    private void processRecords(List<GameRecordGoldenF> recordGoldenFS) {
        PlatformConfig platformConfig = platformConfigService.findFirst();
        recordGoldenFS.forEach(item -> {
            log.error("pg拉单用户名称{}",item.getPlayerName());
            UserThird userThird = userThirdService.findByGoldenfAccount(item.getPlayerName());
            if (userThird == null){
                log.error("pg拉单用户存在");
                return;
            }
            User user = userService.findById(userThird.getUserId());
            item.setUserId(userThird.getUserId());
            item.setFirstProxy(user.getFirstProxy());
            item.setSecondProxy(user.getSecondProxy());
            item.setThirdProxy(user.getThirdProxy());
            if (item.getCreatedAt() != null) {
                item.setCreateAtStr(DateUtil.timeStamp2Date(item.getCreatedAt(), ""));
            }
            saveToDB(item, platformConfig, user);
        });
    }

    private void saveToDB(GameRecordGoldenF item, PlatformConfig platformConfig, User user) {
        try {
            GameRecordGoldenF gameRecordGoldenF =
                gameRecordGoldenFService.findGameRecordGoldenFByTraceId(item.getTraceId());
            if (gameRecordGoldenF != null) {
                log.error("pg拉单订单已经存在");
                return;
            }
            gameRecordGoldenFService.save(item);
            GameRecord gameRecord = combineGameRecord(item);
            // 发送注单消息到MQ后台要统计数据
            gameRecordAsyncOper.proxyGameRecordReport(item.getVendorCode(), gameRecord);
            processBusiness(item, gameRecord, platformConfig, user);
        } catch (Exception e) {
            log.error("注单数据保存失败,msg={}", e.getMessage());
        }

    }


    @Async("asyncExecutor")
    public void processBusiness(GameRecordGoldenF gameRecordGoldenF, GameRecord gameRecord,
        PlatformConfig platformConfig, User user) {
//        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(gameRecordGoldenF.getTraceId());
        try {
//            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
            // 改变用户实时余额
            gameRecordAsyncOper.changeUserBalancePg(gameRecordGoldenF);
            if (gameRecordGoldenF.getBetAmount().compareTo(BigDecimal.ZERO) == 0)
                return;
            if (!gameRecordGoldenF.getTransType().equals(GoldenFConstant.GOLDENF_STAKE))
                return;
            // 洗码
            gameRecordAsyncOper.washCode(gameRecordGoldenF.getVendorCode(), gameRecord);
            // 抽点
            gameRecordAsyncOper.extractPoints(gameRecordGoldenF.getVendorCode(), gameRecord);
            // 扣减打码量
            gameRecordAsyncOper.subCodeNum(gameRecordGoldenF.getVendorCode(), platformConfig, gameRecord);
            //代理分润
            if (Objects.nonNull(user) && Objects.nonNull(user.getThirdPid()) && user.getThirdPid() != 0L) {//没有上级不分润
                gameRecordAsyncOper.shareProfit(gameRecordGoldenF.getVendorCode(), gameRecord);
            }
            // 返利
            gameRecordAsyncOper.rebate(gameRecordGoldenF.getVendorCode(), gameRecord);
            if (gameRecordGoldenF.getTransType() != null && gameRecordGoldenF.getTransType().equals("Stake")) {
                // 等级流水
                gameRecordAsyncOper.levelWater(gameRecordGoldenF.getVendorCode(), gameRecord);
            }
        } catch (Exception e) {
            log.error("注单数据保存失败,msg={}", e.getMessage());
        } finally {
//            RedisKeyUtil.unlock(userMoneyLock);
        }

    }

    private GameRecord combineGameRecord(GameRecordGoldenF item) {
        // 沙巴体育的gameCode和列表提供的不一致,沙巴只有一款游戏，写死适配下
        String gameCode = item.getGameCode();
        String gameName = null;
        if (Constants.PLATFORM_SABASPORT.equals(item.getVendorCode())) {
            gameName = "SABA体育";
        } else {
            AdGame adGame = adGamesService.findByGamePlatformNameAndGameCode(item.getVendorCode(), item.getGameCode());
            if (adGame != null) {
                gameName = adGame.getGameName();
            }
        }
        GameRecord gameRecord = new GameRecord();
        gameRecord.setBetId(item.getBetId());
        gameRecord.setValidbet(item.getBetAmount().toString());
        gameRecord.setUserId(item.getUserId());
        gameRecord.setGameCode(gameCode);
        gameRecord.setGname(gameName);
        gameRecord.setBetTime(item.getCreateAtStr());
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

}
