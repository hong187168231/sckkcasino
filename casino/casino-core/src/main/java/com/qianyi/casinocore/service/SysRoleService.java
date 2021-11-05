package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.SysRole;
import com.qianyi.casinocore.model.SysUserRole;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.SysRoleRepository;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SysRoleService {

    @Autowired
    private SysRoleRepository sysRoleRepository;

    public SysRole findById(Long sysRoleId) {
        Optional<SysRole> optional = sysRoleRepository.findById(sysRoleId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public SysRole save(SysRole sysRole) {
        return sysRoleRepository.save(sysRole);
    }

    public List<SysRole> findAll() {
        return sysRoleRepository.findAll();
    }

    public void deleteById(Long roleId) {
        sysRoleRepository.deleteById(roleId);
    }

    public List<SysRole> findAllIds(List<Long> roleIds) {
        return sysRoleRepository.findAllById(roleIds);
    }



    /**
     * 查询条件拼接，灵活添加条件
     *
     * @param sysUserRole
     * @return
     */
    private Specification<SysUserRole> getCondition(SysUserRole sysUserRole) {
        Specification<SysUserRole> specification = new Specification<SysUserRole>() {
            @Override
            public Predicate toPredicate(Root<SysUserRole> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (sysUserRole.getSysUserId() != null) {
                    list.add(cb.equal(root.get("sysRoleId").as(String.class), sysUserRole.getSysUserId()));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
}
