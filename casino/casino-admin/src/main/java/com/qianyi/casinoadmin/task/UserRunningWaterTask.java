package com.qianyi.casinoadmin.task;

import cn.hutool.core.collection.CollUtil;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.ShareProfitChange;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserRunningWater;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.TaskConst;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UserRunningWaterTask {

    @Autowired
    private UserRunningWaterService userRunningWaterService;

    @Scheduled(cron = TaskConst.USER_RUNNING_WATER)
    public void create() {
        Calendar nowTime = Calendar.getInstance();
        nowTime.add(Calendar.DATE, -1);
        String format = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        List<UserRunningWater> byStaticsTimes = userRunningWaterService.findByStaticsTimes(format);
        if (CollUtil.isNotEmpty(byStaticsTimes))
            return;

        userRunningWaterService.statistics(format);
    }
}
