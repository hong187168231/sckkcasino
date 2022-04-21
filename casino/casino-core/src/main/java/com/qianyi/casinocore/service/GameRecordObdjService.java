package com.qianyi.casinocore.service;

import com.qianyi.casinocore.repository.GameRecordObdjRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
