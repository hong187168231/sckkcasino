package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.Order;
import com.qianyi.casinocore.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Slf4j
@Service
public class OrderBusiness {

    @Autowired
    private OrderService orderService;

    /**
     * 成功订单确认
     * @param order
     */
    @Transactional
    public void checkOrderSuccess(Order order) {

    }
}
