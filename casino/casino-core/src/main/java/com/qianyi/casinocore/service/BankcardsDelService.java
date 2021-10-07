package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.BankcardsDel;
import com.qianyi.casinocore.repository.BankcardsDelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankcardsDelService {
    @Autowired
    private BankcardsDelRepository bankcardsDelRepository;
    public void save(BankcardsDel bankcardsDel){
        bankcardsDelRepository.save(bankcardsDel);
    }
    public List<BankcardsDel> findByUserId(Long userId){
        return bankcardsDelRepository.findByUserId(userId);
    }
}
