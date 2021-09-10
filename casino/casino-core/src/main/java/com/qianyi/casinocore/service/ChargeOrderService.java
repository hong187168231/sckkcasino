package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ChargeOrder;
import com.qianyi.casinocore.repository.ChargeOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChargeOrderService {

    @Autowired
    private ChargeOrderRepository chargeOrderRepository;


    public ChargeOrder saveOrder(ChargeOrder entity){
        return chargeOrderRepository.save(entity);
    }
}
