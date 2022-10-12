package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameDetailVncEndTime;
import com.qianyi.casinocore.model.GameRecordVNCEndTime;
import com.qianyi.casinocore.repository.GameDetailVncEndTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameDetailVncEndTimeService {

    @Autowired
    private GameDetailVncEndTimeRepository gameDetailVncEndTimeRepository;


    public GameDetailVncEndTime findFirstByPlatformAndStatusOrderByEndTimeDesc(String vendorCode, Integer status) {
        return gameDetailVncEndTimeRepository.findFirstByPlatformAndStatusOrderByEndTimeDesc(vendorCode, status);
    }

    public GameDetailVncEndTime save(GameDetailVncEndTime gameDetailVncEndTime) {
        return gameDetailVncEndTimeRepository.save(gameDetailVncEndTime);
    }
}
