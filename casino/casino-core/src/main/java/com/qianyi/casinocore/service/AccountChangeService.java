package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.AccountChange;
import com.qianyi.casinocore.repository.AccountChangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountChangeService {
    @Autowired
    private AccountChangeRepository accountChangeRepository;

    public AccountChange save(AccountChange po){
        return accountChangeRepository.save(po);
    }
}
