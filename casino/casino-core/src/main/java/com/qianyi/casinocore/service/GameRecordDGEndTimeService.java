package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordDGEndTime;
import com.qianyi.casinocore.repository.GameRecordDGEndTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameRecordDGEndTimeService {

    @Autowired
    private GameRecordDGEndTimeRepository gameRecordDGEndTimeRepository;

    public GameRecordDGEndTime findFirstByPlatformAndStatusOrderByEndTimeDesc(String platform, Integer status) {
        return gameRecordDGEndTimeRepository.findFirstByPlatformAndStatusOrderByEndTimeDesc(platform, status);
    }

    public GameRecordDGEndTime save(GameRecordDGEndTime gameRecordDGEndTime) {
        return gameRecordDGEndTimeRepository.save(gameRecordDGEndTime);
    }
}
