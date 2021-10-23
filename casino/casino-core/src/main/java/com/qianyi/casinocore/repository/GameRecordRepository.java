package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.vo.CompanyOrderAmountVo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GameRecordRepository extends JpaRepository<GameRecord, Long>, JpaSpecificationExecutor<GameRecord> {

    @Query(value = "select third_proxy,count(distinct user_id) player_num ,max(first_proxy) first_proxy ,max(second_proxy) second_proxy , sum(validbet)validbet,max(bet_time) bet_time \n" +
            "from game_record gr\n" +
            "where\n" +
            "validbet between ?1 and ?2\n" +
            "and third_proxy is not null \n" +
            "group by third_proxy ",nativeQuery = true)
    List<CompanyOrderAmountVo> getStatisticsResult(String startTime, String endTime);
}
