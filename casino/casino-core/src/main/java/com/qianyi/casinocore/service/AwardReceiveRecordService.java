package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.AwardReceiveRecord;
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
import java.util.List;

@Service
@Transactional
@CacheConfig(cacheNames = {"awardReceiveRecord"})
@Slf4j
public class AwardReceiveRecordService {

    @Autowired
    AwardReceiveRecordRepository awardReceiveRecordRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public AwardReceiveRecord save(AwardReceiveRecord awardReceiveRecord) {
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
        return awardReceiveRecordRepository.queryBonusAmount(startTime, endTime);
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

}
