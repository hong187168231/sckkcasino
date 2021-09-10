package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.SysUserRepository;
import com.qianyi.modulecommon.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SysUserService {

    @Autowired
    private SysUserRepository sysUserRepository;

    public List<SysUser> findAll() {
        return sysUserRepository.findAll();
    }

    public SysUser findByUserName(String userName) {
        return sysUserRepository.findByUserName(userName);
    }

    public void setSecretById(Long id, String gaKey) {
        sysUserRepository.setSecretById(id, gaKey);
    }

    public void save(SysUser sysUser) {
        sysUserRepository.save(sysUser);
    }

    public SysUser findAllById(Long id){
        return sysUserRepository.findAllById(id);
    }

    public SysUser findById(Long id){
        Optional<SysUser> optional = sysUserRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
}
