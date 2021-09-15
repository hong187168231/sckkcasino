package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ChargeOrder;
import com.qianyi.casinocore.repository.ChargeOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChargeOrderService {

    @Autowired
    private ChargeOrderRepository chargeOrderRepository;


    public ChargeOrder saveOrder(ChargeOrder entity){
        return chargeOrderRepository.save(entity);
    }

    public Page<ChargeOrder> findChargeOrderPage(Specification<ChargeOrder> condition, Pageable pageable){
        return chargeOrderRepository.findAll(condition,pageable);
    }

    public ChargeOrder findChargeOrderByIdUseLock(Long id){
        ChargeOrder chargeOrder = chargeOrderRepository.findChargeOrderByIdUseLock(id);
        return chargeOrder;
    }

    public List<ChargeOrder> findChargeOrdersUseLock(Integer status,String time){
        return chargeOrderRepository.findChargeOrdersUseLock(status,time);
    }
}
