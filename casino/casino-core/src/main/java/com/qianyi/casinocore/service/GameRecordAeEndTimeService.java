package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordAeEndTime;
import com.qianyi.casinocore.model.GameRecordObEndTime;
import com.qianyi.casinocore.repository.GameRecordAeEndTimeRepository;
import com.qianyi.casinocore.repository.GameRecordObEndTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameRecordAeEndTimeService {

    @Autowired
    private GameRecordAeEndTimeRepository gameRecordAeEndTimeRepository;

    public GameRecordAeEndTime findFirstByPlatformAndStatusOrderByEndTimeDesc(String vendorCode, Integer status) {
        return gameRecordAeEndTimeRepository.findFirstByPlatformAndStatusOrderByEndTimeDesc(vendorCode, status);
    }

    public GameRecordAeEndTime save(GameRecordAeEndTime gameRecordAeEndTime) {
        return gameRecordAeEndTimeRepository.save(gameRecordAeEndTime);
    }
}
