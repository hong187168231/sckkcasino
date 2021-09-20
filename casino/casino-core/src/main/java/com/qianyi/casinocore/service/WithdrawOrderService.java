package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.WithdrawOrder;
import com.qianyi.casinocore.repository.WithdrawOrderRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WithdrawOrderService {

    @Autowired
    private WithdrawOrderRepository withdrawOrderRepository;

    public WithdrawOrder saveOrder(WithdrawOrder entity){
        return withdrawOrderRepository.save(entity);
    }

    public WithdrawOrder findUserByIdUseLock(Long userId){
        return withdrawOrderRepository.findUserByWithdrawIdOrderLock(userId);
    }

    public WithdrawOrder findById(Long id) {
        Optional<WithdrawOrder> optional = withdrawOrderRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public Page<WithdrawOrder> findUserPage(Pageable pageable, WithdrawOrder withdrawOrder) {
        Specification<WithdrawOrder> condition = this.getCondition(withdrawOrder);
        return withdrawOrderRepository.findAll(condition, pageable);
    }

    public List<WithdrawOrder> findOrderList( WithdrawOrder withdrawOrder) {
        Specification<WithdrawOrder> condition = this.getCondition(withdrawOrder);
        return withdrawOrderRepository.findAll(condition);
    }

    private Specification<WithdrawOrder> getCondition(WithdrawOrder withdrawOrder) {
        Specification<WithdrawOrder> specification = new Specification<WithdrawOrder>() {
            List<Predicate> list = new ArrayList<Predicate>();
            @Override
            public Predicate toPredicate(Root<WithdrawOrder> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                if (!CommonUtil.checkNull(withdrawOrder.getNo())) {
                    list.add(cb.equal(root.get("no").as(String.class), withdrawOrder.getNo()));
                }
                if(!CommonUtil.checkNull(withdrawOrder.getBankId())){
                    list.add(cb.equal(root.get("bankId").as(String.class), withdrawOrder.getBankId()));
                }
                if(withdrawOrder.getStatus() != null){
                    list.add(cb.equal(root.get("status").as(Integer.class), withdrawOrder.getStatus()));
                }
                if(withdrawOrder.getUserId() != null){
                    list.add(cb.equal(root.get("userId").as(Long.class), withdrawOrder.getUserId()));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

}
