package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.WashCodeChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Map;

public interface WashCodeChangeRepository extends JpaRepository<WashCodeChange,Long>, JpaSpecificationExecutor<WashCodeChange> {
    @Query(value = "  select \n" +
            "  sum(amount) wash_amount  \n" +
            "  from wash_code_change w \n" +
            "  where create_time >= ?1 and create_time <= ?2\n"
            ,nativeQuery = true)
    BigDecimal queryWashCodeChangeAll(String startTime, String endTime);
}
