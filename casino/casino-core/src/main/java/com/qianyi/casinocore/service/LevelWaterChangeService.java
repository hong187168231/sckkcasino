package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.LevelWaterChange;
import com.qianyi.casinocore.repository.LevelWaterChangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.math.BigDecimal;

@Service
public class LevelWaterChangeService {
    @Autowired
    private LevelWaterChangeRepository levelWaterChangeRepository;

    @Autowired
    private EntityManager entityManager;


    public LevelWaterChange save(LevelWaterChange levelWaterChange) {
        return levelWaterChangeRepository.save(levelWaterChange);
    }


    public LevelWaterChange findByGameRecordId(Long gameRecordId) {
        return levelWaterChangeRepository.findByGameRecordId(gameRecordId);
    }

    public LevelWaterChange findByUserId(Long userId) {
        return levelWaterChangeRepository.findByUserId(userId);
    }

    public BigDecimal findTotalBetWater(Long userId){
        return levelWaterChangeRepository.findTotalBetWater(userId);
    }
}