package com.qianyi.casinoadmin.task;

import com.qianyi.casinoadmin.util.TaskConst;
import com.qianyi.casinocore.business.ChargeBusiness;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Calendar;

@Slf4j
@Component
public class BackChargeOrderTask {
    @Autowired
    private ChargeBusiness chargeBusiness;
    @Scheduled(cron = TaskConst.BACK_ORDER)
    public void updateOrderChargeOrder(){
        log.info("修改超时充值订单开始执行start=============================================》");
        Calendar nowTime = Calendar.getInstance();
        nowTime.add(Calendar.MINUTE, -30);
        String format = DateUtil.getSimpleDateFormat().format(nowTime.getTime());
        try {
            chargeBusiness.updateChargeOrderStatus(0,format);
        }catch (Exception e){
            log.info("修改超时充值订单出错=============================================》");
        }
    }
}
