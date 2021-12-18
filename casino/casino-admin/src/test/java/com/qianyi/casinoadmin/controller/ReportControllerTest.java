package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.repository.CompanyProxyMonthRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class ReportControllerTest {

    @Autowired
    CompanyProxyMonthRepository companyProxyMonthRepository;

    @Test
    public void should_query_data(){
        List<Map<String,Object>> result = companyProxyMonthRepository.queryAllPersonReport("2021-11-01 00:00:00","2021-11-30 00:00:00",10,10);
        result.forEach(item ->{
            log.info("{}",item);
        });
    }

    @Test
    public void should_query_single_person_data(){
        List<Map<String,Object>> result = companyProxyMonthRepository.queryPersonReport(3,"2021-11-01 00:00:00","2021-11-30 00:00:00");
        result.forEach(item ->{
            log.info("{}",item);
        });
    }

    @Test
    public void should_query_all_total_data(){
        Map<String,Object> result = companyProxyMonthRepository.queryAllTotal("2021-11-01 00:00:00","2021-11-30 00:00:00");

        log.info("{}",result);
    }
}