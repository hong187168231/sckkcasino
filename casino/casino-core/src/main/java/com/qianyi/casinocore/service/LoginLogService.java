package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.LoginLog;
import com.qianyi.casinocore.repository.LoginLogRepository;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class LoginLogService {
    @Autowired
    LoginLogRepository loginLogRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public LoginLog save(LoginLog loginLog) {
        return loginLogRepository.save(loginLog);
    }

    public Page<LoginLog> findLoginLogPage(LoginLog loginLog ,Pageable pageable){
        Specification<LoginLog> condition = this.getCondition(loginLog);
        return loginLogRepository.findAll(condition,pageable);
    }

    public List<LoginLog> findLoginLogList(String ip){
        return loginLogRepository.findLoginLogList(ip);
    }

    public List findLoginLogGroupBy(String ip) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoginLog> query = builder.createQuery(LoginLog.class);
        Root<LoginLog> root = query.from(LoginLog.class);

        List<Predicate> predicates = new ArrayList();

        if (!CommonUtil.checkNull(ip)) {
            predicates.add(
                    builder.equal(root.get("ip").as(String.class), ip)
            );
        }
        query
                .where(predicates.toArray(new Predicate[predicates.size()]))
                .orderBy(builder.desc(root.get("createTime")))
                .groupBy(root.get("account"));

        List<LoginLog> list = entityManager.createQuery(query).getResultList();
        return list;
    }
    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
     */
    private Specification<LoginLog> getCondition(LoginLog loginLog) {
        Specification<LoginLog> specification = new Specification<LoginLog>() {
            @Override
            public Predicate toPredicate(Root<LoginLog> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(loginLog.getIp())) {
                    list.add(cb.equal(root.get("ip").as(String.class), loginLog.getIp()));
                }
                if (loginLog.getUserId() !=null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), loginLog.getUserId()));
                }
                if (!CommonUtil.checkNull(loginLog.getAccount())) {
                    list.add(cb.equal(root.get("account").as(String.class), loginLog.getAccount()));
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }

    public Page<LoginLog> pageByCondition(LoginLog loginLog, Pageable pageable) {
        Specification<LoginLog> specification=(Specification<LoginLog>) (root,query,builder)->{
            List<Predicate> list = new ArrayList<>();
            // 第一个userId为CloudServerDao中的字段，第二个userId为参数
            if (loginLog.getUserId()!=null) {
                // 此处为查询serverName中含有key的数据
                Predicate p1 = builder.equal(root.get("userId"),loginLog.getUserId() );
                list.add(p1);
            }
            return builder.and(list.toArray(new Predicate[0]));
        };

        return loginLogRepository.findAll(specification, pageable);

    }
}
