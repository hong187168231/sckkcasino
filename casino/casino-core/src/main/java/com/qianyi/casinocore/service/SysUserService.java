package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.repository.SysUserRepository;
import com.qianyi.modulecommon.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    public SysUser findByUserName(String userName) {
        return sysUserRepository.findByUserName(userName);
    }

    public void setSecretById(Long id, String gaKey) {
        sysUserRepository.setSecretById(id, gaKey);
    }

    public void saveSysUser(String userName, String password, String nickName) {
        //密码进行加密处理
        BCryptPasswordEncoder passwordEncoder=new BCryptPasswordEncoder();
        //加密
        String newPassword = passwordEncoder.encode(password);
        SysUser sysUser = new SysUser();
        sysUser.setUserName(userName);
        sysUser.setNickName(nickName);
        sysUser.setPassWord(password);
        sysUser.setUserFlag(Constants.open);
        sysUserRepository.save(sysUser);
    }
}
