package com.qianyi.casinocore.service;


import com.qianyi.casinocore.model.SysUserRole;
import com.qianyi.casinocore.repository.SysUserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class SysUserRoleService {

    @Autowired
    private SysUserRoleRepository sysUserRoleRepository;

    public SysUserRole findbySysUserId(Long userid) {

        return sysUserRoleRepository.findBySysUserId(userid);
    }

    public SysUserRole save(SysUserRole sysUserRole) {
        return sysUserRoleRepository.save(sysUserRole);
    }

    public void deleteById(Long roleId) {
        sysUserRoleRepository.deleteBySysRoleId(roleId);
    }
}
