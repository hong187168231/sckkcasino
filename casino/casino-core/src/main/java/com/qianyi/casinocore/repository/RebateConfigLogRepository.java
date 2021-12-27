package com.qianyi.casinocore.repository;
import com.qianyi.casinocore.model.RebateConfigLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Date;

public interface RebateConfigLogRepository extends JpaRepository<RebateConfigLog,Long>, JpaSpecificationExecutor<RebateConfigLog> {


    RebateConfigLog findByTypeAndStaticsTimes(Integer type, String time);


    RebateConfigLog findByTypeAndStaticsTimesAndProxyUserId(Integer type, String time,Long proxyUserId);



}
