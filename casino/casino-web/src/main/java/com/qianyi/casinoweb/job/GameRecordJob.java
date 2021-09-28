package com.qianyi.casinoweb.job;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.business.UserCodeNumBusiness;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinoweb.vo.WashCodeVo;
import com.qianyi.livewm.api.PublicWMApi;
import com.qianyi.modulecommon.util.DateUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class GameRecordJob {

    // 创建线程池
    ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 100, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));

    private String PLATFORM="wm";

    private static SimpleDateFormat yyyyMMdd = new SimpleDateFormat(DateUtil.YYYYMMDD);


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
    UserCodeNumBusiness userCodeNumBusiness;
    @Autowired
    UserWashCodeConfigService userWashCodeConfigService;
    @Autowired
    WashCodeConfigService washCodeConfigService;
    @Autowired
    RedisUtil redisUtil;

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
        //把大集成拆分成5个
        List<List<GameRecord>> lists = averageAssign(gameRecordList, 5);
        // 创建异步执行任务:
        for(List<GameRecord> list:lists){
            if (CollectionUtils.isEmpty(list)){
                continue;
            }
            CompletableFuture cf = CompletableFuture.runAsync(()->{
                for (GameRecord gameRecord : list) {
                    try {
                        UserThird account = userThirdService.findByAccount(gameRecord.getUser());
                        if (account == null) {
                            continue;
                        }
                        BigDecimal validbet = BigDecimal.ZERO;
                        if (gameRecord.getValidbet() != null) {
                            validbet = new BigDecimal(gameRecord.getValidbet());
                        }
                        //查询洗码配置
                        Map<String, BigDecimal> washCode = findWashCode(account.getUserId());
                        //洗码
                        washCode(washCode, gameRecord, validbet,account.getUserId());
                        //有数据会重复注单id唯一约束会报错，所以一条一条保存，避免影响后面的
                        gameRecordService.save(gameRecord);
                        //游戏记录保存成功后扣减打码量
                        userCodeNumBusiness.subCodeNum(validbet, account.getUserId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            },executor);
        }
    }

    /**
     * 将一组数据平均分成n组
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

    /**
     * 洗码
     * @param washCode
     * @param gameRecord
     * @param validbet
     * @return
     * @throws ParseException
     */
    public synchronized BigDecimal washCode(Map<String, BigDecimal> washCode, GameRecord gameRecord, BigDecimal validbet,Long userId) throws ParseException {
        BigDecimal rate = washCode.get(gameRecord.getGid().toString());
        gameRecord.setRate(rate);
        if (rate == null || validbet == null || BigDecimal.ZERO.compareTo(rate) == 0 || BigDecimal.ZERO.compareTo(validbet) == 0) {
            gameRecord.setWashCode(BigDecimal.ZERO);
            return BigDecimal.ZERO;
        }
        BigDecimal washCodeVal = validbet.multiply(rate);
        gameRecord.setWashCode(washCodeVal);
        if (!ObjectUtils.isEmpty(gameRecord.getBetTime())) {
            Date parse = DateUtil.getSimpleDateFormat().parse(gameRecord.getBetTime());
            String date = yyyyMMdd.format(parse);
            String key = PLATFORM + ":" + userId + ":" + gameRecord.getGid() + ":" + date;
            Object redisVal = redisUtil.get(key);
            if(ObjectUtils.isEmpty(redisVal)){
                WashCodeVo vo=new WashCodeVo();
                vo.setValidbet(validbet);
                vo.setAmount(washCodeVal);
                vo.setGameId(gameRecord.getGid().toString());
                redisUtil.set(key,vo);
            }else{
                WashCodeVo vo= (WashCodeVo) redisVal;
                vo.setValidbet(vo.getValidbet().add(validbet));
                vo.setAmount(vo.getAmount().add(washCodeVal));
                vo.setGameId(gameRecord.getGid().toString());
                redisUtil.set(key,vo);
            }
        }
        return washCodeVal;
    }

    /**
     * 获取洗码配置
     * @param userId
     * @return
     */
    public Map<String, BigDecimal> findWashCode(Long userId) {
        Map<String, BigDecimal> config = new HashMap<>();
        //先查询用户级别的配置信息
        List<UserWashCodeConfig> codeConfigs = userWashCodeConfigService.findByUserIdAndPlatform(userId, PLATFORM);
        if (!CollectionUtils.isEmpty(codeConfigs)) {
            codeConfigs.stream().filter(item -> (item.getState() != null && item.getState() == 0)).forEach(item ->
                    config.put(item.getGameId(), item.getRate())
            );
            return config;
        }
        List<WashCodeConfig> platform = washCodeConfigService.findByPlatform(PLATFORM);
        if (CollectionUtils.isEmpty(platform)) {
            return config;
        }
        platform.stream().filter(item -> (item.getState() != null && item.getState() == 0)).forEach(item -> config.put(item.getGameId(), item.getRate()));
        return config;
    }
}
