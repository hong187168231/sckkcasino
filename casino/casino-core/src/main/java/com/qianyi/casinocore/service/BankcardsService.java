package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.repository.BankcardsRepository;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BankcardsService {

    @Autowired
    BankcardsRepository bankcardsRepository;

    public List<Bankcards> findBankcardsByUserId(Long userId){
        return bankcardsRepository.findBankcardsByUserIdOrderByDefaultCardDesc(userId);
    }

    public Bankcards boundCard(Bankcards bankcards){
        return bankcardsRepository.save(bankcards);
    }

    public void delBankcards(Bankcards bankcards){
        bankcardsRepository.delete(bankcards);
    }

    public Bankcards findBankCardsInByUserId(Long userId) {
        return bankcardsRepository.findFirstByUserId(userId);
    }

    public int countByUserId(Long userId){
        return bankcardsRepository.countByUserId(userId);
    }

    public List<Map<String,Object>> findForBankcardsByUserId(Long userId){
        return bankcardsRepository.findForBankcards(userId);
    }

    public List<Bankcards> findAll(List<String> ids){
        Specification<Bankcards> condition = this.getCondition(ids);
        return bankcardsRepository.findAll(condition);
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
                if(!CommonUtil.checkNull(bankcards.getBankId())){
                    list.add(cb.equal(root.get("bankId").as(String.class), bankcards.getBankId()));
                }
                if(!CommonUtil.checkNull(bankcards.getRealName())){
                    list.add(cb.equal(root.get("realName").as(String.class), bankcards.getRealName()));
                }
                if(!CommonUtil.checkNull(bankcards.getBankAccount())){
                    list.add(cb.equal(root.get("bankAccount").as(String.class), bankcards.getBankAccount()));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
    private Specification<Bankcards> getCondition(List<String> ids) {
        Specification<Bankcards> specification = new Specification<Bankcards>() {
            @Override
            public Predicate toPredicate(Root<Bankcards> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                Predicate predicate = cb.conjunction();
                if (ids != null && ids.size() > 0) {
                    Path<Object> userId = root.get("id");
                    CriteriaBuilder.In<Object> in = cb.in(userId);
                    for (String id : ids) {
                        try {
                            in.value(Long.valueOf(id));
                        }catch (Exception e){

                        }
                    }
                    list.add(cb.and(cb.and(in)));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
    public Bankcards findById(Long id) {
        Optional<Bankcards> optional = bankcardsRepository.findById(id);
        if (optional != null && optional.isPresent()) {
            Bankcards bankcards = optional.get();
            return bankcards;
        }
        return null;
    }

    @Transactional
    public void deleteBankCardById(Long id) {
        bankcardsRepository.deleteById(id);
    }

    public Bankcards findByUserIdAndDefaultCard(Long userId, int defaultCard) {
        Bankcards bankcards=bankcardsRepository.findByUserIdAndDefaultCard(userId,defaultCard);
        return bankcards;
    }

    @Transactional
    public void updateBankCards(Bankcards bankcards) {
        bankcardsRepository.save(bankcards);
    }
}
