package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordDMCEndTime;
import com.qianyi.casinocore.repository.GameRecordDMCEndTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameRecordDMCEndTimeService {

    @Autowired
    private GameRecordDMCEndTimeRepository gameRecordDMCEndTimeRepository;

    public GameRecordDMCEndTime findFirstByPlatformAndStatusOrderByEndTimeDesc(String platform, Integer status) {
        return gameRecordDMCEndTimeRepository.findFirstByPlatformAndStatusOrderByEndTimeDesc(platform, status);
    }

    public GameRecordDMCEndTime save(GameRecordDMCEndTime gameRecordDMCEndTime) {
        return gameRecordDMCEndTimeRepository.save(gameRecordDMCEndTime);
    }
}
