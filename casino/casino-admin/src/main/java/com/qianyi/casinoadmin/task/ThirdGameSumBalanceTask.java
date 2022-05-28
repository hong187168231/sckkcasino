package com.qianyi.casinoadmin.task;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.service.UserThirdService;
import com.qianyi.casinocore.util.BillThreadPool;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.TaskConst;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ThirdGameSumBalanceTask {

    @Autowired
    UserThirdService userThirdService;

    @Autowired
    private UserMoneyService userMoneyService;

    @Autowired
    private RedisUtil redisUtil;

    private static final BillThreadPool threadPool = new BillThreadPool(CommonConst.NUMBER_10);


    @Scheduled(cron = TaskConst.TOTAL_PLATFORM_QUOTA_TASK)
    public void create(){
        //查询WM总余额
        List<UserThird> allAcount = userThirdService.findAllAcount();
        if (LoginUtil.checkNull(allAcount) || allAcount.size() == CommonConst.NUMBER_0) return ;


        List<UserThird> wmThird = allAcount.stream().filter(o -> Objects.nonNull(o.getAccount())).collect(Collectors.toList());
        List<UserThird> pgCq9Third = allAcount.stream().filter(o -> Objects.nonNull(o.getGoldenfAccount())).collect(Collectors.toList());
        List<UserThird> obdjThird = allAcount.stream().filter(o -> Objects.nonNull(o.getObdjAccount())).collect(Collectors.toList());
        List<UserThird> obtyThird = allAcount.stream().filter(o -> Objects.nonNull(o.getObtyAccount())).collect(Collectors.toList());
        List<UserThird> sabaThird = allAcount.stream().filter(o -> Objects.nonNull(o.getGoldenfAccount())).collect(Collectors.toList());
        //异步方法，查询三方总余额，缓存到redis

        //WM总余额
        setRedisWMMoneyTotal(wmThird);
        //PG/CQ9总余额
        setRedisPGMoneyTotal(pgCq9Third);
        //查询OB电竞总余额
        setRedisOBDJMoneyTotal(obdjThird);
        //查询OB体育总余额
        setRedisOBTYMoneyTotal(obtyThird);
        //查询沙巴体育总余额
        setRedisSABAMoneyTotal(sabaThird);

    }

    @Async
    private void setRedisSABAMoneyTotal(List<UserThird> sabaThird) {
        if (LoginUtil.checkNull(sabaThird) || sabaThird.size() == CommonConst.NUMBER_0){
            return ;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(sabaThird.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u:sabaThird){
            threadPool.execute(() ->{
                try {
                    JSONObject jsonObject = userMoneyService.getSABAonetUser(u);

                    if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
                        list.add(BigDecimal.ZERO);
                    }else {
                        Integer code = (Integer) jsonObject.get("code");
                        if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))){
                            list.add(new BigDecimal(jsonObject.get("data").toString()));
                        }
                    }
                }finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);

        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        //存入缓存
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_SABASPORT;
        redisUtil.set(key, sum, Constants.THIRD_BALANCE_TTL);
    }

    @Async
    private void setRedisOBTYMoneyTotal(List<UserThird> obtyThird) {
        if (LoginUtil.checkNull(obtyThird) || obtyThird.size() == CommonConst.NUMBER_0){
            return;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(obtyThird.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u:obtyThird){
            threadPool.execute(() ->{
                try {
                    JSONObject jsonObject = userMoneyService.refreshOBTY(u.getUserId());
                    if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
                        list.add(BigDecimal.ZERO);
                    }else {
                        Integer code = (Integer) jsonObject.get("code");
                        if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))){
                            list.add(new BigDecimal(jsonObject.get("data").toString()));
                        }
                    }
                }finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);
        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_OBTY;
        redisUtil.set(key, sum, Constants.THIRD_BALANCE_TTL);
    }

    @Async
    private void setRedisOBDJMoneyTotal(List<UserThird> obdjThird) {
        if (LoginUtil.checkNull(obdjThird) || obdjThird.size() == CommonConst.NUMBER_0){
            return;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(obdjThird.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u:obdjThird){
            threadPool.execute(() ->{
                try {
                    JSONObject jsonObject = userMoneyService.refreshOB(u.getUserId());
                    if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
                        list.add(BigDecimal.ZERO);
                    }else {
                        Integer code = (Integer) jsonObject.get("code");
                        if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))){
                            list.add(new BigDecimal(jsonObject.get("data").toString()));
                        }
                    }
                }finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);
        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_OBDJ;
        redisUtil.set(key, sum, Constants.THIRD_BALANCE_TTL);
    }

    @Async
    private void setRedisPGMoneyTotal(List<UserThird> pgCq9Third) {
        if (LoginUtil.checkNull(pgCq9Third) || pgCq9Third.size() == CommonConst.NUMBER_0){
            return;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(pgCq9Third.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u:pgCq9Third){
            threadPool.execute(() ->{
                try {
                    JSONObject jsonObject = userMoneyService.refreshPGAndCQ9UserId(u.getUserId().toString());
                    if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
                        list.add(BigDecimal.ZERO);
                    }else {
                        Integer code = (Integer) jsonObject.get("code");
                        if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))){
                            synchronized (this){
                                list.add(new BigDecimal(jsonObject.get("data").toString()));
                            }
                        }
                    }
                }finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);
        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_PG;
        redisUtil.set(key, sum, Constants.THIRD_BALANCE_TTL);
    }


    @Async
    public void setRedisWMMoneyTotal(List<UserThird> wmThird){
        if (LoginUtil.checkNull(wmThird) || wmThird.size() == CommonConst.NUMBER_0){
            return ;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(wmThird.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u:wmThird){
            threadPool.execute(() ->{
                try {
                    JSONObject jsonObject = userMoneyService.getWMonetUser(u);

                    if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
                        list.add(BigDecimal.ZERO);
                    }else {
                        Integer code = (Integer) jsonObject.get("code");
                        if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))){
                            list.add(new BigDecimal(jsonObject.get("data").toString()));
                        }
                    }
                }finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);

        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        //存入缓存
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_WM_BIG;
        redisUtil.set(key, sum, Constants.THIRD_BALANCE_TTL);
    }
}
