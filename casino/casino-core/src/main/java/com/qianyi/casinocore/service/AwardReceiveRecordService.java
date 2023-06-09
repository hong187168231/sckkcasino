package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.AwardReceiveRecord;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.AwardReceiveRecordRepository;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
@CacheConfig(cacheNames = {"awardReceiveRecord"})
@Slf4j
public class AwardReceiveRecordService {

    @Autowired
    AwardReceiveRecordRepository awardReceiveRecordRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public AwardReceiveRecord save(AwardReceiveRecord awardReceiveRecord, User user) {
        if (Objects.isNull(user.getThirdProxy()) || user.getThirdProxy()==0L){
            awardReceiveRecord.setFirstProxy(0L);
            awardReceiveRecord.setSecondProxy(0L);
            awardReceiveRecord.setThirdProxy(0L);
        }else {
            awardReceiveRecord.setFirstProxy(user.getFirstProxy());
            awardReceiveRecord.setSecondProxy(user.getSecondProxy());
            awardReceiveRecord.setThirdProxy(user.getThirdProxy());
        }
        if (Objects.isNull(awardReceiveRecord.getReceiveTime())){
            awardReceiveRecord.setReceiveTime(new Date());
        }
        return awardReceiveRecordRepository.save(awardReceiveRecord);
    }

    public int countTodayAward(Long userId) {
        String startTime = DateUtil.getStartTime(0);
        String endTime = DateUtil.getEndTime(0);
        return awardReceiveRecordRepository.countAwardReceiveByTime(userId, startTime, endTime);
    }

    public AwardReceiveRecord selectAwardReceiveByTime(Long userId) {
        String startTime = DateUtil.getStartTime(0);
        String endTime = DateUtil.getEndTime(0);
        return awardReceiveRecordRepository.selectAwardReceiveByTime(userId, startTime, endTime);
    }

    public BigDecimal findBonusAmount(String startTime, String endTime) {
        return awardReceiveRecordRepository.totalAwardByTime(startTime, endTime);
    }

    public void modifyIsReceive(Long userId) {
        awardReceiveRecordRepository.modifyIsReceive(userId);
    }

    public BigDecimal queryBonusAmount(String startTime, String endTime) {
        BigDecimal amount1 =  awardReceiveRecordRepository.queryBonusAmount(startTime, endTime);
        BigDecimal amount2 =  awardReceiveRecordRepository.queryBonusAmount2(startTime, endTime);
        return amount1.add(amount2);
    }

    public int countRiseAwardNum2(Long userId, Integer level) {
        return awardReceiveRecordRepository.countRiseAwardNum(userId, level);
    }


    public AwardReceiveRecord selectNotReceiveRiseAward(Long userId, Integer level) {
        return awardReceiveRecordRepository.selectNotReceiveRiseAward(userId, level);
    }


    public int countNotReceiveRiseAwardNum(Long userId, Integer level) {
        return awardReceiveRecordRepository.countNotReceiveRiseAwardNum(userId, level);
    }

    public int countNotReceiveRiseAwardAll(Long userId) {
        return awardReceiveRecordRepository.countNotReceiveRiseAwardAll(userId);
    }

    public Set<Long> findUserIds(){
        return awardReceiveRecordRepository.findUserIds();
    }

    public void updateProxyAffiliation(Long userId,Long firstProxy,Long secondProxy,Long thirdProxy){
        awardReceiveRecordRepository.updateProxyAffiliation(userId,firstProxy,secondProxy,thirdProxy);
    }

    public void updateReceiveTime(){
        awardReceiveRecordRepository.updateReceiveTime();
    }

    public List<Map<String, Object>> getMapSumTodayAward(String startTime, String endTime){
        return awardReceiveRecordRepository.getMapSumTodayAward(startTime,endTime);
    }

    public List<Map<String, Object>> getMapSumRiseAward(String startTime, String endTime){
        return awardReceiveRecordRepository.getMapSumRiseAward(startTime,endTime);
    }
}
