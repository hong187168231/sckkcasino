package com.qianyi.casinoadmin.install;

import com.qianyi.casinoadmin.task.ThirdGameSumBalanceTask;
import com.qianyi.casinocore.business.ExportReportBusiness;
import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.GameRecordGoldenF;
import com.qianyi.casinocore.service.GameRecordGoldenFService;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.casinocore.service.ProxyGameRecordReportService;
import com.qianyi.casinocore.service.UserGameRecordReportService;
import com.qianyi.casinocore.util.UserPasswordUtil;
import com.qianyi.casinocore.vo.ProxyGameRecordReportVo;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
@Slf4j
@Order(9)
public class AddData implements CommandLineRunner {
    @Autowired
    private GameRecordService gameRecordService;
    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    @Qualifier("proxyGameRecordReportJob")
    private AsyncService asyncService;

    @Autowired
    private ProxyGameRecordReportService proxyGameRecordReportService;

    @Autowired
    private UserGameRecordReportService userGameRecordReportService;

    @Autowired
    private ThirdGameSumBalanceTask thirdGameSumBalanceTask;

    @Autowired
    private ExportReportBusiness exportReportBusiness;

    private static final Integer pageSize = 1000;

    public static final Integer num = 50000;
    @Override
    public void run(String... args) throws Exception {
        log.info("初始化计算数据开始==============================================>");
        long startTime = System.currentTimeMillis();
//        Calendar nowTime = Calendar.getInstance();
////        //计算最近十天注单
//        nowTime.add(Calendar.DATE, -200);
//        Date startDate = nowTime.getTime();
//        String startDay = DateUtil.getSimpleDateFormat(DateUtil.patten1).format(startDate);
//        String yesterday = DateUtil.getSimpleDateFormat(DateUtil.patten1).format(DateUtil.getYesterday());
//        List<String> betweenDate = DateUtil.getBetweenDate(startDay, yesterday);

//        List<String> betweenDate = new ArrayList<>();
//        betweenDate.add("2022-12-18");
//        betweenDate.add("2022-12-19");
//        betweenDate.add("2022-12-20");
//        betweenDate.add("2022-12-21");
//        betweenDate.add("2022-12-22");
//        betweenDate.add("2022-12-23");
//        betweenDate.add("2022-12-24");
//        for (String str:betweenDate){
//            this.delete(str);
//            userGameRecordReportService.comparison(str);
//            proxyGameRecordReportService.comparison(str);
//
////            exportReportBusiness.comparison(str);
//        }
//        for (String str:betweenDate){
//            exportReportBusiness.comparison(str);
//        }
//        thirdGameSumBalanceTask.create();

        new Thread(()->{
            this.asynchronization();
        }).start();
        log.info("初始化计算数据结束耗时{}==============================================>",System.currentTimeMillis()-startTime);
    }

