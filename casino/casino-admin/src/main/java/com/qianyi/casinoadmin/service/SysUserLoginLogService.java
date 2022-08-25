package com.qianyi.casinoadmin.service;

import com.qianyi.casinoadmin.model.SysUserLoginLog;
import com.qianyi.casinoadmin.repository.SysUserLoginLogRepository;
import com.qianyi.casinocore.model.LoginLog;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class SysUserLoginLogService {

    @Autowired
    SysUserLoginLogRepository sysUserLoginLogRepository;

    /**
     * 独立线程执行此方法
     * @param sysUserLoginLog
     */
    @Async
    public void saveSyncLog(SysUserLoginLog sysUserLoginLog) {
        if (!ObjectUtils.isEmpty(sysUserLoginLog.getIp())) {
            //得到IP归属地
            String address = IpUtil.getAddress(sysUserLoginLog.getIp());
            if (address != null) {
                sysUserLoginLog.setAddress(address);
            }
        }

        sysUserLoginLogRepository.save(sysUserLoginLog);
    }

    public Page<SysUserLoginLog> findLoginLogPage(SysUserLoginLog loginLog, Pageable pageable, Date startDate, Date endDate) {
        Specification<SysUserLoginLog> condition = this.getCondition(loginLog, startDate, endDate);
        return sysUserLoginLogRepository.findAll(condition,pageable);

    }

    private Specification<SysUserLoginLog> getCondition(SysUserLoginLog loginLog, Date startDate, Date endDate) {

        Specification<SysUserLoginLog> specification = new Specification<SysUserLoginLog>() {
            @Override
            public Predicate toPredicate(Root<SysUserLoginLog> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(loginLog.getIp())) {
                    list.add(cb.equal(root.get("ip").as(String.class), loginLog.getIp()));
                }

                if (loginLog.getUserId() !=null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), loginLog.getUserId()));
                }
                if (!CommonUtil.checkNull(loginLog.getUserName())) {
                    list.add(cb.equal(root.get("userName").as(String.class), loginLog.getUserName()));
                }
                if (!CommonUtil.checkNull(loginLog.getIp())) {
                    list.add(cb.equal(root.get("ip").as(String.class), loginLog.getIp()));
                }
                if (startDate != null) {
                    list.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), startDate));
                }
                if (endDate != null) {
                    list.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class), endDate));
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }

}
