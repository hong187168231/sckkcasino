package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.repository.GameRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class GameRecordService {

    @Autowired
    GameRecordRepository gameRecordRepository;

    public GameRecord findFirstByOrderByEndTimeDesc(){
        return gameRecordRepository.findFirstByOrderByEndTimeDesc();
    }
    public void saveAll(List<GameRecord> list){
        gameRecordRepository.saveAll(list);
    }

}
