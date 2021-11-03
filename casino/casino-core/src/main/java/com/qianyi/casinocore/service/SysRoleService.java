package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.SysRole;
import com.qianyi.casinocore.repository.SysRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
}
