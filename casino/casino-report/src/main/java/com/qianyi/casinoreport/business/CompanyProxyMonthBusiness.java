package com.qianyi.casinoreport.business;

import com.qianyi.casinocore.model.CompanyProxyMonth;
import com.qianyi.casinocore.service.CompanyProxyMonthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Slf4j
@Service
public class CompanyProxyMonthBusiness {

    @Autowired
    private CompanyProxyMonthService companyProxyMonthService;

    //传入计算当天的时间  yyyy-MM-dd 格式
    @Transactional
    public void processCompanyMonth(String dayTime){
        String starTime = getStartTime(dayTime);
        String endTime = getEndTime(dayTime);
        //删除当月的数据
        companyProxyMonthService.deleteAllMonth(getMonthTime(dayTime));
        //统计当月的数据
        List<CompanyProxyMonth> companyProxyMonthList = companyProxyMonthService.queryMonthByDay(starTime,endTime);
        //插入当月的数据
        companyProxyMonthService.saveAll(companyProxyMonthList);
    }

    public String getMonthTime(String dayTime){
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localtime = LocalDateTime.parse(dayTime+"T00:00:00");
        String strLocalTime = df.format(localtime.plusDays(-1));
        return strLocalTime.substring(0,7);
    }

    public String getStartTime(String dayTime){
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.parse(dayTime+"T00:00:00");
        startTime = startTime.plusDays(-1).with(TemporalAdjusters.firstDayOfMonth());
        return df.format(startTime);
    }

    public String getEndTime(String dayTime){
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime endTime = LocalDateTime.parse(dayTime+"T00:00:00");
        endTime=endTime.plusSeconds(-1);
        return df.format(endTime);
    }
}
