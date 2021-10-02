package com.qianyi.casinocore.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.qianyi.casinocore.model.LoginLog;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.qianyi.casinocore.model.BankInfo;
import com.qianyi.casinocore.repository.BankInfoRepository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Service
public class BankInfoService {
	
    @Autowired
    BankInfoRepository bankInfoRepository;

    public List<BankInfo> findAll() {
        return bankInfoRepository.findAll();
    }

    public List<BankInfo> findAll(BankInfo bankInfo) {
        Sort sort = Sort.by("id").descending();
        Specification<BankInfo> condition = getCondition(bankInfo);
        return bankInfoRepository.findAll(condition,sort);
    }

    public void saveBankInfo(BankInfo bankInfo){
        bankInfoRepository.save(bankInfo);
    }

    public void deleteBankInfo(Long id){
        bankInfoRepository.deleteById(id);
    }

    public BankInfo findById(Long id) {
        Optional<BankInfo> info = bankInfoRepository.findById(id);
        if (info != null && info.isPresent()) {
            return info.get();
        }
        return null;
    }
    private Specification<BankInfo> getCondition(BankInfo bankInfo) {
        Specification<BankInfo> specification = new Specification<BankInfo>() {
            @Override
            public Predicate toPredicate(Root<BankInfo> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (bankInfo.getDisable() != null) {
                    list.add(cb.equal(root.get("disable").as(Integer.class), bankInfo.getDisable()));
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }
}
