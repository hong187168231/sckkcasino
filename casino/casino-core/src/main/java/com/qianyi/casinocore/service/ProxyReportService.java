package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ProxyReport;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.WithdrawOrder;
import com.qianyi.casinocore.repository.ProxyReportRepository;
import com.qianyi.modulecommon.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProxyReportService {
    @Autowired
    private ProxyReportRepository proxyReportRepository;
    @Autowired
    private UserService userService;

    public ProxyReport findByUserId(Long userId){
        return proxyReportRepository.findProxyReportByUserId(userId);
    }

    public ProxyReport save(ProxyReport proxyReport){
        return proxyReportRepository.save(proxyReport);
    }

    public Page<ProxyReport> findAchievementPage(Pageable pageable, Long userId, Long memberId) {
        Specification<ProxyReport> condition = this.getCondition(userId,memberId);
        return proxyReportRepository.findAll(condition, pageable);
    }

    private Specification<ProxyReport> getCondition(Long userId, Long memberId) {
        Specification<ProxyReport> specification = new Specification<ProxyReport>() {
            @Override
            public Predicate toPredicate(Root<ProxyReport> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (memberId != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), memberId));
                } else {
                    List<User> users = userService.findByStateAndFirstPid(Constants.open, userId);
                    if (!CollectionUtils.isEmpty(users)) {
                        CriteriaBuilder.In<Object> in = cb.in(root.get("userId"));
                        for (User user : users) {
                            in.value(user.getId());
                        }
                        list.add(cb.and(cb.and(in)));
                    }else{
                        list.add(cb.equal(root.get("userId").as(Long.class), 0L));
                    }
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
}
