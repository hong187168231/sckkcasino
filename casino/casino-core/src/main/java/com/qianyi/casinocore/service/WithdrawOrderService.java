package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.WithdrawOrder;
import com.qianyi.casinocore.repository.WithdrawOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WithdrawOrderService {

    @Autowired
    private WithdrawOrderRepository withdrawOrderRepository;

    public WithdrawOrder saveOrder(WithdrawOrder entity){
        withdrawOrderRepository.save(entity);
    }
}
