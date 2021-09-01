package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.repository.UserThirdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class UserThirdService {
    @Autowired
    UserThirdRepository userThirdRepository;

    public UserThird findByUserId(Long userId) {
        return userThirdRepository.findByUserId(userId);
    }

    public UserThird save(UserThird third) {
        return userThirdRepository.save(third);
    }

    public UserThird findByAccount(String account) {
        return userThirdRepository.findByAccount(account);
    }
}
