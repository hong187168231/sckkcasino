package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.UserRunningWater;
import com.qianyi.casinocore.repository.UserRunningWaterRepository;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class UserRunningWaterService {
    @Autowired
    private UserRunningWaterRepository userRunningWaterRepository;

    public void updateKey(Long userId, String staticsTimes , BigDecimal amount, BigDecimal commission){
        userRunningWaterRepository.updateKey(userId,staticsTimes,amount,commission);
    }

    public Page<UserRunningWater> findUserPage(Pageable pageable, UserRunningWater userRunningWater, Date startDate, Date endDate){
        Specification<UserRunningWater> condition = this.getCondition(userRunningWater,startDate,endDate);
        return userRunningWaterRepository.findAll(condition,pageable);
    }

    private Specification<UserRunningWater> getCondition(UserRunningWater userRunningWater, Date startDate, Date endDate) {
        Specification<UserRunningWater> specification = new Specification<UserRunningWater>() {
            @Override
            public Predicate toPredicate(Root<UserRunningWater> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (userRunningWater.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), userRunningWater.getUserId()));
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