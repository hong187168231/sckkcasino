package com.qianyi.casinoadmin.install;

import com.qianyi.casinocore.model.GameRecordEndIndex;
import com.qianyi.casinocore.service.ChargeOrderService;
import com.qianyi.casinocore.service.GameRecordEndIndexService;
import com.qianyi.casinocore.service.GameRecordReportNewService;
import com.qianyi.casinocore.service.WithdrawOrderService;
import com.qianyi.modulecommon.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Order(4)
public class SqlInitialize  implements CommandLineRunner {
    @Autowired
    private GameRecordEndIndexService gameRecordEndIndexService;

    @Autowired
    private GameRecordReportNewService gameRecordReportNewService;

    @Autowired
    private WithdrawOrderService withdrawOrderService;

    @Autowired
    private ChargeOrderService chargeOrderService;

    @Override
    public void run(String... args) throws Exception {
//        GameRecordEndIndex first = gameRecordEndIndexService.findUGameRecordEndIndexUseLock();
//        first.setSABASPORTMaxId(0L);
//        gameRecordEndIndexService.save(first);
//        gameRecordReportNewService.deleteByPlatform(Constants.PLATFORM_SABASPORT);
//        gameRecordReportNewService.saveGameRecordReportSABASPORT();
        withdrawOrderService.updateWithdrawOrderWithdrawTime();

        chargeOrderService.updateChargeOrderSucceedTime();

        withdrawOrderService.updateWithdrawWithdrawTime();
    }
}
