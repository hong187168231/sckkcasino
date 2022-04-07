package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ChargeOrder;
import com.qianyi.casinocore.model.ErrorOrder;
import com.qianyi.casinocore.repository.ChargeOrderRepository;
import com.qianyi.casinocore.repository.ErrorOrderRepository;
import com.qianyi.modulecommon.util.CommonUtil;
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
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ErrorOrderService {


    @Autowired
    private ErrorOrderRepository errorOrderRepository;

    public Page<ErrorOrder> findErrorOrderPage(ErrorOrder order,Pageable pageable, Date startDate, Date endDate){
        Specification<ErrorOrder> condition = getCondition(order,startDate,endDate);
        return errorOrderRepository.findAll(condition,pageable);
    }

    public ErrorOrder findErrorOrderByIdUseLock(Long id){
        ErrorOrder errorOrder = errorOrderRepository.findErrorOrderByIdUseLock(id);
        return errorOrder;
    }

    @Transactional
    public void updateErrorOrdersRemark(Integer status,Long id){
        errorOrderRepository.updateErrorOrdersRemark(status,id);
    }
    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
     */
    private Specification<ErrorOrder> getCondition(ErrorOrder order,Date startDate,Date endDate) {
        Specification<ErrorOrder> specification = new Specification<ErrorOrder>() {
            @Override
            public Predicate toPredicate(Root<ErrorOrder> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (order.getStatus() !=null) {
                    list.add(cb.equal(root.get("status").as(Integer.class), order.getStatus()));
                }
                if(order.getType() != null){
                    list.add(cb.equal(root.get("type").as(Integer.class), order.getType()));
                }
                if (!CommonUtil.checkNull(order.getOrderNo())) {
                    list.add(cb.equal(root.get("orderNo").as(String.class), order.getOrderNo()));
                }
                if (startDate != null) {
                    list.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), startDate));
                }
                if (endDate != null) {
                    list.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class),endDate));
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));

                return predicate;
            }
        };
        return specification;
    }

}
