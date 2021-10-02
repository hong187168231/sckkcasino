package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.repository.PlatformConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlatformConfigService {

    @Autowired
    private PlatformConfigRepository platformConfigRepository;

    public List<PlatformConfig> findAll() {
        return platformConfigRepository.findAll();
    }
}
