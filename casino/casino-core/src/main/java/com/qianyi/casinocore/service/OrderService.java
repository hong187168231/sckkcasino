package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.Order;
import com.qianyi.casinocore.repository.OrderRepository;
import com.qianyi.modulecommon.util.CommonUtil;

import com.qianyi.modulecommon.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class OrderService {
    @Autowired
    OrderRepository orderRepository;

    //生成订单 号
    public String getOrderNo() {
        String orderNo = "QY";
        String today = DateUtil.today("yyyyMMddHHmmssSSS");
        String randNum = CommonUtil.random(3);

        orderNo = orderNo + today + randNum;
        return orderNo;
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }
}
