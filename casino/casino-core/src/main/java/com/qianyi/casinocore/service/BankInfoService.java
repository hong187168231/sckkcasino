package com.qianyi.casinocore.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qianyi.casinocore.model.BankInfo;
import com.qianyi.casinocore.repository.BankInfoRepository;

@Service
public class BankInfoService {
	
    @Autowired
    BankInfoRepository bankInfoRepository;

    public List<BankInfo> findAll() {
        return bankInfoRepository.findAll();
    }

    public void saveBankInfo(BankInfo bankInfo){
        bankInfoRepository.save(bankInfo);
    }
    public void deleteBankInfo(Long id){
        bankInfoRepository.deleteById(id);
    }
}
