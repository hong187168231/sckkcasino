package com.qianyi.casinocore.service;

import com.qianyi.casinocore.repository.BetRatioConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BetRatioConfigService {

    @Autowired
    private BetRatioConfigRepository betRatioConfigRepository;
}
