package com.qianyi.casinoweb.job;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.business.UserMoneyBusiness;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.livewm.api.PublicWMApi;
import com.qianyi.modulecommon.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executor;

@Component
public class GameRecordJob {

    @Qualifier("asyncExecutor")
    @Autowired
    private Executor executor;

    // 创建线程池
//    ThreadPoolExecutor executor1 = new ThreadPoolExecutor(5, 10, 100, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));

    @Autowired
    PublicWMApi wmApi;
    @Autowired
    GameRecordService gameRecordService;
    @Autowired
    GameRecordEndTimeService gameRecordEndTimeService;
    @Autowired
    UserThirdService userThirdService;
    @Autowired
    UserService userService;
    @Autowired
    UserMoneyBusiness userMoneyBusiness;
    @Autowired
    UserWashCodeConfigService userWashCodeConfigService;
    @Autowired
    WashCodeConfigService washCodeConfigService;
    @Autowired
    PlatformConfigService platformConfigService;
    @Autowired
    WashCodeChangeService washCodeChangeService;

    //每隔5分钟执行一次
    @Scheduled(fixedRate = 1000 * 60 * 5)
    public void testTasks() {
        try {
            GameRecordEndTime gameRecord = gameRecordEndTimeService.findFirstByEndTimeDesc();
            String startTime = null;
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            String endTime = format.format(new Date());
            if (gameRecord == null) {
                Date date = format.parse(endTime);
                Calendar now = Calendar.getInstance();
                now.setTime(date);
                now.add(Calendar.MINUTE, -5);
                Date afterFiveMin = now.getTime();
                startTime = format.format(afterFiveMin);
            } else {
                startTime = gameRecord.getEndTime();
            }
            String result = wmApi.getDateTimeReport(null, startTime, endTime, 0, 0, 2, null, null);
            if (ObjectUtils.isEmpty(result)) {
                return;
            }
            if ("notData".equals(result)) {
                updateEndTime(endTime, gameRecord);
                return;
            }
            List<GameRecord> gameRecords = JSON.parseArray(result, GameRecord.class);
            if (!CollectionUtils.isEmpty(gameRecords)) {
                saveAll(gameRecords);
            }
            updateEndTime(endTime, gameRecord);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新游戏表结束时间
     *
     * @param endTime
     */
    public void updateEndTime(String endTime, GameRecordEndTime gameRecordEndTime) {
        if (gameRecordEndTime == null) {
            gameRecordEndTime = new GameRecordEndTime();
        }
        gameRecordEndTime.setEndTime(endTime);
        gameRecordEndTimeService.save(gameRecordEndTime);
    }

    public void saveAll(List<GameRecord> gameRecordList) {
        //查询最小清0打码量
        PlatformConfig platformConfig = platformConfigService.findFirst();
        for (GameRecord gameRecord : gameRecordList) {
            try {
                UserThird account = userThirdService.findByAccount(gameRecord.getUser());
                if (account == null || account.getUserId() == null) {
                    continue;
                }
                gameRecord.setUserId(account.getUserId());
                BigDecimal validbet = BigDecimal.ZERO;
                if (gameRecord.getValidbet() != null) {
                    validbet = new BigDecimal(gameRecord.getValidbet());
                }
                Long userId = account.getUserId();
                //有效投注额为0不参与洗码,打码,分润
                if (validbet.compareTo(BigDecimal.ZERO) == 0) {
                    gameRecord.setWashCodeStatus(Constants.yes);
                    gameRecord.setCodeNumStatus(Constants.yes);
                    gameRecord.setShareProfitStatus(Constants.yes);
                } else {
                    gameRecord.setWashCodeStatus(Constants.no);
                    gameRecord.setCodeNumStatus(Constants.no);
                    gameRecord.setShareProfitStatus(Constants.no);
                }
                //有数据会重复注单id唯一约束会报错，所以一条一条保存，避免影响后面的
                GameRecord record = gameRecordService.save(gameRecord);
                if (validbet.compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }
                //洗码
                userMoneyBusiness.washCode(Constants.PLATFORM, record);
                //扣减打码量
                userMoneyBusiness.subCodeNum(platformConfig, record);
                //代理分润
                userMoneyBusiness.shareProfit(platformConfig,record);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将一组数据平均分成n组
     *
     * @param source 要分组的数据源
     * @param n      平均分成n组
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> averageAssign(List<T> source, int n) {
        List<List<T>> result = new ArrayList<List<T>>();
        int remainder = source.size() % n;  //(先计算出余数)
        int number = source.size() / n;  //然后是商
        int offset = 0;//偏移量
        for (int i = 0; i < n; i++) {
            List<T> value = null;
            if (remainder > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remainder--;
                offset++;
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            result.add(value);
        }
        return result;
    }
}
