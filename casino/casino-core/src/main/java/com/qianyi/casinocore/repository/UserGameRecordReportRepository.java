package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.UserGameRecordReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface UserGameRecordReportRepository extends JpaRepository<UserGameRecordReport,Long>,
    JpaSpecificationExecutor<UserGameRecordReport> {

    @Modifying
    @Query(value = "INSERT INTO user_game_record_report (user_game_record_report_id,user_id,order_times,valid_amount,win_loss,"
        + "betting_number,bet_amount,platform) " +
        "VALUES (?1,?2,?3,?4,?5,1,?6,?7) ON DUPLICATE KEY UPDATE valid_amount=valid_amount + ?4,"
        + "win_loss=win_loss + ?5,betting_number = betting_number +1,bet_amount=bet_amount + ?6 ;",nativeQuery = true)
    void updateKey(Long gameRecordReportId,Long userId,String orderTimes, BigDecimal validAmount,BigDecimal winLoss,BigDecimal betAmount,String platform);

    @Query(value = "select u.user_id userId,SUM(u.valid_amount) validbet from user_game_record_report u where u.order_times >= ?1 and u.order_times <= ?2 GROUP BY u.user_id;",nativeQuery = true)
    List<Map<String, Object>> sumUserRunningWater(String startTime,String endTime);

    @Query(value = "select ifnull(SUM(u.valid_amount),0) validbet from user_game_record_report u where u.user_id = ?3 and u.order_times >= ?1 and u.order_times <= ?2 ;",nativeQuery = true)
    BigDecimal sumUserRunningWaterByUserId(String startTime,String endTime,Long userId);

    @Query(value = "select ifnull(SUM(betting_number),0) from user_game_record_report u where  u.order_times >= ?1 and u.order_times <= ?2 ;",nativeQuery = true)
    Integer findBetNumber(String startTime,String endTime);

    @Modifying
    @Query(value = "DELETE from user_game_record_report where order_times = ?1 ;",nativeQuery = true)
    void deleteByOrderTimes(String orderTimes);

    @Query(value = "SELECT sum(ifnull( main_t.num, 0 )) + sum(ifnull( goldenf_t.num, 0 ))+ sum(ifnull( grobdj_t.num, 0 ))"
        + "+ sum(ifnull( grobty_t.num, 0 )) num FROM USER u LEFT JOIN ( SELECT user_id, count( 1 ) num FROM game_record gr "
        + "WHERE bet_time >= ?1 AND bet_time <= ?2 GROUP BY user_id ) main_t "
        + "ON u.id = main_t.user_id LEFT JOIN ( SELECT user_id, count( 1 ) num FROM game_record_goldenf grg "
        + "WHERE create_at_str >= ?1 AND create_at_str <= ?2 GROUP BY user_id ) goldenf_t "
        + "ON u.id = goldenf_t.user_id LEFT JOIN (SELECT user_id,count( 1 ) num FROM game_record_obdj grobdj "
        + "WHERE bet_status IN ( 5, 6, 8, 9, 10 ) AND set_str_time >= ?1  AND set_str_time <= ?2 "
        + "GROUP BY user_id ) grobdj_t ON u.id = grobdj_t.user_id LEFT JOIN ( SELECT user_id, count( 1 ) num FROM game_record_obty grobty "
        + "WHERE settle_str_time >= ?1 AND settle_str_time <= ?2 GROUP BY user_id ) grobty_t "
        + "ON u.id = grobty_t.user_id ;",nativeQuery = true)
    Integer findTotalBetNumber(String startTime,String endTime);

    @Query(value = "select user_id user_id, count(1) num,sum(bet) bet_amount,sum(validbet) validbet ,sum(win_loss) win_loss from "
        + "game_record gr where bet_time >= ?1 and bet_time <= ?2 group by user_id ;",nativeQuery = true)
    List<Map<String, Object>> findWm(String startTime,String endTime);

    @Query(value = "select user_id user_id,vendor_code vendor_code, count(1) num,sum(bet_amount) bet_amount,sum(bet_amount) validbet,sum(win_amount-bet_amount) win_loss "
        + "from game_record_goldenf grg where create_at_str >= ?1 and create_at_str <= ?2 "
        + "GROUP BY grg.vendor_code,grg.user_id ;",nativeQuery = true)
    List<Map<String, Object>> findPg(String startTime,String endTime);

    @Query(value = "select user_id user_id,count(1) num,sum(order_amount) bet_amount,sum(order_amount) validbet,sum(profit_amount) win_loss from game_record_obty grg "
        + "where settle_str_time >= ?1 and settle_str_time <= ?2 group by user_id ;",nativeQuery = true)
    List<Map<String, Object>> findObty(String startTime,String endTime);

    @Query(value = "select user_id user_id,count(1) num,sum(bet_amount) bet_amount,sum(bet_amount) validbet,sum(win_amount-bet_amount) win_loss from game_record_obdj grg "
        + "where bet_status in (5,6,8,9,10) and set_str_time >= ?1 and set_str_time <= ?2"
        + " group by user_id ;",nativeQuery = true)
    List<Map<String, Object>> findObdj(String startTime,String endTime);
}
