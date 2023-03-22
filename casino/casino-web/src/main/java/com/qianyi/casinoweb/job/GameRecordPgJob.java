package com.qianyi.casinoweb.job;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.business.UserMoneyBusiness;
import com.qianyi.casinocore.constant.GoldenFConstant;
import com.qianyi.casinocore.exception.BusinessException;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.RedisKeyUtil;
import com.qianyi.casinoweb.util.DateUtil;
import com.qianyi.casinoweb.util.SplitListUtils;
import com.qianyi.casinoweb.vo.GameRecordObj;
import com.qianyi.casinoweb.vo.GoldenFTimeVO;
import com.qianyi.livegoldenf.api.PublicGoldenFApi;
import com.qianyi.modulecommon.Constants;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

@Component
@Slf4j
public class GameRecordPgJob {

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

    private ExecutorService executorService =
        Executors.newFixedThreadPool(32, new CustomizableThreadFactory("process-records-"));

    @Scheduled(initialDelay = 7000, fixedDelay = 1000 * 60 * 2)
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

    public void excutePull(boolean pull, String vendorCode, Long startTime, Long endTime, Integer pullType) {
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
            processRecords3(recordGoldenFS);
            return true;
        } catch (Exception ex) {
            log.error("处理结果集异常", ex);
            return false;
        }
    }

    private void processRecords3(List<GameRecordGoldenF> recordGoldenFS) {
        PlatformConfig platformConfig = platformConfigService.findFirst();
        // 大集合拆分成N个小集合, 这里集合的size可以稍微小一些（这里我用100刚刚好）, 以保证多线程异步执行, 过大容易回到单线程
        List<List<GameRecordGoldenF>> splitNList = SplitListUtils.split(recordGoldenFS, 10);
        // 记录单个任务的执行次数
        log.error("拆分前面的list" + JSON.toJSONString(splitNList));
        CountDownLatch countDownLatch = new CountDownLatch(splitNList.size());
        long start = System.currentTimeMillis();
        // 对拆分的集合进行批量处理, 先拆分的集合, 再多线程执行
        for (List<GameRecordGoldenF> singleList : splitNList) {
            // 线程池执行
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        log.info("singleList长度={}", singleList.size());
                        for (GameRecordGoldenF item : singleList) {
                            //log.error("拆分后面的list" + JSON.toJSONString(singleList));
                            UserThird userThird = userThirdService.findByGoldenfAccount(item.getPlayerName());
                            if (userThird == null) {
                                log.warn("userThird is null");
                                continue;
                            }
                            User user = userService.findById(userThird.getUserId());
                            item.setUserId(userThird.getUserId());
                            item.setFirstProxy(user.getFirstProxy());
                            item.setSecondProxy(user.getSecondProxy());
                            item.setThirdProxy(user.getThirdProxy());
                            if (item.getCreatedAt() != null) {
                                item.setCreateAtStr(DateUtil.timeStamp2Date(item.getCreatedAt(), ""));
                            }
                            GameRecordGoldenF gameRecordGoldenF =
                                gameRecordGoldenFService.findGameRecordGoldenFByTraceId(item.getTraceId());
                            if (gameRecordGoldenF != null) {
                                log.warn("gameRecordGoldenF is not null");
                                continue;
                            }
                            gameRecordGoldenFService.save(item);
                            log.info("item保存成功,id={}", item.getId());
                            // 改变用户实时余额
                            changeUserBalance(item);
                            GameRecord gameRecord = combineGameRecord(item);
                            // 发送注单消息到MQ后台要统计数据
                            gameRecordAsyncOper.proxyGameRecordReport(item.getVendorCode(), gameRecord);
                            processBusiness(item, gameRecord, platformConfig, user);
                        }
                    } catch (Exception e) {
                        log.error("singleList error", e);
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (Exception e) {
            throw new RuntimeException("countDownLatch阻塞等待出错", e);
        }
        log.info("/payTypeList{}毫秒", (System.currentTimeMillis() - start));
    }

    /**
     * 改变用户实时余额
     */
    private void changeUserBalance(GameRecordGoldenF gameRecordGoldenF) {
        Long userId = gameRecordGoldenF.getUserId();
        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(userId.toString());
        try {
            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
            BigDecimal betAmount = gameRecordGoldenF.getBetAmount();
            BigDecimal winAmount = gameRecordGoldenF.getWinAmount();
            if (betAmount == null || winAmount == null) {
                return;
            }
            BigDecimal winLossAmount = winAmount.subtract(betAmount);
            // 下注金额大于0，扣减
            if (betAmount.compareTo(BigDecimal.ZERO) == 1) {
                userMoneyBusiness.subBalance(userId, betAmount);
            }
            // 派彩金额大于0，增加
            if (winLossAmount.compareTo(BigDecimal.ZERO) == 1) {
                userMoneyBusiness.addBalance(userId, winLossAmount);
            }
        } catch (Exception e) {
            log.error("改变用户实时余额时报错，msg={}", e.getMessage());
        } finally {
            // 释放锁
            RedisKeyUtil.unlock(userMoneyLock);
        }
    }

    private void processBusiness(GameRecordGoldenF gameRecordGoldenF, GameRecord gameRecord,
        PlatformConfig platformConfig, User user) {
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
