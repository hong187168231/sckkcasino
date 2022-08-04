package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.RebateDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface RebateDetailRepository extends JpaRepository<RebateDetail, Long>, JpaSpecificationExecutor<RebateDetail> {
    @Query(value = "select SUM(t1.user_amount) user_amount,SUM(t1.surplus_amount) surplus_amount,IFNULL(u.first_proxy,0) first_proxy,"
        + "IFNULL(u.second_proxy,0) second_proxy,IFNULL(u.third_proxy,0) third_proxy from (select r.user_id,ifnull(SUM(r.user_amount), 0 ) as user_amount,"
        + "ifnull(SUM(r.surplus_amount), 0 ) as surplus_amount from rebate_detail r where r.platform = ?1 and r.create_time BETWEEN ?2 and ?3 GROUP BY r.user_id) t1 left join user u on u.id = t1.user_id GROUP BY third_proxy;",nativeQuery = true)
    List<Map<String, Object>> getMapSumAmount(String platform,String startTime, String endTime);
}
