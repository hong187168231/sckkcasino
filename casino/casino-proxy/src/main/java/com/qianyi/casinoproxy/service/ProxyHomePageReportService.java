package com.qianyi.casinoproxy.service;

import com.qianyi.casinoproxy.model.ProxyHomePageReport;
import com.qianyi.casinoproxy.repository.ProxyHomePageReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProxyHomePageReportService {
    @Autowired
    private ProxyHomePageReportRepository proxyHomePageReportRepository;

    public ProxyHomePageReport save(ProxyHomePageReport proxyHomePageReport){
        return proxyHomePageReportRepository.save(proxyHomePageReport);
    }

    public List<ProxyHomePageReport> findHomePageReports(ProxyHomePageReport proxyHomePageReport,String startTime, String endTime) {
        Specification<ProxyHomePageReport> condition = this.getCondition(proxyHomePageReport,startTime,endTime);
        return proxyHomePageReportRepository.findAll(condition);
    }

    public List<ProxyHomePageReport> findHomePageReports(ProxyHomePageReport proxyHomePageReport,String startTime, String endTime, Sort sort) {
        Specification<ProxyHomePageReport> condition = this.getCondition(proxyHomePageReport,startTime,endTime);
        return proxyHomePageReportRepository.findAll(condition,sort);
    }

    private Specification<ProxyHomePageReport> getCondition(ProxyHomePageReport proxyHomePageReport,String startTime, String endTime) {
        Specification<ProxyHomePageReport> specification = new Specification<ProxyHomePageReport>() {
            @Override
            public Predicate toPredicate(Root<ProxyHomePageReport> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (proxyHomePageReport.getProxyUserId() != null) {
                    list.add(cb.equal(root.get("proxyUserId").as(Long.class), proxyHomePageReport.getProxyUserId()));
                }
                if (proxyHomePageReport.getProxyRole() != null) {
                    list.add(cb.equal(root.get("proxyRole").as(Long.class), proxyHomePageReport.getProxyRole()));
                }
                if (proxyHomePageReport.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), proxyHomePageReport.getFirstProxy()));
                }
                if (proxyHomePageReport.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), proxyHomePageReport.getSecondProxy()));
                }
                if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                    list.add(cb.between(root.get("staticsTimes").as(String.class), startTime, endTime));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
}
