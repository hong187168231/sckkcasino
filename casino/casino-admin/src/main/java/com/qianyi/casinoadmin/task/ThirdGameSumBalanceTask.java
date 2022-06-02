package com.qianyi.casinoadmin.task;

import com.qianyi.casinoadmin.service.ThridUserBalanceSumService;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.service.UserThirdService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.TaskConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
     * year要求10分钟跑一次
     */
    @Scheduled(cron = TaskConst.BACK_GAME_ORDER)
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
        thridUserBalanceSumService.setRedisWMMoneyTotal(wmThird);
        //PG/CQ9总余额
        thridUserBalanceSumService.setRedisPGMoneyTotal(pgCq9Third);
        //查询OB电竞总余额
        thridUserBalanceSumService.setRedisOBDJMoneyTotal(obdjThird);
        //查询OB体育总余额
        thridUserBalanceSumService.setRedisOBTYMoneyTotal(obtyThird);
        //查询沙巴体育总余额
        thridUserBalanceSumService.setRedisSABAMoneyTotal(sabaThird);

    }


}
