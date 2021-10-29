package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.CompanyProxyDetail;
import com.qianyi.casinocore.model.CompanyProxyMonth;
import com.qianyi.casinocore.repository.CompanyProxyMonthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompanyProxyMonthService {

    @Autowired
    private CompanyProxyMonthRepository companyProxyMonthRepository;

    public void deleteAllMonth(String monthTime){
        companyProxyMonthRepository.deleteByStaticsTimes(monthTime);
    }

    public List<CompanyProxyMonth> saveAll(List<CompanyProxyMonth> companyProxyMonthList){
        return companyProxyMonthRepository.saveAll(companyProxyMonthList);
    }

    public List<CompanyProxyMonth> queryMonthByDay(String startDate,String endDate){
        return companyProxyMonthRepository.queryMonthData(startDate,endDate);
    }
}
