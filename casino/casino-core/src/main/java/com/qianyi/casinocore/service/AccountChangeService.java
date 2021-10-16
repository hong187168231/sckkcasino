package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.AccountChange;
import com.qianyi.casinocore.repository.AccountChangeRepository;
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

@Service
public class AccountChangeService {
    @Autowired
    private AccountChangeRepository accountChangeRepository;

    public AccountChange save(AccountChange po){
        return accountChangeRepository.save(po);
    }

    public Page<AccountChange> findAccountChangePage(Pageable pageable,AccountChange accountChange){
        Specification<AccountChange> condition = this.getCondition(accountChange);
        Page<AccountChange> all = accountChangeRepository.findAll(condition, pageable);
        return all;
    }
    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
     */
    private Specification<AccountChange> getCondition(AccountChange AccountChange) {
        Specification<AccountChange> specification = new Specification<AccountChange>() {
            @Override
            public Predicate toPredicate(Root<AccountChange> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(AccountChange.getOrderNo())) {
                    list.add(cb.equal(root.get("orderNo").as(String.class), AccountChange.getOrderNo()));
                }
                if (AccountChange.getType() !=null) {
                    list.add(cb.equal(root.get("type").as(Integer.class), AccountChange.getType()));
                }
                if (AccountChange.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), AccountChange.getUserId()));
                }
                if (AccountChange.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), AccountChange.getFirstProxy()));
                }
                if (AccountChange.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), AccountChange.getSecondProxy()));
                }
                if (AccountChange.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), AccountChange.getThirdProxy()));
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }

    public Page<AccountChange> findAccountChange(Specification<AccountChange> condition, Pageable pageable) {
        Page<AccountChange> all = accountChangeRepository.findAll(condition, pageable);
        return all;
    }
}
