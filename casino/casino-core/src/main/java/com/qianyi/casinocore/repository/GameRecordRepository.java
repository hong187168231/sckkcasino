package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.vo.CompanyOrderAmountVo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.Tuple;
import java.util.List;
import java.util.Map;

public interface GameRecordRepository extends JpaRepository<GameRecord, Long>, JpaSpecificationExecutor<GameRecord> {

    @Query(value = "select max(first_proxy) first_proxy ,max(second_proxy) second_proxy ,third_proxy third_proxy,count(distinct user_id) player_num ,max(bet_time) bet_time, sum(validbet) validbet \n" +
            "from game_record gr\n" +
            "where\n" +
            "bet_time between ?1 and ?2\n" +
            "and third_proxy is not null \n" +
            "group by third_proxy ",nativeQuery = true)
    List<Map<String,Object>> getStatisticsResult(String startTime, String endTime);

    @Modifying
    @Query("update GameRecord u set u.codeNumStatus=?2 where u.id=?1")
    void updateCodeNumStatus(Long id, Integer codeNumStatus);

    @Modifying
    @Query("update GameRecord u set u.washCodeStatus=?2 where u.id=?1")
    void updateWashCodeStatus(Long id, Integer washCodeStatus);
}
