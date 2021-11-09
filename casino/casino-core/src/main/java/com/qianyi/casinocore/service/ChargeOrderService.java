package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ChargeOrder;
import com.qianyi.casinocore.repository.ChargeOrderRepository;
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
import java.util.Optional;

@Service
public class ChargeOrderService {

    @Autowired
    private ChargeOrderRepository chargeOrderRepository;


    public ChargeOrder saveOrder(ChargeOrder entity){
        return chargeOrderRepository.save(entity);
    }

    public Page<ChargeOrder> findChargeOrderPage(ChargeOrder chargeOrder, Pageable pageable){
        Specification<ChargeOrder> condition = getCondition(chargeOrder,null,null);
        return chargeOrderRepository.findAll(condition,pageable);
    }

    public List<ChargeOrder> findChargeOrders(ChargeOrder chargeOrder,Date startDate,Date endDate){
        Specification<ChargeOrder> condition = getCondition(chargeOrder,startDate,endDate);
        return chargeOrderRepository.findAll(condition);
    }

    public Page<ChargeOrder> findChargeOrderPage(Specification<ChargeOrder> condition, Pageable pageable){
        return chargeOrderRepository.findAll(condition,pageable);
    }
    public ChargeOrder findChargeOrderByIdUseLock(Long id){
        ChargeOrder chargeOrder = chargeOrderRepository.findChargeOrderByIdUseLock(id);
        return chargeOrder;
    }

    public ChargeOrder findById(Long id){
        Optional<ChargeOrder> optional = chargeOrderRepository.findById(id);
        if (optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    @Transactional
    public void updateChargeOrders(Integer status,String time){
        chargeOrderRepository.updateChargeOrders(status,time);
    }

    public Integer countByUserIdAndStatus(Long userId,int status) {
        return chargeOrderRepository.countByUserIdAndStatus(userId,status);
    }

    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
     */
    private Specification<ChargeOrder> getCondition(ChargeOrder chargeOrder,Date startDate,Date endDate) {
        Specification<ChargeOrder> specification = new Specification<ChargeOrder>() {
            @Override
            public Predicate toPredicate(Root<ChargeOrder> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (chargeOrder.getStatus() !=null) {
                    list.add(cb.equal(root.get("status").as(Integer.class), chargeOrder.getStatus()));
                }
                if(chargeOrder.getType() != null){
                    list.add(cb.equal(root.get("type").as(Integer.class), chargeOrder.getType()));
                }
                if (!CommonUtil.checkNull(chargeOrder.getOrderNo())) {
                    list.add(cb.equal(root.get("orderNo").as(String.class), chargeOrder.getOrderNo()));
                }
                if (chargeOrder.getUserId() != null ) {
                    list.add(cb.equal(root.get("userId").as(Long.class), chargeOrder.getUserId()));
                }
                if (chargeOrder.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), chargeOrder.getFirstProxy()));
                }
                if (chargeOrder.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), chargeOrder.getSecondProxy()));
                }
                if (chargeOrder.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), chargeOrder.getThirdProxy()));
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
