package com.qianyi.casinocore.service;

import com.qianyi.casinocore.repository.CompanyProxyMonthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    CompanyProxyMonthRepository companyProxyMonthRepository;

    public List<Map<String,Object>> queryAllPersonReport(String startTime,String endTime,int page,int count){
        return companyProxyMonthRepository.queryAllPersonReport(startTime,endTime,page,count);
    }

    public int queryTotalElement(String startTime,String endTime){
        return companyProxyMonthRepository.queryAllTotalElement(startTime,endTime);
    }

    public List<Map<String,Object>> queryPersonReport(long user_id ,String startTime,String endTime){
        return companyProxyMonthRepository.queryPersonReport(user_id, startTime, endTime);
    }

    public Map<String,Object> queryAllTotal(String startTime,String endTime){
        return companyProxyMonthRepository.queryAllTotal(startTime, endTime);
    }
}