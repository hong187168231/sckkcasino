package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.Customer;
import com.qianyi.casinocore.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class CustomerService {
    @Autowired
    CustomerRepository customerRepository;

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Customer findFirst() {
        List<Customer> all = customerRepository.findAll();
        if (all == null || all.size() == 0) {
            return null;
        }

        return all.get(0);
    }
    public void save(Customer customer){
        customerRepository.save(customer);
    }
}
