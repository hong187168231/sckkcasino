package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordObEndTime;
import com.qianyi.casinocore.model.GameRecordObzrTime;
import com.qianyi.casinocore.repository.GameRecordObEndTimeRepository;
import com.qianyi.casinocore.repository.GameRecordObzrTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameRecordObzrTimeService {

    @Autowired
    private GameRecordObzrTimeRepository gameRecordObzrTimeRepository;

    public String findLastEndTime() {
        return gameRecordObzrTimeRepository.findLastEndTime();
    }

    public GameRecordObzrTime save(String endTime) {
        GameRecordObzrTime gameRecordObzrTime = new GameRecordObzrTime();
        gameRecordObzrTime.setEndTime(endTime);
        return gameRecordObzrTimeRepository.save(gameRecordObzrTime);
    }
}
