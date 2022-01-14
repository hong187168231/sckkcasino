package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordReport;
import com.qianyi.casinocore.repository.GameRecordReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameRecordReportService {
    @Autowired
    private GameRecordReportRepository gameRecordReportRepository;

    public GameRecordReport save(GameRecordReport gameRecordReport) {
        return gameRecordReportRepository.save(gameRecordReport);
    }

    public List<GameRecordReport> findByStaticsTimes(String staticsTimes){
        return gameRecordReportRepository.findByStaticsTimes(staticsTimes);
    }

}
