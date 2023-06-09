package com.qianyi.casinoadmin.task;

import cn.hutool.core.util.ObjectUtil;
import com.qianyi.casinocore.business.UserLevelBusiness;
import com.qianyi.casinocore.model.UserLevelDecline;
import com.qianyi.casinocore.repository.UserLevelDeclineRepository;
import com.qianyi.casinocore.service.UserLevelService;
import com.qianyi.modulecommon.util.DateUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Transactional
public class VipDeclineTask {

    @Autowired
    private UserLevelBusiness userLevelBusiness;
    @Autowired
    private UserLevelDeclineRepository userLevelDeclineRepository;
    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private RedisUtil redisUtil;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void begin() {
        log.info(" Vip 升级开始 start=============================================》");
        try {
            Date startTime = DateUtil.getStartTime();
            Date endTime = DateUtil.getEndTime();
            startTime = cn.hutool.core.date.DateUtil.offsetDay(startTime, -10);
            endTime = cn.hutool.core.date.DateUtil.offsetDay(endTime, -10);
            List<Map<String, Object>> idList = userLevelService.findLastRiseUser(startTime, endTime);
            for (Map<String, Object> map : idList) {
                Long userId = Long.parseLong(map.get("userId").toString());
                userLevelBusiness.processUserKeepLevel(userId);
                int row = queryToday(userId);
                if (row < 1) {
                    UserLevelDecline userLevelDecline = new UserLevelDecline();
                    userLevelDecline.setTodayDeclineStatus(1);
                    userLevelDecline.setUserId(userId);
                    userLevelDeclineRepository.save(userLevelDecline);
                }
            }
            log.info(" Vip 升级开始 结束end=============================================》");
        } catch (Exception ex) {
            log.error(" Vip 升级开始 失败=============================================》", ex);
        }
    }

    private int queryToday(Long userId) {
        String startTime = DateUtil.getStartTime(0);
        String endTime = DateUtil.getEndTime(0);
        return userLevelDeclineRepository.queryBonusAmount(userId, startTime, endTime);
    }

    public static void main(String[] args) {
        Date startTime = DateUtil.getStartTime();
        Date endTime = DateUtil.getEndTime();
        startTime = cn.hutool.core.date.DateUtil.offsetDay(startTime, -15);
        endTime = cn.hutool.core.date.DateUtil.offsetDay(endTime, -15);
        System.out.println();
    }

    @Scheduled(fixedDelay = 1000 * 10 * 3  )
    public void beginOne() {
        Integer vipTask = (Integer)redisUtil.get("vipTast");
        if(ObjectUtil.isNotNull(vipTask) && vipTask.equals(1)){
            log.info(" Vip 升级开始 start=============================================》");
            try {
                Date startTime = DateUtil.getStartTime();
                Date endTime = DateUtil.getEndTime();
                startTime = cn.hutool.core.date.DateUtil.offsetDay(startTime, -110);
                endTime = cn.hutool.core.date.DateUtil.offsetDay(endTime, -11);
                List<Map<String, Object>> idList = userLevelService.findLastRiseUserNotLimit(startTime, endTime);
                for (Map<String, Object> map : idList) {
                    Long userId = Long.parseLong(map.get("userId").toString());
                    userLevelBusiness.processUserKeepLevel2(userId);
                    int row = queryToday(userId);
                    if (row < 1) {
                        UserLevelDecline userLevelDecline = new UserLevelDecline();
                        userLevelDecline.setTodayDeclineStatus(1);
                        userLevelDecline.setUserId(userId);
                        userLevelDeclineRepository.save(userLevelDecline);
                    }
                }
                log.info(" Vip 升级开始 结束end=============================================》");
            } catch (Exception ex) {
                log.error(" Vip 升级开始 失败=============================================》", ex);
            }finally {
                redisUtil.delete("vipTast");
            }
        }
    }

}