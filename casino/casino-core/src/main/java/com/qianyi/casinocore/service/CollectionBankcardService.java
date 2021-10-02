package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.CollectionBankcard;
import com.qianyi.casinocore.model.LoginLog;
import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.repository.CollectionBankCardRepository;
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
public class CollectionBankcardService {

    @Autowired
    private CollectionBankCardRepository collectionBankCardRepository;

    public List<CollectionBankcard> getCollectionBandcards(){
        return collectionBankCardRepository.findAll();
    }
    public Page<CollectionBankcard> getCollectionBandPage(CollectionBankcard collectionBankcard , Pageable pageable){
        Specification<CollectionBankcard> condition = this.getCondition(collectionBankcard);
        return collectionBankCardRepository.findAll(condition,pageable);
    }
    public List<CollectionBankcard> findAll(CollectionBankcard collectionBankcard ){
        Specification<CollectionBankcard> condition = this.getCondition(collectionBankcard);
        return collectionBankCardRepository.findAll(condition);
    }

    public List<CollectionBankcard> findByBankNo(String bankNo) {
        return collectionBankCardRepository.findByBankNo(bankNo);
    }

    public void save(CollectionBankcard bankcard) {
        collectionBankCardRepository.save(bankcard);
    }

    public CollectionBankcard findById(Long id) {
        Optional<CollectionBankcard> optional = collectionBankCardRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public List<CollectionBankcard> findAll() {
        return collectionBankCardRepository.findAll();
    }

    public List<CollectionBankcard> findByDisable(Integer disable){
        return collectionBankCardRepository.findByDisable(disable);
    }

    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
     */
    private Specification<CollectionBankcard> getCondition(CollectionBankcard collectionBankcard ) {
        Specification<CollectionBankcard> specification = new Specification<CollectionBankcard>() {
            @Override
            public Predicate toPredicate(Root<CollectionBankcard> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (collectionBankcard.getDisable() != null) {
                    list.add(cb.equal(root.get("disable").as(Integer.class), collectionBankcard.getDisable()));
                }
                if (!CommonUtil.checkNull(collectionBankcard.getBankId())) {
                    list.add(cb.equal(root.get("bankId").as(String.class), (collectionBankcard.getBankId())));
                }
//                if (!CommonUtil.checkNull(loginLog.getAccount())) {
//                    list.add(cb.equal(root.get("account").as(String.class), loginLog.getAccount()));
//                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }
}
