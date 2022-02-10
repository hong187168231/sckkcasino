package com.qianyi.casinocore.service;

import com.qianyi.casinocore.repository.GameRecordGoldenFRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameRecordGoldenFService {

    @Autowired
    private GameRecordGoldenFRepository gameRecordGoldenFRepository;

}
