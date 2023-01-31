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
        + "betting_number,bet_amount,platform,create_time,update_time) " +
        "VALUES (?1,?2,?3,?4,?5,1,?6,?7,NOW(),NOW()) ON DUPLICATE KEY UPDATE valid_amount=valid_amount + ?4,"
        + "win_loss=win_loss + ?5,betting_number = betting_number +1,bet_amount=bet_amount + ?6,update_time = NOW() ;",nativeQuery = true)
    void updateKey(Long gameRecordReportId,Long userId,String orderTimes, BigDecimal validAmount,BigDecimal winLoss,BigDecimal betAmount,String platform);

    @Modifying
    @Query(value = "INSERT INTO user_game_record_report (user_game_record_report_id,user_id,order_times,valid_amount,win_loss,"
        + "betting_number,bet_amount,platform,create_time,update_time) " +
        "VALUES (?1,?2,?3,?4,?5,0,?6,?7,NOW(),NOW()) ON DUPLICATE KEY UPDATE valid_amount=valid_amount + ?4,"
        + "win_loss=win_loss + ?5,bet_amount=bet_amount + ?6,update_time = NOW() ;",nativeQuery = true)
    void updateBet(Long gameRecordReportId,Long userId,String orderTimes, BigDecimal validAmount,BigDecimal winLoss,BigDecimal betAmount,String platform);

    @Query(value = "select u.user_id userId,SUM(u.valid_amount) validbet from user_game_record_report u where u.order_times >= ?1 and u.order_times <= ?2 GROUP BY u.user_id;",nativeQuery = true)
    List<Map<String, Object>> sumUserRunningWater(String startTime,String endTime);

    @Query(value = "select ifnull(SUM(u.valid_amount),0) validbet from user_game_record_report u where u.user_id = ?3 and u.order_times >= ?1 and u.order_times <= ?2 ;",nativeQuery = true)
    BigDecimal sumUserRunningWaterByUserId(String startTime,String endTime,Long userId);

    @Query(value = "select ifnull(SUM(betting_number),0) from user_game_record_report u where  u.order_times >= ?1 and u.order_times <= ?2 ;",nativeQuery = true)
    Integer findBetNumber(String startTime,String endTime);

    @Modifying
    @Query(value = "DELETE from user_game_record_report where order_times = ?1 ;",nativeQuery = true)
    void deleteByOrderTimes(String orderTimes);

    @Query(value = "SELECT sum(ifnull( main_t.num, 0 )) + sum(ifnull( goldenf_t.num, 0 )) + sum(ifnull( goldenf_sb.num, 0 )) + "
        + "sum(ifnull( grobdj_t.num, 0 ))+sum(ifnull( grobty_t.num, 0 )) num FROM USER u LEFT JOIN ( SELECT user_id, count( 1 ) num "
        + "FROM game_record gr WHERE bet_time >= ?1 AND bet_time <= ?2 GROUP BY user_id ) main_t "
        + "ON u.id = main_t.user_id LEFT JOIN ( SELECT user_id, count( 1 ) num FROM game_record_goldenf grg WHERE create_at_str >= ?1 "
        + "AND create_at_str <=  ?2  And vendor_code in ('PG','CQ9') GROUP BY user_id ) goldenf_t ON u.id = goldenf_t.user_id "
        + "LEFT JOIN ( select user_id ,count( DISTINCT sk.bet_id ) num FROM (select bet_id bet_id,user_id user_id from game_record_goldenf t1 where "
        + "t1.vendor_code = 'SABASPORT' AND t1.trans_type = 'Payoff' AND t1.create_at_str BETWEEN ?1 and ?2 GROUP BY t1.bet_id) off LEFT JOIN "
        + "(SELECT bet_id FROM game_record_goldenf WHERE vendor_code = 'SABASPORT' AND trans_type = 'Stake') sk ON off.bet_id = sk.bet_id  GROUP BY user_id ) goldenf_sb "
        + "ON u.id = goldenf_sb.user_id LEFT JOIN (SELECT user_id,count( 1 ) num FROM game_record_obdj grobdj WHERE bet_status "
        + "IN ( 5, 6, 8, 9, 10 ) AND set_str_time >= ?1 AND set_str_time <= ?2  "
        + "GROUP BY user_id ) grobdj_t ON u.id = grobdj_t.user_id LEFT JOIN ( SELECT user_id, count( 1 ) num FROM game_record_obty grobty "
        + "WHERE settle_str_time >= ?1 AND settle_str_time <= ?2  GROUP BY user_id ) grobty_t "
        + "ON u.id = grobty_t.user_id;",nativeQuery = true)
    Integer findTotalBetNumber(String startTime,String endTime);

    @Query(value = "SELECT count( 1 ) num from game_record_ae g where g.tx_status = 1 and g.tx_time BETWEEN ?1 and ?2 ;",nativeQuery = true)
    Integer findTotalBetNumberByAe(String startTime,String endTime);

    @Query(value = "SELECT count( 1 ) num from rpt_bet_info_detail g where  g.settle_time BETWEEN ?1 and ?2 ;",nativeQuery = true)
    Integer findTotalBetNumberByVnc(String startTime,String endTime);

    @Query(value = "SELECT count( 1 ) num from game_record_dmc g where  g.settle_time BETWEEN ?1 and ?2 ;",nativeQuery = true)
    Integer findTotalBetNumberByDmc(String startTime,String endTime);

    @Query(value = "SELECT count( 1 ) num from game_record_dg g where  g.bet_time BETWEEN ?1 and ?2 ;",nativeQuery = true)
    Integer findTotalBetNumberByDg(String startTime,String endTime);

    @Query(value = "SELECT count( 1 ) num from game_record_obzr g where  g.settle_time BETWEEN ?1 and ?2 ;",nativeQuery = true)
    Integer findTotalBetNumberByObzr(String startTime,String endTime);


    @Query(value = "select user_id user_id, count(1) num,sum(bet) bet_amount,sum(validbet) validbet ,sum(win_loss) win_loss from "
        + "game_record gr where bet_time >= ?1 and bet_time <= ?2 group by user_id ;",nativeQuery = true)
    List<Map<String, Object>> findWm(String startTime,String endTime);

    @Query(value = "select user_id user_id,vendor_code vendor_code, count(1) num,sum(bet_amount) bet_amount,sum(bet_amount) validbet,sum(win_amount-bet_amount) win_loss "
        + "from game_record_goldenf grg where create_at_str >= ?1 and create_at_str <= ?2 And vendor_code in ('PG','CQ9')"
        + "GROUP BY grg.vendor_code,grg.user_id ;",nativeQuery = true)
    List<Map<String, Object>> findPg(String startTime,String endTime);

    //    @Query(value = "select off.user_id user_id,off.vendor_code vendor_code,"
    //        + "count( DISTINCT sk.bet_id ) num,SUM( sk.bet_amount ) bet_amount,"
    //        + "SUM( sk.bet_amount ) validbet,sum(off.win_amount)-sum( sk.bet_amount ) win_loss "
    //        + "FROM (select bet_id bet_id,user_id user_id,vendor_code,SUM(win_amount) win_amount from game_record_goldenf t1 where t1.vendor_code = ?3 AND t1.trans_type = 'Payoff' "
    //        + "AND t1.create_at_str BETWEEN ?1 AND ?2 GROUP BY t1.bet_id) off LEFT JOIN (SELECT bet_amount, bet_id FROM game_record_goldenf "
    //        + "WHERE vendor_code = ?3 AND trans_type = 'Stake') sk ON off.bet_id = sk.bet_id GROUP BY user_id",nativeQuery = true)
    //    List<Map<String, Object>> findSb(String startTime,String endTime,String vendorCode);

    @Query(value = "SELECT off.user_id user_id,off.vendor_code vendor_code,"
        + "   count( DISTINCT sk.bet_id ) num, ifnull( SUM( sk.bet_amount ), 0 ) bet_amount,"
        + "   ifnull( SUM( sk.bet_amount ), 0 ) validbet,"
        + "   ifnull(sum(off.win_amount), 0 )-ifnull(sum( sk.bet_amount ), 0 )+ifnull(sum(t3.win_amount), 0 ) win_loss"
        + "   FROM ( SELECT user_id user_id,"
        + " vendor_code vendor_code, bet_id bet_id,"
        + "           SUM( win_amount ) win_amount  FROM  game_record_goldenf t1  WHERE"
        + "   t1.vendor_code = ?3  AND t1.trans_type = 'Payoff'"
        + "   AND t1.create_at_str BETWEEN ?1 AND ?2  GROUP BY t1.bet_id) off"
        + "   LEFT JOIN ( SELECT bet_amount, bet_id FROM game_record_goldenf WHERE vendor_code = ?3 AND trans_type = 'Stake' ) sk ON off.bet_id = sk.bet_id"
        + "   LEFT JOIN ( SELECT SUM(win_amount) win_amount, bet_id FROM game_record_goldenf WHERE vendor_code = ?3 AND trans_type = 'cancelPayoff' GROUP BY bet_id) t3 "
        + "ON off.bet_id = t3.bet_id GROUP BY off.user_id",nativeQuery = true)
    List<Map<String, Object>> findSb(String startTime,String endTime,String vendorCode);

    @Query(value = "select user_id user_id,count(1) num,sum(order_amount) bet_amount,sum(order_amount) validbet,sum(profit_amount) win_loss from game_record_obty grg "
        + "where settle_str_time >= ?1 and settle_str_time <= ?2 group by user_id ;",nativeQuery = true)
    List<Map<String, Object>> findObty(String startTime,String endTime);

    @Query(value = "select user_id user_id,count(1) num,sum(bet_amount) bet_amount,sum(bet_amount) validbet,sum(win_amount-bet_amount) win_loss from game_record_obdj grg "
        + "where bet_status in (5,6,8,9,10) and set_str_time >= ?1 and set_str_time <= ?2"
        + " group by user_id ;",nativeQuery = true)
    List<Map<String, Object>> findObdj(String startTime,String endTime);

    @Query(value = "select user_id user_id,count(1) num,sum(bet_amount) bet_amount,sum(valid_bet_amount) validbet,sum(payout_amount) win_loss from game_record_obzr grg "
            + "where settle_str_time >= ?1 and settle_str_time <= ?2 group by user_id ;",nativeQuery = true)
    List<Map<String, Object>> findObzr(String startTime,String endTime);

    @Query(value = "select user_id user_id,count(1) num,sum(bet_amount) bet_amount,sum(turnover) validbet,sum(real_win_amount-real_bet_amount) win_loss from game_record_ae g "
        + "where g.tx_status = 1 and g.platform = ?3 and tx_time >= ?1 and tx_time <= ?2 group by user_id ;",nativeQuery = true)
    List<Map<String, Object>> findAe(String startTime,String endTime,String platform);

    @Query(value = "select user_id user_id,count(1) num,sum(bet_amount) bet_amount,sum(turnover) validbet,sum(real_win_amount-real_bet_amount) win_loss from game_record_ae g "
        + "where g.tx_status = 1 and tx_time >= ?1 and tx_time <= ?2 group by user_id ;",nativeQuery = true)
    List<Map<String, Object>> findAe(String startTime,String endTime);

    @Query(value = "SELECT user_id user_id,count(1) num,ifnull( sum( bet_money ), 0 ) bet_amount,ifnull( sum( real_money ), 0 ) validbet,"
        + "ifnull( sum( win_money ), 0 )- ifnull( sum( real_money ), 0 ) win_loss FROM rpt_bet_info_detail grv WHERE settle_time BETWEEN ?1 AND ?2 "
        + "group by user_id ;",nativeQuery = true)
    List<Map<String, Object>> findVnc(String startTime,String endTime);

    @Query(value = "SELECT user_id user_id,count(1) num,ifnull( sum( bet_money ), 0 ) bet_amount,ifnull( sum( real_money ), 0 ) validbet,"
            + "ifnull( sum( win_money ), 0 )- ifnull( sum( real_money ), 0 ) win_loss FROM game_record_dmc grv WHERE settle_time BETWEEN ?1 AND ?2 "
            + "group by user_id ;",nativeQuery = true)
    List<Map<String, Object>> findDmc(String startTime,String endTime);

    @Query(value = "SELECT user_id user_id,count(1) num,ifnull( sum( bet_points ), 0 ) bet_amount,ifnull( sum( available_bet ), 0 ) validbet,"
            + "ifnull( sum( win_money ), 0 )- ifnull( sum( real_money ), 0 ) win_loss FROM game_record_dg grv WHERE cal_time BETWEEN ?1 AND ?2 "
            + "group by user_id ;",nativeQuery = true)
    List<Map<String, Object>> findDg(String startTime,String endTime);

    @Modifying
    @Query(value = "DELETE from user_game_record_report where platform = ?1 ;",nativeQuery = true)
    void deleteByPlatform(String platform);
}