package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ConsumerError;
import com.qianyi.casinocore.repository.ConsumerErrorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConsumerErrorService {

    @Autowired
    private ConsumerErrorRepository consumerErrorRepository;

    public ConsumerError save(ConsumerError consumerError){
        return consumerErrorRepository.save(consumerError);
    }
}
