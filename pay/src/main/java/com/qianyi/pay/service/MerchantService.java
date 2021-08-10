package com.qianyi.pay.service;

import com.qianyi.pay.model.Merchant;
import com.qianyi.pay.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class MerchantService {

    @Autowired
    MerchantRepository repository;
    public Merchant save(Merchant merchant) {
        return  repository.save(merchant);
    }
}
