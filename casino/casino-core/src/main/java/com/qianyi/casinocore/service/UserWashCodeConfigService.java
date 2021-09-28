package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.UserWashCodeConfig;
import com.qianyi.casinocore.model.WashCodeConfig;
import com.qianyi.casinocore.repository.UserWashCodeConfigRepository;
import com.qianyi.casinocore.repository.WashCodeConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserWashCodeConfigService {

    @Autowired
    private UserWashCodeConfigRepository userWashCodeConfigRepository;

    public List<UserWashCodeConfig> saveAll(List<UserWashCodeConfig> list){
        return userWashCodeConfigRepository.saveAll(list);
    }

    public List<UserWashCodeConfig> findByUserIdAndPlatform(Long userId, String platform) {
        return userWashCodeConfigRepository.findByUserIdAndPlatform(userId,platform);
    }

    public List<UserWashCodeConfig> findByUserId(Long userId) {
        return userWashCodeConfigRepository.findByUserId(userId);
    }
}
