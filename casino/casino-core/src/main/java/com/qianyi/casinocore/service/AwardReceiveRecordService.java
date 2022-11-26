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

    public List<AwardReceiveRecord> countUpgradeAward(Long userId) {
        return awardReceiveRecordRepository.countUpgradeAward(userId);
    }

    public AwardReceiveRecord queryUpgradeAward(Long userId) {
        return awardReceiveRecordRepository.findByAwardTypeAndReceiveStatusAndUserId(2, 0, userId);
    }

    public AwardReceiveRecord queryMaxUpgradeAward(Long userId) {
        return awardReceiveRecordRepository.queryMaxUpgradeAward(userId);
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

    public int countRiseAwardNum(Long userId, Integer level) {
        return awardReceiveRecordRepository.countRiseAwardNum(userId, level);
    }


}
