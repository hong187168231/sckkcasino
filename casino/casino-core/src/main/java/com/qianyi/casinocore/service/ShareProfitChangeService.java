package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ShareProfitChange;
import com.qianyi.casinocore.repository.ShareProfitChangeRepository;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class ShareProfitChangeService {

    @Autowired
    private ShareProfitChangeRepository shareProfitChangeRepository;

    public ShareProfitChange save(ShareProfitChange shareProfitChange){
        return shareProfitChangeRepository.save(shareProfitChange);
    }

    public List<ShareProfitChange> saveAll(List<ShareProfitChange> shareProfitChangeList){
        return shareProfitChangeRepository.saveAll(shareProfitChangeList);
    }

    public List<ShareProfitChange> findAll(Long fromUserId, Date startDate, Date endDate){
        Specification<ShareProfitChange> condition = getCondition(fromUserId,startDate,endDate);
        return shareProfitChangeRepository.findAll(condition);
    }
    /**
     * 查询条件拼接，灵活添加条件
     *
     * @param fromUserId
     * @return
     */
    private Specification<ShareProfitChange> getCondition(Long fromUserId, Date startDate, Date endDate) {
        Specification<ShareProfitChange> specification = new Specification<ShareProfitChange>() {
            @Override
            public Predicate toPredicate(Root<ShareProfitChange> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (fromUserId != null) {
                    list.add(cb.equal(root.get("fromUserId").as(String.class), fromUserId));
                }
                if (startDate != null) {
                    list.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), startDate));
                }
                if (endDate != null) {
                    list.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class),endDate));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
}
