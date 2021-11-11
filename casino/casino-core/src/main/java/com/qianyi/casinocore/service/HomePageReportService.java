package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.HomePageReport;
import com.qianyi.casinocore.repository.HomePageReportRepository;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class HomePageReportService {

    @Autowired
    private HomePageReportRepository homePageReportRepository;

    public HomePageReport save(HomePageReport homePageReport){
        return homePageReportRepository.save(homePageReport);
    }

    public List<HomePageReport> findHomePageReports(String startTime, String endTime) {
        Specification<HomePageReport> condition = this.getCondition(startTime,endTime);
        return homePageReportRepository.findAll(condition);
    }
    /**
     * 查询条件拼接，灵活添加条件
     *
     * @param
     * @return
     */
    private Specification<HomePageReport> getCondition(Date startDate, Date endDate) {
        Specification<HomePageReport> specification = new Specification<HomePageReport>() {
            @Override
            public Predicate toPredicate(Root<HomePageReport> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
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
    private Specification<HomePageReport> getCondition(String startTime, String endTime) {
        Specification<HomePageReport> specification = new Specification<HomePageReport>() {
            @Override
            public Predicate toPredicate(Root<HomePageReport> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                    list.add(cb.between(root.get("staticsTimes").as(String.class), startTime, endTime));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
}
