package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.RptBetInfoDetail;
import com.qianyi.casinocore.repository.RptBetInfoDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RptBetInfoDetailService {

    @Autowired
    private RptBetInfoDetailRepository repository;


    public void save(RptBetInfoDetail rptBetInfoDetail) {
        repository.save(rptBetInfoDetail);
    }
}
