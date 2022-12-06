package com.qianyi.casinocore.service;


import com.qianyi.casinocore.model.SysUserRole;
import com.qianyi.casinocore.repository.SysUserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@CacheConfig(cacheNames = {"sysUserRole"})
public class SysUserRoleService {

    @Autowired
    private SysUserRoleRepository sysUserRoleRepository;

    @Cacheable(key = "#id")
    public SysUserRole findbySysUserId(Long id) {
        return sysUserRoleRepository.findBySysUserId(id);
    }

    @CachePut(key = "#result.sysUserId", condition = "#result != null")
    public SysUserRole save(SysUserRole sysUserRole) {
        return sysUserRoleRepository.save(sysUserRole);
    }

    @CacheEvict(key = "#result.sysUserId", condition = "#result != null")
    public SysUserRole deleteBySysRoleId(Long roleId) {
        return sysUserRoleRepository.deleteBySysRoleId(roleId);
    }

    public List<SysUserRole> findAllIds(List<Long> userIds) {
        return sysUserRoleRepository.findAll(getConditionFirstPid(userIds));
    }

    private Specification<SysUserRole> getConditionFirstPid(List<Long> userIds) {
        Specification<SysUserRole> specification = new Specification<SysUserRole>() {
            @Override
            public Predicate toPredicate(Root<SysUserRole> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                Predicate predicate = cb.conjunction();
                if (userIds != null && userIds.size() > 0) {
                    Path<Object> userId = root.get("sysUserId");
                    CriteriaBuilder.In<Object> in = cb.in(userId);
                    for (Long id : userIds) {
                        in.value(id);
                    }
                    list.add(cb.and(cb.and(in)));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

    public SysUserRole findByRoleUserId(Long id) {
        return sysUserRoleRepository.findBySysUserId(id);
    }
}
