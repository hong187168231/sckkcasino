package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.CompanyProxyMonth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CompanyProxyMonthRepository extends JpaRepository<CompanyProxyMonth,Long>, JpaSpecificationExecutor<CompanyProxyMonth> {

    void deleteByStaticsTimes(String staticsTimes);

    @Query(value = "select user_id,max(proxy_role) proxy_role ,\n" +
            "max(first_proxy) first_proxy ,max(second_proxy) second_proxy ,max(third_proxy) third_proxy ,\n" +
            "max(substr( ?1 ,1,7)) statics_times, \n" +
            "sum(player_num) player_num ,sum(group_bet_amount) group_bet_amount ,max(profit_level) profit_level ,\n" +
            "max(profit_rate) profit_rate ,sum(group_totalprofit) group_totalprofit ,max(benefit_rate)benefit_rate ,\n" +
            "sum(profit_amount) profit_amount ,max(settle_status) settle_status \n" +
            "from company_proxy_detail cpd \n" +
            "where bet_time between ?1 and ?2\n" +
            "group by user_id",nativeQuery = true)
    List<Map<String,Object>> queryMonthData(String startTime, String endTime);
}
