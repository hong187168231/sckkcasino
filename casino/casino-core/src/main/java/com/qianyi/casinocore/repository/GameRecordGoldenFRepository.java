package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordGoldenF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
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

    @Query(value = "select ifnull(sum(g.bet_amount),0) betAmount from game_record_goldenf g where g.create_at_str "
        + " BETWEEN ?2  and ?3 and g.user_id = ?1 ",nativeQuery = true)
    BigDecimal findSumBetAmount(Long userId,String startTime,String endTime);

    @Query(value = "select MAX(g.id) maxId,LEFT(g.create_at_str,?2) set_time,ifnull(g.first_proxy,0) first_proxy," +
        "ifnull(g.second_proxy,0) second_proxy,ifnull(g.third_proxy,0) third_proxy," +
        "COUNT(1) num,SUM(g.bet_amount) bet,SUM(g.bet_amount) validbet,SUM(g.win_amount) win_loss," +
        " ifnull(SUM(w.amount),0) amount from game_record_goldenf g left join  " +
        "wash_code_change w  on  w.game_record_id = g.id and w.platform = ?3 where g.id > ?1 and g.vendor_code = ?3 " +
        " GROUP BY g.third_proxy,LEFT(g.create_at_str,?2)  ",nativeQuery = true)
    List<Map<String,Object>> queryGameRecords(Long id,Integer num,String platform);

    @Query(value = "select g.user_id userId,SUM(g.bet_amount) betAmount from game_record_goldenf g where g.create_at_str BETWEEN ?1 and ?2 GROUP BY g.user_id;",nativeQuery = true)
    List<Map<String, Object>> findSumBetAmount(String startTime,String endTime);
}
