package com.qianyi.casinoadmin.service;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.util.BillThreadPool;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.ExpirationTimeUtil;
import com.qianyi.casinocore.util.RedisKeyUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class ThridUserBalanceSumService {

    @Autowired
    private RedisUtil redisUtil;

    private static final BillThreadPool threadPool = new BillThreadPool(CommonConst.NUMBER_10);

    @Autowired
    private UserMoneyService userMoneyService;

    @Autowired
    private RedisKeyUtil redisKeyUtil;

    public void setRedisSABAMoneyTotal(List<UserThird> sabaThird, String platform, long time) {
        if (LoginUtil.checkNull(sabaThird) || sabaThird.size() == CommonConst.NUMBER_0) {
            return;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(sabaThird.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u : sabaThird) {
            threadPool.execute(() -> {
                try {
                    if (ExpirationTimeUtil.check(platform, u.getUserId().toString(), time, ExpirationTimeUtil.sports)) {
                        log.info("平台{}查询{}余额走缓存",platform, u.getUserId().toString());
                        list.add(ExpirationTimeUtil.getTripartiteBalance(platform, u.getUserId().toString()));
                    } else {
                        JSONObject jsonObject = userMoneyService.refreshSABA(u.getUserId());
                        if (u.getUserId().intValue() == 72933) {
                            log.info("72933会员余额查询返回结果：【{}】", jsonObject);
                        }
                        if (LoginUtil.checkNull(jsonObject)
                            || LoginUtil.checkNull(jsonObject.get("code"), jsonObject.get("msg"))) {
                            list.add(BigDecimal.ZERO);
                        } else {
                            Integer code = (Integer)jsonObject.get("code");
                            if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))) {
                                list.add(new BigDecimal(jsonObject.get("data").toString()));
                            }
                        }
                    }
                } finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);

        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal bigDecimal : list) {
            sum = bigDecimal.add(sum);
        }
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        // 存入缓存
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_SABASPORT;
        redisUtil.set(key, sum, Constants.THIRD_BALANCE_ALL);
        // 更新数据的
        redisUtil.set(key + "TIME", DateUtil.dateToString(new Date(), DateUtil.patten), Constants.THIRD_BALANCE_ALL);

    }

    public void setRedisOBTYMoneyTotal(List<UserThird> obtyThird, String platform, long time) {
        if (LoginUtil.checkNull(obtyThird) || obtyThird.size() == CommonConst.NUMBER_0) {
            return;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(obtyThird.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u : obtyThird) {
            threadPool.execute(() -> {
                try {
                    if (ExpirationTimeUtil.check(platform, u.getUserId().toString(), time, ExpirationTimeUtil.sports)) {
                        log.info("平台{}查询{}余额走缓存",platform, u.getUserId().toString());
                        list.add(ExpirationTimeUtil.getTripartiteBalance(platform, u.getUserId().toString()));
                    } else {
                        JSONObject jsonObject = userMoneyService.refreshOBTY(u.getUserId());
                        if (LoginUtil.checkNull(jsonObject)
                            || LoginUtil.checkNull(jsonObject.get("code"), jsonObject.get("msg"))) {
                            list.add(BigDecimal.ZERO);
                        } else {
                            Integer code = (Integer)jsonObject.get("code");
                            if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))) {
                                list.add(new BigDecimal(jsonObject.get("data").toString()));
                            }
                        }
                    }
                } finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);
        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_OBTY;
        redisUtil.set(key, sum, Constants.THIRD_BALANCE_ALL);
        redisUtil.set(key + "TIME", DateUtil.dateToString(new Date(), DateUtil.patten), Constants.THIRD_BALANCE_ALL);
    }

    public void setRedisOBDJMoneyTotal(List<UserThird> obdjThird, String platform, long time) {
        if (LoginUtil.checkNull(obdjThird) || obdjThird.size() == CommonConst.NUMBER_0) {
            return;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(obdjThird.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u : obdjThird) {
            threadPool.execute(() -> {
                try {
                    if (ExpirationTimeUtil.check(platform, u.getUserId().toString(), time, ExpirationTimeUtil.universality)) {
                        log.info("平台{}查询{}余额走缓存",platform, u.getUserId().toString());
                        list.add(ExpirationTimeUtil.getTripartiteBalance(platform, u.getUserId().toString()));
                    } else {
                        JSONObject jsonObject = userMoneyService.refreshOB(u.getUserId(), false);
                        if (LoginUtil.checkNull(jsonObject)
                            || LoginUtil.checkNull(jsonObject.get("code"), jsonObject.get("msg"))) {
                            list.add(BigDecimal.ZERO);
                        } else {
                            Integer code = (Integer)jsonObject.get("code");
                            if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))) {
                                list.add(new BigDecimal(jsonObject.get("data").toString()));
                            }
                        }
                    }
                } finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);
        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_OBDJ;
        redisUtil.set(key, sum, Constants.THIRD_BALANCE_ALL);
        redisUtil.set(key + "TIME", DateUtil.dateToString(new Date(), DateUtil.patten), Constants.THIRD_BALANCE_ALL);
    }

    @Async
    public void setRedisPGMoneyTotal(List<UserThird> pgCq9Third, String platform, long time) {
        if (LoginUtil.checkNull(pgCq9Third) || pgCq9Third.size() == CommonConst.NUMBER_0) {
            return;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(pgCq9Third.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u : pgCq9Third) {
            threadPool.execute(() -> {
                try {
                    if (ExpirationTimeUtil.check(platform, u.getUserId().toString(), time, ExpirationTimeUtil.universality)) {
                        log.info("平台{}查询{}余额走缓存",platform, u.getUserId().toString());
                        list.add(ExpirationTimeUtil.getTripartiteBalance(platform, u.getUserId().toString()));
                    } else {
                        JSONObject jsonObject = userMoneyService.refreshPGAndCQ9UserId(u.getUserId().toString());
                        if (LoginUtil.checkNull(jsonObject)
                            || LoginUtil.checkNull(jsonObject.get("code"), jsonObject.get("msg"))) {
                            list.add(BigDecimal.ZERO);
                        } else {
                            Integer code = (Integer)jsonObject.get("code");
                            if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))) {
                                list.add(new BigDecimal(jsonObject.get("data").toString()));
                            }
                        }
                    }

                } finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);
        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_PG;
        redisUtil.set(key, sum, Constants.THIRD_BALANCE_ALL);
        redisUtil.set(key + "TIME", DateUtil.dateToString(new Date(), DateUtil.patten), Constants.THIRD_BALANCE_ALL);
    }

    public void setRedisWMMoneyTotal(List<UserThird> wmThird, String platform, long time) {
        if (LoginUtil.checkNull(wmThird) || wmThird.size() == CommonConst.NUMBER_0) {
            return;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(wmThird.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u : wmThird) {
            threadPool.execute(() -> {
                try {
                    if (ExpirationTimeUtil.check(platform, u.getUserId().toString(), time, ExpirationTimeUtil.universality)) {
                        log.info("平台{}查询{}余额走缓存",platform, u.getUserId().toString());
                        list.add(ExpirationTimeUtil.getTripartiteBalance(platform, u.getUserId().toString()));
                    } else {
                        JSONObject jsonObject = userMoneyService.getWMonetUser(u);
                        if (LoginUtil.checkNull(jsonObject)
                            || LoginUtil.checkNull(jsonObject.get("code"), jsonObject.get("msg"))) {
                            list.add(BigDecimal.ZERO);
                        } else {
                            Integer code = (Integer)jsonObject.get("code");
                            if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))) {
                                list.add(new BigDecimal(jsonObject.get("data").toString()));
                                ExpirationTimeUtil.resetTripartiteBalance(Constants.PLATFORM_WM_BIG,u.getUserId().toString(),new BigDecimal(jsonObject.get("data").toString()));
                            }
                        }
                    }
                } finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);

        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        // 存入缓存
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_WM_BIG;
        redisUtil.set(key, sum, Constants.THIRD_BALANCE_ALL);
        redisUtil.set(key + "TIME", DateUtil.dateToString(new Date(), DateUtil.patten), Constants.THIRD_BALANCE_ALL);
    }

    public void setRedisAEMoneyTotal() {
        JSONObject jsonObject = userMoneyService.refreshAE(null, false);
        BigDecimal sum = null;
        if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"), jsonObject.get("msg"))) {
            sum = BigDecimal.ZERO;
        } else {
            Integer code = (Integer)jsonObject.get("code");
            if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))) {
                BigDecimal sunAamount =
                    new BigDecimal(jsonObject.get("data").toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
                sum = sunAamount;
            }
        }
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        // 存入缓存
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_AE;
        log.info("查询AE总余额{}=============================================》", sum);
        redisUtil.set(key, sum, Constants.THIRD_BALANCE_ALL);
        redisUtil.set(key + "TIME", DateUtil.dateToString(new Date(), DateUtil.patten), Constants.THIRD_BALANCE_ALL);
    }

    public void setRedisVNCMoneyTotal() {
        JSONObject jsonObject = userMoneyService.refreshVNC(null, false);
        BigDecimal sum = null;
        if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"), jsonObject.get("msg"))) {
            sum = BigDecimal.ZERO;
        } else {
            Integer code = (Integer)jsonObject.get("code");
            if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))) {
                BigDecimal sunAamount =
                    new BigDecimal(jsonObject.get("data").toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
                sum = sunAamount;
            }
        }
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        // 存入缓存
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_VNC;
        log.info("查询VNC总余额{}=============================================》", sum);
        redisUtil.set(key, sum, Constants.THIRD_BALANCE_ALL);
        redisUtil.set(key + "TIME", DateUtil.dateToString(new Date(), DateUtil.patten), Constants.THIRD_BALANCE_ALL);
    }

    public void setRedisDGMoneyTotal(List<UserThird> dgThird, String platform, long time) {
        if (LoginUtil.checkNull(dgThird) || dgThird.size() == CommonConst.NUMBER_0) {
            return;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(dgThird.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u : dgThird) {
            threadPool.execute(() -> {
                try {
                    if (ExpirationTimeUtil.check(platform, u.getUserId().toString(), time, ExpirationTimeUtil.universality)) {
                        log.info("平台{}查询{}余额走缓存",platform, u.getUserId().toString());
                        list.add(ExpirationTimeUtil.getTripartiteBalance(platform, u.getUserId().toString()));
                    } else {
                        JSONObject jsonObject = userMoneyService.refreshDG(u.getUserId(), true);
                        if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"), jsonObject.get("msg"))) {
                            list.add(BigDecimal.ZERO);
                        }
                        try {
                            Integer code = (Integer)jsonObject.get("code");
                            if (code == CommonConst.NUMBER_0) {
                                if (LoginUtil.checkNull(jsonObject.get("data"))) {
                                    list.add(BigDecimal.ZERO);
                                }else {
                                    list.add(new BigDecimal(jsonObject.get("data").toString()));
                                }
                            } else {
                                list.add(BigDecimal.ZERO);
                            }
                        } catch (Exception ex) {
                            list.add(BigDecimal.ZERO);
                        }
                    }
                } finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);
        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        String key = Constants.REDIS_THRID_SUMBALANCE + platform;
        redisUtil.set(key, sum, Constants.THIRD_BALANCE_ALL);
        redisUtil.set(key + "TIME", DateUtil.dateToString(new Date(), DateUtil.patten), Constants.THIRD_BALANCE_ALL);
    }
}
