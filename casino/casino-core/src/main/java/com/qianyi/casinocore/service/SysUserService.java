package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.Customer;
import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.repository.SysUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class SysUserService {

    @Autowired
    private SysUserRepository sysUserRepository;

    public List<SysUser> findAll() {
        return sysUserRepository.findAll();
    }
}
