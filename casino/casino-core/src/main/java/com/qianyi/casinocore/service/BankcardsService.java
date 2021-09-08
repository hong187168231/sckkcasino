package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.repository.BankcardsRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class BankcardsService {

    @Autowired
    BankcardsRepository bankcardsRepository;

    public List<Bankcards> findBankcardsByUserId(Long userId){
        return bankcardsRepository.findBankcardsById(userId);
    }

    public Bankcards boundCard(Bankcards bankcards){
        return bankcardsRepository.save(bankcards);
    }

    public Bankcards findBankCardsInByUserId(Long userId) {
        return bankcardsRepository.findBankcardsInUserCardByUserId(userId);
    }


    /**
     * 查询用户银行卡
     *
     * @param bankcards
     * @return
     */
    public List<Bankcards> findUserBank(Bankcards bankcards) {
        Specification<Bankcards> condition = this.getCondition(bankcards);
        List<Bankcards> bankcardsList = bankcardsRepository.findAll(condition);
        return bankcardsList;
    }

    private Specification<Bankcards> getCondition(Bankcards bankcards) {
        Specification<Bankcards> specification = new Specification<Bankcards>(){
            @Override
            public Predicate toPredicate(Root<Bankcards> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if(bankcards.getUserId() != null){
                    list.add(cb.equal(root.get("userId").as(Long.class), bankcards.getUserId()));
                }
                if(bankcards.getBankId() != null){
                    list.add(cb.equal(root.get("bankId").as(Long.class), bankcards.getBankId()));
                }
                if(StringUtils.isNotBlank(bankcards.getRealName())){
                    list.add(cb.equal(root.get("realName").as(Long.class), bankcards.getRealName()));
                }
                if(StringUtils.isNotBlank(bankcards.getBankAccount())){
                    list.add(cb.equal(root.get("bankAccount").as(Long.class), bankcards.getBankAccount()));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
}
