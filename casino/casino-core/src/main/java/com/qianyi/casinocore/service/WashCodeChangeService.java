package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.WashCodeChange;
import com.qianyi.casinocore.repository.WashCodeChangeRepository;
import com.qianyi.modulecommon.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class WashCodeChangeService {
    @Autowired
    private WashCodeChangeRepository washCodeChangeRepository;

    @Autowired
    private EntityManager entityManager;


    public WashCodeChange save(WashCodeChange washCodeChange) {
        return washCodeChangeRepository.save(washCodeChange);
    }

    public List<WashCodeChange> getList(Long userId, String startTime, String endTime) {
        List<WashCodeChange> list =new ArrayList<>();
        List<WashCodeChange> wmList = getWmList(Constants.PLATFORM_WM, userId, startTime, endTime);
        List<WashCodeChange> pgList = getWmList(Constants.PLATFORM_PG, userId, startTime, endTime);
        List<WashCodeChange> cq9List = getWmList(Constants.PLATFORM_CQ9, userId, startTime, endTime);
        list.addAll(wmList);
        list.addAll(pgList);
        list.addAll(cq9List);
        return list;
    }
    public List<WashCodeChange> getWmList(String platform,Long userId, String startTime, String endTime) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<WashCodeChange> query = builder.createQuery(WashCodeChange.class);
        Root<WashCodeChange> root = query.from(WashCodeChange.class);
        query.multiselect(
                root.get("platform").as(String.class),
                root.get("gameId").as(String.class),
                root.get("gameName").as(String.class),
                builder.sum(root.get("amount").as(BigDecimal.class)).alias("amount"),
                builder.sum(root.get("validbet").as(BigDecimal.class)).alias("validbet")
        );

        List<Predicate> predicates = new ArrayList();
        predicates.add(builder.equal(root.get("userId").as(Long.class), userId));
        predicates.add(builder.equal(root.get("platform").as(String.class), platform));
        if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
            predicates.add(builder.between(root.get("createTime").as(String.class), startTime, endTime));
        }
        if(Constants.PLATFORM_PG.equals(platform)||Constants.PLATFORM_CQ9.equals(platform)){
            query.where(predicates.toArray(new Predicate[predicates.size()])).groupBy(root.get("platform"));
        }else {
            query.where(predicates.toArray(new Predicate[predicates.size()])).groupBy(root.get("platform"), root.get("gameId"));
        }
        List<WashCodeChange> list = entityManager.createQuery(query).getResultList();
        return list;
    }

    public List<WashCodeChange> findUserList( Date startDate, Date endDate) {
        Specification<WashCodeChange> condition = this.getCondition(startDate,endDate);
        return washCodeChangeRepository.findAll(condition);
    }

    public BigDecimal queryWashCodeChangeAll(String startTime, String endTime){
        return washCodeChangeRepository.queryWashCodeChangeAll(startTime,endTime);
    }

    private Specification<WashCodeChange> getCondition(Date startDate,Date endDate) {
        Specification<WashCodeChange> specification = new Specification<WashCodeChange>() {
            @Override
            public Predicate toPredicate(Root<WashCodeChange> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();

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
