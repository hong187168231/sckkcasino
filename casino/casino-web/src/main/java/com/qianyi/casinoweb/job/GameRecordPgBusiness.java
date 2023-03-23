package com.qianyi.casinoweb.job;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.business.UserMoneyBusiness;
import com.qianyi.casinocore.constant.GoldenFConstant;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.RedisKeyUtil;
import com.qianyi.casinoweb.util.DateUtil;
import com.qianyi.casinoweb.vo.GameRecordObj;
import com.qianyi.casinoweb.vo.GoldenFTimeVO;
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
public class GameRecordPgBusiness {

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
    private PlatformConfigService platformConfigService;

    @Autowired
    private AdGamesService adGamesService;

    @Autowired
    private GameRecordPgAsyncOper gameRecordPgAsyncOper;

    @Autowired
    private RedisKeyUtil redisKeyUtil;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 补单
     *
     * @param vendorCode
     * @param timeVOS
     */
    public void supplementPullGameRecord(String vendorCode, List<GoldenFTimeVO> timeVOS) {
        timeVOS.forEach(item -> {
            log.info("{},开始补单{}到{}的注单数据", vendorCode, item.getStartTime(), item.getEndTime());
            excutePull(false, vendorCode, item.getStartTime(), item.getEndTime(),3);
            log.info("{},{}到{}数据补单完成", vendorCode, item.getStartTime(), item.getEndTime());
        });
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
            processSuccessRequest(startTime, endTime, vendorCode,pullType);
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
            UserThird userThird = userThirdService.findByGoldenfAccount(item.getPlayerName());
            if (userThird == null)
                return;
            User user = userService.findById(userThird.getUserId());
            item.setUserId(userThird.getUserId());
            item.setFirstProxy(user.getFirstProxy());
            item.setSecondProxy(user.getSecondProxy());
            item.setThirdProxy(user.getThirdProxy());
            if (item.getCreatedAt() != null) {
                item.setCreateAtStr(DateUtil.timeStamp2Date(item.getCreatedAt(), ""));
            }
            saveToDB(item, platformConfig,user);
        });
    }

    private void saveToDB(GameRecordGoldenF item, PlatformConfig platformConfig,User user) {
        try {
            GameRecordGoldenF gameRecordGoldenF =
                gameRecordGoldenFService.findGameRecordGoldenFByTraceId(item.getTraceId());
            if (gameRecordGoldenF != null) {
                return;
            }
            gameRecordGoldenFService.save(item);
            GameRecord gameRecord = combineGameRecord(item);
            // 发送注单消息到MQ后台要统计数据
            gameRecordPgAsyncOper.proxyGameRecordReport(item.getVendorCode(), gameRecord);
            processBusinessPg(item, gameRecord, platformConfig,user);
        } catch (Exception e) {
            log.error("注单数据保存失败,msg={}", e.getMessage());
        }

    }


    @Async("asyncExecutor")
    public void processBusinessPg(GameRecordGoldenF gameRecordGoldenF, GameRecord gameRecord,
        PlatformConfig platformConfig, User user) {
        RLock userMoneyLock = redisKeyUtil.getUserMoneyLock(gameRecord.getUserId().toString());
        try {
            userMoneyLock.lock(RedisKeyUtil.LOCK_TIME, TimeUnit.SECONDS);
            // 改变用户实时余额
            gameRecordPgAsyncOper.changeUserBalancePg(gameRecordGoldenF);
            if (gameRecordGoldenF.getBetAmount().compareTo(BigDecimal.ZERO) == 0)
                return;
            if (!gameRecordGoldenF.getTransType().equals(GoldenFConstant.GOLDENF_STAKE))
                return;
            // 洗码
            gameRecordPgAsyncOper.washCode(gameRecordGoldenF.getVendorCode(), gameRecord);
            // 抽点
            gameRecordPgAsyncOper.extractPoints(gameRecordGoldenF.getVendorCode(), gameRecord);
            // 扣减打码量
            gameRecordPgAsyncOper.subCodeNum(gameRecordGoldenF.getVendorCode(), platformConfig, gameRecord);
            //代理分润
            if (Objects.nonNull(user) && Objects.nonNull(user.getThirdPid()) && user.getThirdPid() != 0L) {//没有上级不分润
                gameRecordPgAsyncOper.shareProfit(gameRecordGoldenF.getVendorCode(), gameRecord);
            }
            // 返利
            gameRecordPgAsyncOper.rebate(gameRecordGoldenF.getVendorCode(), gameRecord);
            if (gameRecordGoldenF.getTransType() != null && gameRecordGoldenF.getTransType().equals("Stake")) {
                // 等级流水
                gameRecordPgAsyncOper.levelWater(gameRecordGoldenF.getVendorCode(), gameRecord);
            }
        } catch (Exception e) {
            log.error("注单数据保存失败,msg={}", e.getMessage());
        } finally {
            RedisKeyUtil.unlock(userMoneyLock);
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
