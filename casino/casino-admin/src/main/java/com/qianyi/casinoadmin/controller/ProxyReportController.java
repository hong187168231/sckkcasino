package com.qianyi.casinoadmin.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinoadmin.vo.ProxyReportVo;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.BillThreadPool;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/proxyReport")
@Api(tags = "人人代管理")
public class ProxyReportController {
    @Autowired
    private UserService userService;

    @Autowired
    private ShareProfitChangeService shareProfitChangeService;

    @Autowired
    private GameRecordService gameRecordService;

    public final static  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public final static  SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public final static String start = " 00:00:00";

    public final static String end = " 23:59:59";

    private static final BillThreadPool threadPool = new BillThreadPool(CommonConst.NUMBER_20);

    @ApiOperation("查询人人代报表")
    @GetMapping("/find")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "账号", required = true),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity<ProxyReportVo> find(String userName, @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                              @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (LoginUtil.checkNull(userName,startDate,endDate)){
            return ResponseUtil.custom("参数必填");
        }
        User byAccount = userService.findByAccount(userName);
        if (LoginUtil.checkNull(byAccount)){
            return ResponseUtil.custom("找不到该会员");
        }
        String startTime = format.format(startDate);
        String endTime = format.format(endDate);
        Vector<ProxyReportVo> list = new Vector<>();
        Map<Long, BigDecimal> map = new ConcurrentHashMap<>();
        Map<Long, Integer> userMap = new ConcurrentHashMap<>();
        long start = System.currentTimeMillis();
        this.assemble(map,userMap,byAccount,byAccount.getId(),startTime,endTime,list,startDate,endDate,CommonConst.NUMBER_0,CommonConst.NUMBER_0);
        log.info("==1耗时{}",System.currentTimeMillis()-start);
        start = System.currentTimeMillis();
        List<User> firstUsers = userService.findFirstUser(byAccount.getId());
        if (!LoginUtil.checkNull(firstUsers) && firstUsers.size() > CommonConst.NUMBER_0) {
            List<User> secondPid = userService.findBySecondPid(byAccount.getId());
            List<User> thirdPid = userService.findByThirdPid(byAccount.getId());
            ReentrantLock reentrantLock = new ReentrantLock();
            Condition condition = reentrantLock.newCondition();
            AtomicInteger atomicInteger = new AtomicInteger(firstUsers.size()+secondPid.size()+thirdPid.size());
            firstUsers.forEach(f -> {
                threadPool.execute(() -> assemble(map, userMap, f, byAccount.getId(), startTime, endTime, list, startDate, endDate, CommonConst.NUMBER_1, CommonConst.NUMBER_1,reentrantLock, condition, atomicInteger));
            });
            log.info("==2耗时{}", System.currentTimeMillis() - start);
            start = System.currentTimeMillis();

            secondPid.forEach(s -> {
                threadPool.execute(() -> assemble(map, userMap, s, byAccount.getId(), startTime, endTime, list, startDate, endDate, CommonConst.NUMBER_2, CommonConst.NUMBER_2,reentrantLock, condition, atomicInteger));
            });
            log.info("==3耗时{}", System.currentTimeMillis() - start);
            start = System.currentTimeMillis();
            thirdPid.forEach(t -> {
                threadPool.execute(() -> assemble(map, userMap, t, byAccount.getId(), startTime, endTime, list, startDate, endDate, CommonConst.NUMBER_3, CommonConst.NUMBER_3,reentrantLock, condition, atomicInteger));
            });
            log.info("==4耗时{}", System.currentTimeMillis() - start);
            BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);
        }
        if (list.size() > CommonConst.NUMBER_0){
            log.info("集合长度list{},map{},userMap{}",list.size(),map.size(),userMap.size());
            List<Long> userIds = list.stream().map(ProxyReportVo::getFirstPid).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            list.stream().forEach(proxyReportVo ->{
                proxyReportVo.setAllPerformance(map.get(proxyReportVo.getUserId()).subtract(proxyReportVo.getPerformance()));
                proxyReportVo.setAllGroupNum(userMap.get(proxyReportVo.getUserId()));
                userList.stream().forEach(user->{
                    if (user.getId().equals(proxyReportVo.getFirstPid())){
                        proxyReportVo.setFirstPidAccount(user.getAccount());
                    }
                });
            });
            userIds.clear();
            userList.clear();
        }
        map.clear();
        userMap.clear();
        return this.getData(list);
    }
    @ApiOperation("下级明细")
    @GetMapping("/findDetail")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "当前列id", required = true),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
            @ApiImplicitParam(name = "tier", value = "当前层级 层级 0 当前 1 一级 2 二级 3 三级", required = true),
    })
    public ResponseEntity<ProxyReportVo> findDetail(Long id, @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                              @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate,Integer tier){
        if (LoginUtil.checkNull(id,startDate,endDate,tier)){
            return ResponseUtil.custom("参数必填");
        }
        Vector<ProxyReportVo> list = new Vector<>();
        User user = userService.findById(id);
        if (LoginUtil.checkNull(user)){
            return ResponseUtil.success(list);
        }
        Long userId = null;
        if (tier == CommonConst.NUMBER_1){
            userId = user.getFirstPid();
        }else if (tier == CommonConst.NUMBER_2){
            userId = user.getSecondPid();
        }
        String startTime = format.format(startDate);
        String endTime = format.format(endDate);
        Map<Long, BigDecimal> map = new ConcurrentHashMap<>();
        Map<Long, Integer> userMap = new ConcurrentHashMap<>();
        this.assemble(map,userMap,user,userId,startTime,endTime,list,startDate,endDate,tier,CommonConst.NUMBER_0);
        List<User> firstUsers = userService.findFirstUser(user.getId());
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        if (tier == CommonConst.NUMBER_1){
            if (!LoginUtil.checkNull(firstUsers) && firstUsers.size() > CommonConst.NUMBER_0){
                List<User> secondPid = userService.findBySecondPid(user.getId());
                AtomicInteger atomicInteger = new AtomicInteger(firstUsers.size()+secondPid.size());
                firstUsers.forEach(f ->{
                    threadPool.execute(() -> assemble(map,userMap,f,user.getFirstPid(),startTime,endTime,list,startDate,endDate,CommonConst.NUMBER_2,CommonConst.NUMBER_1,reentrantLock, condition, atomicInteger));
                });
                secondPid.forEach(s ->{
                    threadPool.execute(() -> assemble(map,userMap,s,user.getFirstPid(),startTime,endTime,list,startDate,endDate,CommonConst.NUMBER_3,CommonConst.NUMBER_2,reentrantLock, condition, atomicInteger));
                });
                BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);
            }
        }else if(tier == CommonConst.NUMBER_2){
            if (!LoginUtil.checkNull(firstUsers) && firstUsers.size() > CommonConst.NUMBER_0){
                AtomicInteger atomicInteger = new AtomicInteger(firstUsers.size());
                firstUsers.forEach(f ->{
                    threadPool.execute(() -> assemble(map,userMap,f,user.getSecondPid(),startTime,endTime,list,startDate,endDate,CommonConst.NUMBER_3,CommonConst.NUMBER_1,reentrantLock, condition, atomicInteger));
                });
                BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);
            }
        }else {
            return ResponseUtil.custom("参数不合法");
        }
        if (list.size() > CommonConst.NUMBER_0){
            List<Long> userIds = list.stream().map(ProxyReportVo::getFirstPid).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            list.stream().forEach(proxyReportVo ->{
                proxyReportVo.setAllPerformance(map.get(proxyReportVo.getUserId()).subtract(proxyReportVo.getPerformance()));
                proxyReportVo.setAllGroupNum(userMap.get(proxyReportVo.getUserId()));
                userList.stream().forEach(u->{
                    if (u.getId().equals(proxyReportVo.getFirstPid())){
                        proxyReportVo.setFirstPidAccount(u.getAccount());
                    }
                });
            });
            userIds.clear();
            userList.clear();
        }
        map.clear();
        userMap.clear();
        return this.getData(list);
    }
    private ResponseEntity<ProxyReportVo> getData(List<ProxyReportVo> list){
        ProxyReportVo proxyReportVo = new ProxyReportVo();
        proxyReportVo.setPerformance(list.stream().map(ProxyReportVo::getPerformance).reduce(BigDecimal.ZERO, BigDecimal::add));
        proxyReportVo.setContribution(list.stream().map(ProxyReportVo::getContribution).reduce(BigDecimal.ZERO, BigDecimal::add));
        JSONObject jsonObject = new JSONObject();
        Collections.sort(list, new ProxyReportVo());
        jsonObject.put("data", list);
        jsonObject.put("sum", proxyReportVo);
        return ResponseUtil.success(jsonObject);
    }
    private ResponseEntity<ProxyReportVo> getDataDetail(List<ProxyReportVo> list){
        ProxyReportVo proxyReportVo = new ProxyReportVo();
        proxyReportVo.setPerformance(list.stream().map(ProxyReportVo::getPerformance).reduce(BigDecimal.ZERO, BigDecimal::add));
        proxyReportVo.setContribution(list.stream().map(ProxyReportVo::getContribution).reduce(BigDecimal.ZERO, BigDecimal::add));
        proxyReportVo.setAllPerformance(list.stream().map(ProxyReportVo::getAllPerformance).reduce(BigDecimal.ZERO, BigDecimal::add));
        JSONObject jsonObject = new JSONObject();
        Collections.sort(list, new ProxyReportVo());
        jsonObject.put("data", list);
        jsonObject.put("sum", proxyReportVo);
        return ResponseUtil.success(jsonObject);
    }
    @ApiOperation("每日结算细节")
    @GetMapping("/findDayDetail")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "当前列id", required = true),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
            @ApiImplicitParam(name = "tier", value = "当前层级 层级 0 当前 1 一级 2 二级 3 三级", required = true),
    })
    public ResponseEntity<ProxyReportVo> findDayDetail(Long id, @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate,Integer tier){
        if (LoginUtil.checkNull(id,startDate,endDate)){
            return ResponseUtil.custom("参数必填");
        }
        Vector<ProxyReportVo> list = new Vector<>();
        User user = userService.findById(id);
        if (LoginUtil.checkNull(user)){
            return ResponseUtil.success(list);
        }
        Map<Integer,String> mapDate = this.findDates("D", startDate, endDate);

        if (LoginUtil.checkNull(mapDate) || mapDate.size() == CommonConst.NUMBER_0){
            return ResponseUtil.success(list);
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(mapDate.size());
        mapDate.forEach((k,date)->{
            String startTime = date+start;
            String endTime  = date+end;
            try {
                Date start = formatter.parse(startTime);
                Date end = formatter.parse(endTime);
                threadPool.execute(() -> asyn(k,date,startTime,endTime,start,end,tier,id,user,list,reentrantLock, condition, atomicInteger));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);
        Collections.sort(list, new ProxyReportVo());
        return this.getDataDetail(list);
    }
    private void asyn(Integer key,String date,String startTime,String endTime,Date startDate,Date endDate,Integer tier,Long id,User user,List<ProxyReportVo> list, ReentrantLock reentrantLock, Condition condition, AtomicInteger atomicInteger){
        synchronized (key.toString().intern()){
            List<BigDecimal> allPerformances = new ArrayList<>();
            ProxyReportVo proxyReportVo = null;
            try {
                if (tier == CommonConst.NUMBER_0){
                    proxyReportVo = this.assemble(null,id,date,startTime,endTime,startDate,endDate,CommonConst.NUMBER_0);
                    List<User> firstUsers = userService.findFirstUser(id);
                    if (!LoginUtil.checkNull(firstUsers) && firstUsers.size() > CommonConst.NUMBER_0){
                        firstUsers.forEach(f ->{
                            this.assemble(f.getId(),date,allPerformances);
                        });
                        List<User> secondPid = userService.findBySecondPid(id);
                        if (!LoginUtil.checkNull(secondPid) && secondPid.size() > CommonConst.NUMBER_0){
                            secondPid.forEach(s ->{
                                this.assemble(s.getId(),date,allPerformances);
                            });
                            List<User> thirdPid = userService.findByThirdPid(id);
                            if (!LoginUtil.checkNull(thirdPid) && thirdPid.size() > CommonConst.NUMBER_0){
                                thirdPid.forEach(t ->{
                                    this.assemble(t.getId(),date,allPerformances);
                                });
                            }
                        }
                    }
                    proxyReportVo.setSort(key);
                    proxyReportVo.setAllPerformance(allPerformances.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
                }else if (tier == CommonConst.NUMBER_1){
                    proxyReportVo = this.assemble(id,user.getFirstPid(),date,startTime,endTime,startDate,endDate,CommonConst.NUMBER_1);
                    List<User> firstUsers = userService.findFirstUser(id);
                    if (!LoginUtil.checkNull(firstUsers) && firstUsers.size() > CommonConst.NUMBER_0){
                        firstUsers.forEach(f ->{
                            this.assemble(f.getId(),date,allPerformances);
                        });
                        List<User> secondPid = userService.findBySecondPid(id);
                        if (!LoginUtil.checkNull(secondPid) && secondPid.size() > CommonConst.NUMBER_0){
                            secondPid.forEach(s ->{
                                this.assemble(s.getId(),date,allPerformances);
                            });
                        }
                    }
                    proxyReportVo.setSort(key);
                    proxyReportVo.setAllPerformance(allPerformances.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
                }else if (tier == CommonConst.NUMBER_2){
                    proxyReportVo = this.assemble(id,user.getSecondPid(),date,startTime,endTime,startDate,endDate,CommonConst.NUMBER_2);
                    List<User> firstUsers = userService.findFirstUser(id);
                    if (!LoginUtil.checkNull(firstUsers) && firstUsers.size() > CommonConst.NUMBER_0){
                        firstUsers.forEach(f ->{
                            this.assemble(f.getId(),date,allPerformances);
                        });
                    }
                    proxyReportVo.setSort(key);
                    proxyReportVo.setAllPerformance(allPerformances.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
                }else {
                    proxyReportVo = this.assemble(id,user.getThirdPid(),date,startTime,endTime,startDate,endDate,CommonConst.NUMBER_3);
                    proxyReportVo.setSort(key);
                }
                list.add(proxyReportVo);
            }catch (Exception e){
                log.error("每日结算细节统计失败{}",e);
            }finally {
                allPerformances.clear();
                atomicInteger.decrementAndGet();
                BillThreadPool.toResume(reentrantLock, condition);
            }
        }
    }

    private void  assemble(Long userId,String date,List<BigDecimal> allPerformances){
        String startTime = date+start;
        String endTime  = date+end;
        GameRecord gameRecord = gameRecordService.findRecordRecordSum(userId, startTime, endTime);
        allPerformances.add((gameRecord == null || gameRecord.getValidbet() == null) ? BigDecimal.ZERO:new BigDecimal(gameRecord.getValidbet()));
    }
    private ProxyReportVo assemble(Long id,Long userId,String date,String startTime,String endTime,Date startDate,Date endDate,Integer tag){
        ProxyReportVo proxyReportVo = new ProxyReportVo();
        proxyReportVo.setStaticsTimes(date);
        List<ShareProfitChange> shareProfitChanges = shareProfitChangeService.findAll(id,userId,startDate, endDate);
        BigDecimal contribution = shareProfitChanges.stream().map(ShareProfitChange::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        contribution = contribution.setScale(CommonConst.NUMBER_4, RoundingMode.HALF_UP);
        proxyReportVo.setContribution(contribution);
        if (tag != CommonConst.NUMBER_0){
            GameRecord gameRecord = gameRecordService.findRecordRecordSum(id, startTime, endTime);
            proxyReportVo.setPerformance((gameRecord == null || gameRecord.getValidbet() == null) ? BigDecimal.ZERO:new BigDecimal(gameRecord.getValidbet()));
            if (shareProfitChanges == null || shareProfitChanges.size() == CommonConst.NUMBER_0){
                proxyReportVo.setCommission("0");
            }else {
                BigDecimal commission = shareProfitChanges.get(CommonConst.NUMBER_0).getProfitRate();
                commission = commission.setScale(CommonConst.NUMBER_3, RoundingMode.HALF_UP);
                proxyReportVo.setCommission(commission + "%");
            }
        }
        return proxyReportVo;
    }

    private void assemble(Map<Long,BigDecimal> map,Map<Long,Integer> userMap,User user,Long userId,String startTime,String endTime,List<ProxyReportVo> list,
                          Date startDate,Date endDate,Integer tag,Integer level){
        ProxyReportVo proxyReportVo = new ProxyReportVo();
        proxyReportVo.setTier(tag);
        GameRecord gameRecord = gameRecordService.findRecordRecordSum(user.getId(), startTime+start, endTime+end);
        proxyReportVo.setPerformance((gameRecord == null || gameRecord.getValidbet() == null) ? BigDecimal.ZERO:new BigDecimal(gameRecord.getValidbet()));
        this.compute(map,user,level,proxyReportVo.getPerformance(),userMap);
        if (tag == CommonConst.NUMBER_0){
            proxyReportVo.setContribution(BigDecimal.ZERO);
        }else {
            List<ShareProfitChange> shareProfitChanges = shareProfitChangeService.findAll(user.getId(),userId, startDate, endDate);
            BigDecimal contribution = shareProfitChanges.stream().map(ShareProfitChange::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            contribution = contribution.setScale(CommonConst.NUMBER_4, RoundingMode.HALF_UP);
            proxyReportVo.setContribution(contribution);
        }
        proxyReportVo.setUserId(user.getId());
        proxyReportVo.setAccount(user.getAccount());
        proxyReportVo.setFirstPid(user.getFirstPid());
        proxyReportVo.setSort(tag);
        list.add(proxyReportVo);
    }
    private void assemble(Map<Long,BigDecimal> map,Map<Long,Integer> userMap,User user,Long userId,String startTime,String endTime,List<ProxyReportVo> list,
                          Date startDate,Date endDate,Integer tag,Integer level,ReentrantLock reentrantLock, Condition condition, AtomicInteger atomicInteger){
        synchronized (user.getId().toString().intern()){
            try {
                this.assemble(map,userMap,user,userId,startTime,endTime,list,startDate,endDate,tag,level);
            }finally {
                atomicInteger.decrementAndGet();
                BillThreadPool.toResume(reentrantLock, condition);
            }
        }
    }
    private void compute(Map<Long,BigDecimal> map,User user,Integer tag,BigDecimal performance,Map<Long,Integer> userMap){
        if (tag == CommonConst.NUMBER_0){
            map.put(user.getId(),performance);
            userMap.put(user.getId(),CommonConst.NUMBER_0);
        }else if(tag == CommonConst.NUMBER_1){
            map.put(user.getId(),performance.add(map.get(user.getId()) == null?BigDecimal.ZERO:map.get(user.getId())));
            map.put(user.getFirstPid(),performance.add(map.get(user.getFirstPid()) == null?BigDecimal.ZERO:map.get(user.getFirstPid())));
            userMap.put(user.getId(),userMap.get(user.getId())==null?CommonConst.NUMBER_0:userMap.get(user.getId()));
            userMap.put(user.getFirstPid(),userMap.get(user.getFirstPid())==null?CommonConst.NUMBER_0:userMap.get(user.getFirstPid()) + CommonConst.NUMBER_1);
        }else if(tag == CommonConst.NUMBER_2){
            map.put(user.getId(),performance.add(map.get(user.getId()) == null?BigDecimal.ZERO:map.get(user.getId())));
            map.put(user.getFirstPid(),performance.add(map.get(user.getFirstPid()) == null?BigDecimal.ZERO:map.get(user.getFirstPid())));
            map.put(user.getSecondPid(),performance.add(map.get(user.getSecondPid()) == null?BigDecimal.ZERO:map.get(user.getSecondPid())));
            userMap.put(user.getId(),userMap.get(user.getId())==null?CommonConst.NUMBER_0:userMap.get(user.getId()));
            userMap.put(user.getFirstPid(),userMap.get(user.getFirstPid())==null?CommonConst.NUMBER_0:userMap.get(user.getFirstPid()) + CommonConst.NUMBER_1);
            userMap.put(user.getSecondPid(),userMap.get(user.getSecondPid())==null?CommonConst.NUMBER_0:userMap.get(user.getSecondPid()) + CommonConst.NUMBER_1);
        }else {
            map.put(user.getId(),performance.add(map.get(user.getId()) == null?BigDecimal.ZERO:map.get(user.getId())));
            map.put(user.getFirstPid(),performance.add(map.get(user.getFirstPid())) == null?BigDecimal.ZERO:map.get(user.getFirstPid()));
            map.put(user.getSecondPid(),performance.add(map.get(user.getSecondPid()) == null?BigDecimal.ZERO:map.get(user.getSecondPid())));
            map.put(user.getThirdPid(),performance.add(map.get(user.getThirdPid()) == null?BigDecimal.ZERO:map.get(user.getThirdPid())));
            userMap.put(user.getId(),userMap.get(user.getId())==null?CommonConst.NUMBER_0:userMap.get(user.getId()));
            userMap.put(user.getFirstPid(),userMap.get(user.getFirstPid())==null?CommonConst.NUMBER_0:userMap.get(user.getFirstPid())+ CommonConst.NUMBER_1);
            userMap.put(user.getSecondPid(),userMap.get(user.getSecondPid())==null?CommonConst.NUMBER_0:userMap.get(user.getSecondPid()) + CommonConst.NUMBER_1);
            userMap.put(user.getThirdPid(),userMap.get(user.getThirdPid())==null?CommonConst.NUMBER_0:userMap.get(user.getThirdPid()) + CommonConst.NUMBER_1);
        }

    }
    public static Map<Integer,String> findDates(String dateType, Date dBegin, Date dEnd){
        Map<Integer,String> mapDate = new HashMap<>();
        Calendar calBegin = Calendar.getInstance();
        calBegin.setTime(dBegin);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(dEnd);
        Integer count = CommonConst.NUMBER_0;
        while (calEnd.after(calBegin)) {
            count++;
            if (calEnd.after(calBegin))
                mapDate.put(count,new SimpleDateFormat("yyyy-MM-dd").format(calBegin.getTime()));
            else
                mapDate.put(count,new SimpleDateFormat("yyyy-MM-dd").format(calBegin.getTime()));
            switch (dateType) {
                case "M":
                    calBegin.add(Calendar.MONTH, 1);
                    break;
                case "D":
                    calBegin.add(Calendar.DAY_OF_YEAR, 1);break;
                case "H":
                    calBegin.add(Calendar.HOUR, 1);break;
                case "N":
                    calBegin.add(Calendar.SECOND, 1);break;
            }
        }
        return mapDate;
    }
}
