package com.qianyi.casinocore.service;

import cn.hutool.core.collection.CollUtil;
import com.qianyi.casinocore.model.CompanyManagement;
import com.qianyi.casinocore.model.ErrorOrder;
import com.qianyi.casinocore.repository.CompanyManagementRepository;
import com.qianyi.casinocore.vo.CompanyVo;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import java.util.*;

@Service
public class CompanyManagementService {

    @Autowired
    private CompanyManagementRepository repository;

    public CompanyManagement findById(Long id) {
        Optional<CompanyManagement> info = repository.findById(id);
        if (info != null && info.isPresent()) {
            return info.get();
        }
        return new CompanyManagement();
    }

    public void saveOrUpdate(CompanyManagement companyManagement) {
        repository.save(companyManagement);
    }

    public List<CompanyManagement> findAll() {
        return repository.findAll();
    }

    public List<CompanyManagement> findCompanyManagementList(Set<Long> companyIds) {
        Specification<CompanyManagement> condition = getCondition(companyIds);
        List<CompanyManagement> proxyUserList = repository.findAll(condition);
        return proxyUserList;
    }

    private Specification<CompanyManagement> getCondition(Set<Long> companyIds) {
        Specification<CompanyManagement> specification = new Specification<CompanyManagement>() {
            @Override
            public Predicate toPredicate(Root<CompanyManagement> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (CollUtil.isNotEmpty(companyIds)){
                    Path<Object> userId = root.get("id");
                    CriteriaBuilder.In<Object> in = cb.in(userId);
                    for (Long id : companyIds) {
                        in.value(id);
                    }
                    list.add(cb.and(cb.and(in)));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

    public void deleteId(Long id) {
        repository.deleteById(id);
    }

    public List<CompanyManagement> findCompany(String companyName) {
        Specification<CompanyManagement> condition = getConditionCompany(companyName);
        return repository.findAll(condition);
    }

    private Specification<CompanyManagement> getConditionCompany(String companyName) {
        Specification<CompanyManagement> specification = new Specification<CompanyManagement>() {
            @Override
            public Predicate toPredicate(Root<CompanyManagement> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(companyName)) {
                    list.add(cb.equal(root.get("companyName").as(String.class), companyName));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

    public List<Map> findGroupByCount(String companyName) {
        return repository.findGroupByCount(companyName);
    }
}
