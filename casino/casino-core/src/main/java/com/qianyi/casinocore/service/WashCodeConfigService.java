package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.WashCodeConfig;
import com.qianyi.casinocore.repository.WashCodeConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WashCodeConfigService {

    @Autowired
    private WashCodeConfigRepository washCodeConfigRepository;

    public List<WashCodeConfig> findAll(){
        return washCodeConfigRepository.findAll();
    }

    public List<WashCodeConfig> findByPlatformAndState(String platform,Integer state){
        return washCodeConfigRepository.findByPlatformAndState(platform,state);
    }

    public List<WashCodeConfig> findByPlatform(String platform){
        return washCodeConfigRepository.findByPlatform(platform);
    }

    public List<WashCodeConfig> saveAll(List<WashCodeConfig> list){
        return washCodeConfigRepository.saveAll(list);
    }

    public List<List<WashCodeConfig>> findWashCodeConfigAll(WashCodeConfig washCodeConfig) {
        return null;
    }
}
