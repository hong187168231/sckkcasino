package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordVNC;
import com.qianyi.casinocore.repository.GameRecordVNCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GameRecordVNCService {

    @Autowired
    private GameRecordVNCRepository gameRecordVNCRepository;

    public GameRecordVNC findByMerchantCodeAndBetOrder(String merchantCode, String betOrder) {

        return gameRecordVNCRepository.findByMerchantCodeAndBetOrder(merchantCode, betOrder);
    }

    public GameRecordVNC save(GameRecordVNC gameRecord) {
        return gameRecordVNCRepository.save(gameRecord);
    }

    public void updateCodeNumStatus(Long id, Integer codeNumStatus) {
        gameRecordVNCRepository.updateCodeNumStatus(id, codeNumStatus);
    }

    public void updateWashCodeStatus(Long id, Integer washCodeStatus) {
        gameRecordVNCRepository.updateWashCodeStatus(id, washCodeStatus);
    }

    public void updateExtractStatus(Long id, Integer extractStatus) {
        gameRecordVNCRepository.updateExtractStatus(id,extractStatus);
    }

    public void updateRebateStatus(Long id, Integer rebateStatus) {
        gameRecordVNCRepository.updateRebateStatus(id, rebateStatus);
    }

    public Map<String,Object> findSumByPlatformAndTime(String platform, String startTime, String endTime) {
        return gameRecordVNCRepository.findSumByPlatformAndTime(platform,startTime,endTime);
    }
}
