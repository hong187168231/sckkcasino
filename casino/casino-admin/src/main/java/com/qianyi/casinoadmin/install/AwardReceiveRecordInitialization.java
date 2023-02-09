package com.qianyi.casinoadmin.install;

import cn.hutool.core.collection.CollUtil;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.AwardReceiveRecordService;
import com.qianyi.casinocore.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

@Component
@Slf4j
@Order(12)
public class AwardReceiveRecordInitialization implements CommandLineRunner {

    @Autowired
    private AwardReceiveRecordService awardReceiveRecordService;
    
    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        log.info("初始话修改vip数据开始==============================================》");
        long startTime = System.currentTimeMillis();
        Set<Long> userIds = awardReceiveRecordService.findUserIds();
        if (CollUtil.isNotEmpty(userIds)){
            userIds.forEach(userId->{
                try {
                    User user = userService.findById(userId);
                    Long firstProxy = 0L;
                    Long secondProxy = 0L;
                    Long thirdProxy = 0L;
                    if (Objects.nonNull(user) && Objects.nonNull(user.getThirdProxy())){
                        firstProxy = user.getFirstProxy();
                        secondProxy = user.getSecondProxy();
                        thirdProxy = user.getThirdProxy();
                    }
                    awardReceiveRecordService.updateProxyAffiliation(userId,firstProxy,secondProxy,thirdProxy);
                }catch (Exception e){
                    log.error("更新vip数据失败{}",e.getMessage());
                }
            });
        }

        awardReceiveRecordService.updateReceiveTime();
        log.info("初始话修改vip数据结束耗时{}==============================================>",System.currentTimeMillis()-startTime);
    }
}
