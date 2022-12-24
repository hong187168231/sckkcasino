package com.qianyi.casinoadmin.task;

import com.beust.jcommander.internal.Lists;
import com.qianyi.casinoadmin.service.ThridUserBalanceSumService;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.service.UserThirdService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.TaskConst;
import com.qianyi.modulecommon.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ThirdGameSumBalanceTask {

    @Autowired
    UserThirdService userThirdService;


    @Autowired
    private ThridUserBalanceSumService thridUserBalanceSumService;


    /**
     *
     */
    @Scheduled(cron = TaskConst.THIRD_GAME_SUM)
    public void create(){
        log.info("查询三方总余额统计开始start=============================================》");
        long startTime = System.currentTimeMillis();
        //查询WM总余额
        List<UserThird> allAcount = userThirdService.findAllAcount();
        if (LoginUtil.checkNull(allAcount) || allAcount.size() == CommonConst.NUMBER_0) return ;


        List<UserThird> wmThird = allAcount.stream().filter(o -> Objects.nonNull(o.getAccount())).collect(Collectors.toList());
        List<UserThird> pgCq9Third = allAcount.stream().filter(o -> Objects.nonNull(o.getGoldenfAccount())).collect(Collectors.toList());
        List<UserThird> obdjThird = allAcount.stream().filter(o -> Objects.nonNull(o.getObdjAccount())).collect(Collectors.toList());
        List<UserThird> obtyThird = allAcount.stream().filter(o -> Objects.nonNull(o.getObtyAccount())).collect(Collectors.toList());
        List<UserThird> sabaThird = allAcount.stream().filter(o -> Objects.nonNull(o.getGoldenfAccount())).collect(Collectors.toList());
        List<UserThird> dgThird = allAcount.stream().filter(o -> Objects.nonNull(o.getDgAccount())).collect(Collectors.toList());
        //异步方法，查询三方总余额，缓存到redis
        long startTimeAE = System.currentTimeMillis();
        try {
            thridUserBalanceSumService.setRedisAEMoneyTotal();
        }catch (Exception e){
            log.error("AE余额查询失败：【{}】", e.getMessage());
        }
        log.info("查询AE总余额统计结束end耗时{}=============================================》",System.currentTimeMillis()-startTimeAE);

        long startTimeVNC = System.currentTimeMillis();
        try {
            thridUserBalanceSumService.setRedisVNCMoneyTotal();
        }catch (Exception e){
            log.error("VNC余额查询失败：【{}】", e.getMessage());
        }
        log.info("查询VNC总余额统计结束end耗时{}=============================================》",System.currentTimeMillis()-startTimeVNC);

        long startTimeWM = System.currentTimeMillis();
        //WM总余额
        try {
            thridUserBalanceSumService.setRedisWMMoneyTotal(wmThird, Constants.PLATFORM_WM_BIG,startTime);
        }catch (Exception e){
            log.error("WM余额查询失败：【{}】", e.getMessage());
        }
        log.info("查询WM总余额统计结束end耗时{}=============================================》",System.currentTimeMillis()-startTimeWM);
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            log.error("暂停【{}】", e.getMessage());
        }
        long startTimePG = System.currentTimeMillis();
        //PG/CQ9总余额
        try {
            thridUserBalanceSumService.setRedisPGMoneyTotal(pgCq9Third, Constants.PLATFORM_PG_CQ9,startTime);
        }catch (Exception e){
            log.error("CQ9余额查询失败：【{}】", e.getMessage());
        }
        log.info("查询PG总余额统计结束end耗时{}=============================================》",System.currentTimeMillis()-startTimePG);
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            log.error("暂停【{}】", e.getMessage());
        }
        long startTimeOBDJ = System.currentTimeMillis();
        //查询OB电竞总余额
        try {
            thridUserBalanceSumService.setRedisOBDJMoneyTotal(obdjThird, Constants.PLATFORM_OBDJ,startTime);
        }catch (Exception e){
            log.error("OBDJ余额查询失败：【{}】", e.getMessage());
        }
        log.info("查询OBDJ总余额统计结束end耗时{}=============================================》",System.currentTimeMillis()-startTimeOBDJ);
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            log.error("暂停【{}】", e.getMessage());
        }
        long startTimeOBTY = System.currentTimeMillis();
        //查询OB体育总余额
        try {
            thridUserBalanceSumService.setRedisOBTYMoneyTotal(obtyThird, Constants.PLATFORM_OBTY,startTime);
        }catch  (Exception e){
            log.error("OBTY余额查询失败：【{}】", e.getMessage());
        }
        log.info("查询OBTY总余额统计结束end耗时{}=============================================》",System.currentTimeMillis()-startTimeOBTY);

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            log.error("暂停【{}】", e.getMessage());
        }
        long startTimeSABA = System.currentTimeMillis();
        //查询沙巴体育总余额
        try {
            thridUserBalanceSumService.setRedisSABAMoneyTotal(sabaThird, Constants.PLATFORM_SABASPORT,startTime);
        }catch (Exception e){
            log.error("沙巴余额查询失败：【{}】", e.getMessage());
        }
        log.info("查询SABA总余额统计结束end耗时{}=============================================》",System.currentTimeMillis()-startTimeSABA);

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            log.error("暂停【{}】", e.getMessage());
        }
        long startTimeDg = System.currentTimeMillis();
        try {
            thridUserBalanceSumService.setRedisDGMoneyTotal(dgThird, Constants.PLATFORM_DG,startTime);
        }catch (Exception e){
            log.error("DG余额查询失败：【{}】", e.getMessage());
        }
        log.info("查询DG总余额统计结束end耗时{}=============================================》",System.currentTimeMillis()-startTimeDg);
        log.info("查询三方总余额统计结束end总耗时{}=============================================》",System.currentTimeMillis()-startTime);
    }


}
