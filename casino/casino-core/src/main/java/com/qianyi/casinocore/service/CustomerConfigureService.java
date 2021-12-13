package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.CustomerConfigure;
import com.qianyi.casinocore.model.ShareProfitChange;
import com.qianyi.casinocore.repository.CustomerConfigureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class CustomerConfigureService {
    @Autowired
    CustomerConfigureRepository customerConfigureRepository;

    public List<CustomerConfigure> findAll() {
        return customerConfigureRepository.findAll();
    }

    @Cacheable(cacheNames = "customerConfigure")
    public CustomerConfigure findFirst() {
        List<CustomerConfigure> all = customerConfigureRepository.findAll();
        if (all == null || all.size() == 0) {
            return null;
        }
        return all.get(0);
    }

    @CacheEvict(cacheNames = "customerConfigure", allEntries = true)
    public CustomerConfigure save(CustomerConfigure customerConfigure){
        return customerConfigureRepository.save(customerConfigure);
    }

    public List<CustomerConfigure> saveAll(List<CustomerConfigure> customerConfigure){
        return customerConfigureRepository.saveAll(customerConfigure);
    }


    public CustomerConfigure getById(Long id){
        return customerConfigureRepository.getById(id);
    }

    @Cacheable(cacheNames = "customerConfigure")
    public List<CustomerConfigure> findByState(Integer state){
        return customerConfigureRepository.findByState(state);
    }

    public int countCustomerConfigure(Integer state){
        return customerConfigureRepository.countCustomerConfigure(state);
    }
}
