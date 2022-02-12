package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordGoldenF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface GameRecordGoldenFRepository extends JpaRepository<GameRecordGoldenF,Long>, JpaSpecificationExecutor<GameRecordGoldenF> {


    @Modifying
    @Query("update GameRecordGoldenF u set u.codeNumStatus=?2 where u.id=?1")
    void updateCodeNumStatus(Long id, Integer codeNumStatus);

    @Modifying
    @Query("update GameRecordGoldenF u set u.washCodeStatus=?2 where u.id=?1")
    void updateWashCodeStatus(Long id, Integer washCodeStatus);

    @Modifying
    @Query("update GameRecordGoldenF u set u.shareProfitStatus= u.shareProfitStatus+?2 where u.id=?1")
    void updateProfitStatus(Long id, Integer shareProfitStatus);

    @Query(value = "select max(first_proxy) first_proxy ,max(second_proxy) second_proxy ,third_proxy third_proxy,user_id as  userId,count(distinct user_id) player_num ,max(create_at_str) bet_time, sum(bet_amount) validbet ,(case WHEN vendor_code ='PG' THEN 2 ELSE 3 END) as gameType\n" +
            "from game_record_goldenf gr\n" +
            "where\n" +
            "create_at_str between ?1 and ?2\n" +
            "and third_proxy is not null \n" +
            "group by third_proxy,user_id,vendor_code ",nativeQuery = true)
    List<Map<String,Object>> getStatisticsResult(String startTime, String endTime);

}
