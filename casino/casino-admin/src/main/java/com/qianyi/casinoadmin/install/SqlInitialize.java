package com.qianyi.casinoadmin.install;

import cn.hutool.core.collection.CollUtil;
import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.model.GameRecordEndIndex;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
@Order(13)
public class SqlInitialize implements CommandLineRunner {
    @Autowired
    private GameRecordEndIndexService gameRecordEndIndexService;

    @Autowired
    private GameRecordReportNewService gameRecordReportNewService;

    @Autowired
    private WithdrawOrderService withdrawOrderService;

    @Autowired
    private ChargeOrderService chargeOrderService;

    @Autowired
    private RptBetInfoDetailService rptBetInfoDetailService;

    @Autowired
    private GameRecordDGService gameRecordDGService;

    @Autowired
    private BankcardsService bankcardsService;

    public final static String start = " 12:00:00";

    public final static String end = " 11:59:59";

    public final static String staticsTimesEnd = " 12";

    @Override
    public void run(String... args) throws Exception {
        // GameRecordEndIndex first = gameRecordEndIndexService.findUGameRecordEndIndexUseLock();
        // first.setSABASPORTMaxId(0L);
        // gameRecordEndIndexService.save(first);
        // gameRecordReportNewService.deleteByPlatform(Constants.PLATFORM_SABASPORT);
        // gameRecordReportNewService.saveGameRecordReportSABASPORT();
        withdrawOrderService.updateWithdrawOrderWithdrawTime();

        chargeOrderService.updateChargeOrderSucceedTime();

        withdrawOrderService.updateWithdrawWithdrawTime();

        rptBetInfoDetailService.updateRptBetInfoDetailGamePlay();

        gameRecordDGService.updateGameRecordDGAvailableBet();

        List<Bankcards> byRealNameLowercase = bankcardsService.findByRealNameLowercase();
        if (CollUtil.isNotEmpty(byRealNameLowercase)) {
            byRealNameLowercase.forEach(bankcards -> {
                bankcards.setRealNameLowercase(CommonUtil.formatting(bankcards.getRealName()));
                bankcardsService.boundCard(bankcards);
            });
        }

         GameRecordEndIndex first = gameRecordEndIndexService.findUGameRecordEndIndexUseLock();
         first.setDGMaxId(0L);
         gameRecordEndIndexService.save(first);
         gameRecordReportNewService.deleteDataByPlatform(Constants.PLATFORM_DG);
         gameRecordReportNewService.saveGameRecordReportDG();

        // // 计算最近十天注单
//        Calendar nowTime = Calendar.getInstance();
//        nowTime.add(Calendar.DATE, -100);
//        Date startDate = nowTime.getTime();
//        String startDay = DateUtil.getSimpleDateFormat(DateUtil.patten1).format(startDate);
//        String yesterday = DateUtil.getSimpleDateFormat(DateUtil.patten1).format(new Date());
//
//        List<String> betweenDate = DateUtil.getBetweenDate(startDay, yesterday);
//        for (String str : betweenDate) {
//            Date date = DateUtil.getDate(str);
//            Calendar cal = Calendar.getInstance();
//            cal.setTime(date);
//            cal.add(Calendar.DATE, 1);
//            String tomorrow = DateUtil.getSimpleDateFormat1().format(cal.getTime());
//            // gameRecordReportNewService.statisticsWashCode(Constants.PLATFORM_OBZR, Constants.PLATFORM_OBZR, str +
//            // staticsTimesEnd,
//            // str + start, tomorrow+end);
//            gameRecordReportNewService.statisticsAward(str + staticsTimesEnd, str + start, tomorrow + end);
//        }
    }
}