    private void asynchronization(){
        log.info("异步初始化计算数据开始==============================================>");
        long startTime = System.currentTimeMillis();
        Calendar nowTime = Calendar.getInstance();
        //        //计算最近十天注单
        nowTime.add(Calendar.DATE, -50);
        Date startDate = nowTime.getTime();
        String startDay = DateUtil.getSimpleDateFormat(DateUtil.patten1).format(startDate);
        String yesterday = DateUtil.getSimpleDateFormat(DateUtil.patten1).format(DateUtil.getYesterday());
        List<String> betweenDate = DateUtil.getBetweenDate(startDay, yesterday);
//        for (String str:betweenDate){
//            this.delete(str);
//            userGameRecordReportService.comparison(str);
//            proxyGameRecordReportService.comparison(str);
//
//            exportReportBusiness.comparison(str);
//        }
        Collections.reverse(betweenDate);
        for (String str : betweenDate) {
            exportReportBusiness.comparison(str);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("异步初始化计算数据结束耗时{}==============================================>",System.currentTimeMillis()-startTime);
    }


    private void delete(String yesterday){
        try {
            log.info("删除昨日数据{}",yesterday);
            proxyGameRecordReportService.deleteByOrderTimes(yesterday);

            userGameRecordReportService.deleteByOrderTimes(yesterday);
        }catch (Exception ex){
            log.error("删除昨日数据失败{}",yesterday);
        }
    }

    private void recursionWm(Integer pageCode,Integer pageSize,Long maxId){
        log.info("GameRecord pageCode {} pageSize {}",pageCode,pageSize);
        Sort sort = Sort.by("id").ascending();
        Pageable pageable = PageRequest.of(pageCode-1, pageSize,sort);
        GameRecord game = new GameRecord();
        game.setId(maxId);
        Page<GameRecord> gameRecordPage = gameRecordService.findGameRecordPage(game, pageable,null,null,null,null);
        List<GameRecord> list = gameRecordPage.getContent();
        if (list == null || list.isEmpty()){
            return;
        }
        list.forEach(g->{
            ProxyGameRecordReportVo vo = new ProxyGameRecordReportVo();
            vo.setOrderId(g.getBetId());
            vo.setFirstProxy(g.getFirstProxy());
            vo.setSecondProxy(g.getSecondProxy());
            vo.setThirdProxy(g.getThirdProxy());
            vo.setOrderTimes(g.getBetTime());
            vo.setUserId(g.getUserId());
            vo.setValidAmount(new BigDecimal(g.getValidbet()));
            vo.setWinLoss(new BigDecimal(g.getWinLoss()));
            vo.setBetAmount(new BigDecimal(g.getBet()));
            vo.setPlatform("wm");
            asyncService.executeAsync(vo);
        });
        log.info("GameRecord list.size {} pageCode {} pageSize {}",list.size(),pageCode,pageSize);
        if (list.size() == pageSize){
            pageCode++;
            recursionWm(pageCode,pageSize,maxId);
        }
    }

    private void recursionPg(Integer pageCode,Integer pageSize,Long maxId){
        log.info("GameRecordGoldenF pageCode {} pageSize {}",pageCode,pageSize);
        Sort sort = Sort.by("id").ascending();
        Pageable pageable = PageRequest.of(pageCode-1, pageSize,sort);
        GameRecordGoldenF gameRecordGoldenF = new GameRecordGoldenF();
        gameRecordGoldenF.setId(maxId);
        Page<GameRecordGoldenF> gameRecordPage = gameRecordGoldenFService.findGameRecordGoldenFPage(gameRecordGoldenF, pageable,null,null);
        List<GameRecordGoldenF> list = gameRecordPage.getContent();
        if (list == null || list.isEmpty()){
            return;
        }
        list.forEach(g -> {
            ProxyGameRecordReportVo vo = new ProxyGameRecordReportVo();
            vo.setOrderId(g.getTraceId());
            vo.setFirstProxy(g.getFirstProxy());
            vo.setSecondProxy(g.getSecondProxy());
            vo.setThirdProxy(g.getThirdProxy());
            vo.setOrderTimes(g.getCreateAtStr());
            vo.setUserId(g.getUserId());
            vo.setValidAmount(g.getBetAmount());
            vo.setWinLoss(g.getWinAmount().subtract(g.getBetAmount()));
            vo.setBetAmount(g.getBetAmount());
            vo.setPlatform(g.getVendorCode());
            asyncService.executeAsync(vo);
        });
        log.info("GameRecordGoldenF list.size {} pageCode {} pageSize {}",list.size(),pageCode,pageSize);
        if (list.size() == pageSize){
            pageCode++;
            recursionPg(pageCode,pageSize,maxId);
        }
    }

    private void beginWM1(){
        log.info("开始执行beginWM1");
        GameRecord byBetId = gameRecordService.findByBetId("179170088");
        log.info("beginWM1:byBetId{}",byBetId);
        for (int i=0;i<=num;i++){
            byBetId.setId(null);
            byBetId.setUserId(60951L);
            byBetId.setBetId(UserPasswordUtil.getRandomPwd()+UserPasswordUtil.getRandomPwd());
            byBetId.setBetTime(DateUtil.dateToPatten(new Date()));
            gameRecordService.save(byBetId);
        }
    }
    private void beginWM2(){
        log.info("开始执行beginWM2");
        GameRecord byBetId = gameRecordService.findByBetId("179170087");
        for (int i=0;i<=num;i++){
            byBetId.setId(null);
            byBetId.setUserId(60952L);
            byBetId.setBetId(UserPasswordUtil.getRandomPwd()+UserPasswordUtil.getRandomPwd());
            byBetId.setBetTime(DateUtil.dateToPatten(new Date()));
            gameRecordService.save(byBetId);
        }
    }
    private void beginPG1(){
        log.info("开始执行beginPG1");
        GameRecordGoldenF gameRecordById = gameRecordGoldenFService.findGameRecordById(6959L);
        log.info("beginPG1:gameRecordById{}",gameRecordById);
        Date date = new Date();
        String format = DateUtil.getSimpleDateFormat().format(date);
        for (int i=0;i<=num;i++){
            gameRecordById.setId(null);
            gameRecordById.setUserId(60835L);
            gameRecordById.setCreateAtStr(format);
            gameRecordById.setTraceId(UserPasswordUtil.getRandomPwd()+UserPasswordUtil.getRandomPwd());
            gameRecordById.setCreateAtStr(DateUtil.dateToPatten(new Date()));
            gameRecordGoldenFService.save(gameRecordById);
        }
    }
    private void beginPG2(){
        GameRecordGoldenF gameRecordById = gameRecordGoldenFService.findGameRecordById(6927L);
        Date date = new Date();
        String format = DateUtil.getSimpleDateFormat().format(date);
        for (int i=0;i<=num;i++){
            gameRecordById.setId(null);
            gameRecordById.setUserId(60834L);
            gameRecordById.setCreateAtStr(format);
            gameRecordById.setTraceId(UserPasswordUtil.getRandomPwd()+UserPasswordUtil.getRandomPwd());
            gameRecordById.setCreateAtStr(DateUtil.dateToPatten(new Date()));
            gameRecordGoldenFService.save(gameRecordById);
        }
    }
}
