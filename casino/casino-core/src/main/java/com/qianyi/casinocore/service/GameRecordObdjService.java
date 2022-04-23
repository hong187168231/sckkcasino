package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordObdj;
import com.qianyi.casinocore.repository.GameRecordObdjRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GameRecordObdjService {

    @Autowired
    private GameRecordObdjRepository gameRecordObdjRepository;

    public void updateCodeNumStatus(Long id, Integer codeNumStatus) {
        gameRecordObdjRepository.updateCodeNumStatus(id, codeNumStatus);
    }

    public void updateWashCodeStatus(Long id, Integer washCodeStatus) {
        gameRecordObdjRepository.updateWashCodeStatus(id, washCodeStatus);
    }

    public void updateRebateStatus(Long id, Integer rebateStatus) {
        gameRecordObdjRepository.updateRebateStatus(id, rebateStatus);
    }

    public void updateGameRecordStatus(Long id, Integer gameRecordStatus) {
        gameRecordObdjRepository.updateGameRecordStatus(id, gameRecordStatus);
    }

    public void updateProfitStatus(Long id, Integer shareProfitStatus) {
        gameRecordObdjRepository.updateProfitStatus(id, shareProfitStatus);
    }

    public void updateExtractStatus(Long id, Integer extractStatus) {
        gameRecordObdjRepository.updateExtractStatus(id,extractStatus);
    }

    public List<Map<String,Object>> queryGameRecords(Long id,Integer num){
        return gameRecordObdjRepository.queryGameRecords(id,num);
    }

    public GameRecordObdj findGameRecordById(Long gameId){return gameRecordObdjRepository.findById(gameId).orElse(null);}

    public GameRecordObdj save(GameRecordObdj gameRecord) {
        return gameRecordObdjRepository.save(gameRecord);
    }

    public GameRecordObdj findByBetId(Long betId){
        return gameRecordObdjRepository.findByBetId(betId);
    }

}
