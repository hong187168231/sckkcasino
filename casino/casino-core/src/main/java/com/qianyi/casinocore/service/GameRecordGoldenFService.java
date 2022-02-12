package com.qianyi.casinocore.service;

import com.qianyi.casinocore.repository.GameRecordGoldenFRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameRecordGoldenFService {

    @Autowired
    private GameRecordGoldenFRepository gameRecordGoldenFRepository;

    public void updateCodeNumStatus(Long id,Integer codeNumStatus){
        gameRecordGoldenFRepository.updateCodeNumStatus(id,codeNumStatus);
    }

    public void updateWashCodeStatus(Long id,Integer washCodeStatus){
        gameRecordGoldenFRepository.updateWashCodeStatus(id,washCodeStatus);
    }

    public void updateProfitStatus(Long id,Integer shareProfitStatus){
        gameRecordGoldenFRepository.updateProfitStatus(id,shareProfitStatus);
    }

}
