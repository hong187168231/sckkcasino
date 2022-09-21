package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordVNCEndTime;
import com.qianyi.casinocore.repository.GameRecordVNCEndTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameRecordVNCEndTimeService {

    @Autowired
    private GameRecordVNCEndTimeRepository gameRecordVNCEndTimeRepository;

    public GameRecordVNCEndTime findFirstByPlatformAndStatusOrderByEndTimeDesc(String vendorCode, Integer status) {
        return gameRecordVNCEndTimeRepository.findFirstByPlatformAndStatusOrderByEndTimeDesc(vendorCode, status);
    }

    public GameRecordVNCEndTime save(GameRecordVNCEndTime gameRecordVNCEndTime) {
        return gameRecordVNCEndTimeRepository.save(gameRecordVNCEndTime);
    }

}
