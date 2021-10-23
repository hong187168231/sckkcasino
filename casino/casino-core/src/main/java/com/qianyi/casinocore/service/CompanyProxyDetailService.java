package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.CompanyProxyDetail;
import com.qianyi.casinocore.repository.CompanyProxyDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyProxyDetailService {

    @Autowired
    private CompanyProxyDetailRepository companyProxyDetailRepository;

    public CompanyProxyDetail save(CompanyProxyDetail companyProxyDetail){
        return companyProxyDetailRepository.save(companyProxyDetail);
    }

    public List<CompanyProxyDetail> saveAll(List<CompanyProxyDetail> companyProxyDetailList){
        return companyProxyDetailRepository.saveAll(companyProxyDetailList);
    }

    public CompanyProxyDetail getCompanyProxyDetailByUidAndTime(Long uid,String staticsTime){
        return companyProxyDetailRepository.getCompanyProxyDetailByUserIdAndStaticsTimes(uid,staticsTime);
    }
}
