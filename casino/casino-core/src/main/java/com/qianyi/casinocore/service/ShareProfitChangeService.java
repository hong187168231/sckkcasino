package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ShareProfitChange;
import com.qianyi.casinocore.repository.ShareProfitChangeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ShareProfitChangeService {

    @Autowired
    private ShareProfitChangeRepository shareProfitChangeRepository;

    public ShareProfitChange save(ShareProfitChange shareProfitChange){
        return shareProfitChangeRepository.save(shareProfitChange);
    }

    public List<ShareProfitChange> saveAll(List<ShareProfitChange> shareProfitChangeList){
        return shareProfitChangeRepository.saveAll(shareProfitChangeList);
    }
}
