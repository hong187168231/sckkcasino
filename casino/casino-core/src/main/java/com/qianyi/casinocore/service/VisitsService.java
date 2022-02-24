package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.Visits;
import com.qianyi.casinocore.repository.VisitsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VisitsService {

    @Autowired
    private VisitsRepository visitsRepository;

    public void save(Visits visits) {
        visitsRepository.save(visits);
    }
}
