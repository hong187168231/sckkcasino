package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.LoginLog;
import com.qianyi.casinocore.repository.LoginLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class LoginLogService {
    @Autowired
    LoginLogRepository loginLogRepository;

    public LoginLog save(LoginLog loginLog) {
        return loginLogRepository.save(loginLog);
    }

    public Page<LoginLog> findLoginLogPage(Specification<LoginLog> condition,Pageable pageable){
        return loginLogRepository.findAll(condition,pageable);
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
