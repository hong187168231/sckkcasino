package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.CompanyManagement;
import com.qianyi.casinocore.repository.CompanyManagementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class CompanyManagementService {

    @Autowired
    private CompanyManagementRepository repository;

    public CompanyManagement findById(Long id) {
        Optional<CompanyManagement> info = repository.findById(id);
        if (info != null && info.isPresent()) {
            return info.get();
        }
        return new CompanyManagement();
    }

    public void saveOrUpdate(CompanyManagement companyManagement) {
        repository.save(companyManagement);
    }

    public List<CompanyManagement> findAll() {
        return repository.findAll();
    }
}
