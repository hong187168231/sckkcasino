package com.qianyi.casinoreport.business;

import com.qianyi.casinoreport.vo.CompanyLevelBO;
import org.junit.Test;

import javax.persistence.criteria.CriteriaBuilder;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class CompanyProxyBusinessTest {

    @Test
    public void should_get_correct_int_val(){
        CompanyLevelProcessBusiness companyProxyBusiness = new CompanyLevelProcessBusiness();
        List<Integer> valList = new ArrayList<>();
        valList.add(1);
        valList.add(5);
        valList.add(10);
        valList.add(20);
        valList.add(30);
        Map<Integer,BigDecimal> bigDecimalMap = new HashMap<>();
        bigDecimalMap.put(1,BigDecimal.valueOf(1));
        bigDecimalMap.put(5,BigDecimal.valueOf(2));
        bigDecimalMap.put(10,BigDecimal.valueOf(3));
        bigDecimalMap.put(20,BigDecimal.valueOf(4));
        bigDecimalMap.put(30,BigDecimal.valueOf(5));
        CompanyLevelBO rs = companyProxyBusiness.getProfitLevel(BigDecimal.valueOf(210293),valList,bigDecimalMap);
        System.out.println(rs);
    }

    @Test
    public void should_get_correct_start_time() {
        CompanyProxyDailyBusiness companyProxyBusiness = new CompanyProxyDailyBusiness();
        String dayTime = "2021-10-21";
        String startTime = companyProxyBusiness.getStartTime("2021-10-21");
        System.out.println(dayTime+":"+startTime);
    }

    @Test
    public void should_get_correct_end_time() {
        CompanyProxyDailyBusiness companyProxyBusiness = new CompanyProxyDailyBusiness();
        String dayTime = "2021-10-21";
        String endTime = companyProxyBusiness.getEndTime("2021-10-21");
        System.out.println(dayTime+":"+endTime);
    }

    @Test
    public void grouping_by_convert_result_test(){
//        List<BlogPost>
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTime = df.format(LocalDateTime.now());
        System.out.println(dateTime);
        System.out.println(dateTime.substring(0,10));
    }

    @Test
    public void should_get_month_correct_local_time() {
        CompanyProxyMonthBusiness companyProxyBusiness = new CompanyProxyMonthBusiness();
        String dayTime = "2021-10-28";
        String startTime = companyProxyBusiness.getMonthTime(dayTime);
        System.out.println(dayTime+":"+startTime);
    }

    @Test
    public void should_get_month_correct_start_time() {
        CompanyProxyMonthBusiness companyProxyBusiness = new CompanyProxyMonthBusiness();
        String dayTime = "2021-10-28";
        String startTime = companyProxyBusiness.getStartTime(dayTime);
        System.out.println(dayTime+":"+startTime);
    }

    @Test
    public void should_get_month_correct_end_time() {
        CompanyProxyMonthBusiness companyProxyBusiness = new CompanyProxyMonthBusiness();
        String dayTime = "2021-10-28";
        String endTime = companyProxyBusiness.getEndTime(dayTime);
        System.out.println(dayTime+":"+endTime);
    }
}