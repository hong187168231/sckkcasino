package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.repository.ProxyUserRepository;
import com.qianyi.casinocore.repository.SysUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProxyUserService {

    @Autowired
    private ProxyUserRepository proxyUserRepository;

    public List<ProxyUser> findAll() {
        return proxyUserRepository.findAll();
    }

    public ProxyUser findByUserName(String userName) {
        return proxyUserRepository.findByUserName(userName);
    }

    public void setSecretById(Long id, String gaKey) {
        proxyUserRepository.setSecretById(id, gaKey);
    }

    public void save(ProxyUser proxyUser) {
        proxyUserRepository.save(proxyUser);
    }

    public ProxyUser findAllById(Long id){
        return proxyUserRepository.findAllById(id);
    }

    public ProxyUser findById(Long id){
        Optional<ProxyUser> optional = proxyUserRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
}
