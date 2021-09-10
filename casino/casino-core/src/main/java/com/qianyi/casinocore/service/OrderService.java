package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.Order;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.OrderRepository;
import com.qianyi.modulecommon.util.CommonUtil;

import com.qianyi.modulecommon.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

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

    public Page<Order> findOrderPage(Pageable pageable, Order order) {
        Specification<Order> condition = this.getCondition(order);
        return orderRepository.findAll(condition, pageable);
    }

    public Order findByNo(String no) {
        return orderRepository.getByNo(no);
    }

    private Specification<Order> getCondition(Order order) {

        Specification<Order> specification = new Specification<Order>() {
            @Override
            public Predicate toPredicate(Root<Order> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(order.getNo())) {
                    list.add(cb.equal(root.get("no").as(String.class), order.getNo()));
                }
                if(order.getUserId() != null){
                    list.add(cb.equal(root.get("userId").as(Long.class), order.getUserId()));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

}
