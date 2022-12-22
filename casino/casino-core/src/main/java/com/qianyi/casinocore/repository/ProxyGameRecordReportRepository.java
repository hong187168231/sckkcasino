package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ProxyGameRecordReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ProxyGameRecordReportRepository extends JpaRepository<ProxyGameRecordReport,Long>,
    JpaSpecificationExecutor<ProxyGameRecordReport> {

    @Modifying
    @Query(value = "INSERT INTO proxy_game_record_report (proxy_game_record_report_id,user_id,order_times,valid_amount,win_loss,"
        + "first_proxy,second_proxy,third_proxy,betting_number,bet_amount,create_time,update_time) " +
        "VALUES (?1,?2,?3,?4,?5,?6,?7,?8,1,?9,NOW(),NOW()) ON DUPLICATE KEY UPDATE valid_amount=valid_amount + ?4,"
        + "win_loss=win_loss + ?5,betting_number = betting_number +1,bet_amount=bet_amount + ?9,update_time = NOW() ;",nativeQuery = true)
    void updateKey(Long gameRecordReportId,Long userId,String orderTimes, BigDecimal validAmount,BigDecimal winLoss,Long firstProxy,Long secondProxy,Long thirdProxy,BigDecimal betAmount);

    @Modifying
    @Query(value = "INSERT INTO proxy_game_record_report (proxy_game_record_report_id,user_id,order_times,valid_amount,win_loss,"
        + "first_proxy,second_proxy,third_proxy,betting_number,bet_amount,create_time,update_time) " +
        "VALUES (?1,?2,?3,?4,?5,?6,?7,?8,0,?9,NOW(),NOW()) ON DUPLICATE KEY UPDATE valid_amount=valid_amount + ?4,"
        + "win_loss=win_loss + ?5,bet_amount=bet_amount + ?9,update_time = NOW() ;",nativeQuery = true)
    void updateBet(Long gameRecordReportId,Long userId,String orderTimes, BigDecimal validAmount,BigDecimal winLoss,Long firstProxy,Long secondProxy,Long thirdProxy,BigDecimal betAmount);

    @Modifying
    @Query(value = "INSERT INTO proxy_game_record_report (proxy_game_record_report_id,user_id,order_times,valid_amount,win_loss,"
        + "first_proxy,second_proxy,third_proxy,betting_number,bet_amount,create_time,update_time) " +
        "VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?10,?9,NOW(),NOW()) ON DUPLICATE KEY UPDATE valid_amount=valid_amount + ?4,"
        + "win_loss=win_loss + ?5,betting_number = betting_number +?10,bet_amount=bet_amount + ?9,update_time = NOW() ;",nativeQuery = true)
    void updateKey(Long gameRecordReportId,Long userId,String orderTimes, BigDecimal validAmount,BigDecimal winLoss,Long firstProxy,Long secondProxy,Long thirdProxy,BigDecimal betAmount,Integer bettingNumber);

    @Query(value = "select COUNT(1) num,ifnull(SUM(validAmount),0) validAmount,ifnull(SUM(winLoss),0) winLoss from (select SUM(p.valid_amount) "
        + "validAmount,SUM(p.win_loss) winLoss from proxy_game_record_report p where p.first_proxy= ?3 and p.order_times "
        + "BETWEEN ?1 and ?2 GROUP BY p.user_id) a",nativeQuery = true)
    Map<String, Object> findSumBetAndWinLossByFirst(String startTime,String endTime,Long firstProxy);

    @Query(value = "select COUNT(1) num,ifnull(SUM(validAmount),0) validAmount,ifnull(SUM(winLoss),0) winLoss from (select SUM(p.valid_amount) "
        + "validAmount,SUM(p.win_loss) winLoss from proxy_game_record_report p where p.second_proxy= ?3 and p.order_times "
        + "BETWEEN ?1 and ?2 GROUP BY p.user_id) a",nativeQuery = true)
    Map<String, Object> findSumBetAndWinLossBySecond(String startTime,String endTime,Long secondProxy);

    @Query(value = "select COUNT(1) num,ifnull(SUM(validAmount),0) validAmount,ifnull(SUM(winLoss),0) winLoss from (select SUM(p.valid_amount) "
        + "validAmount,SUM(p.win_loss) winLoss from proxy_game_record_report p where p.third_proxy= ?3 and p.order_times "
        + "BETWEEN ?1 and ?2 GROUP BY p.user_id) a",nativeQuery = true)
    Map<String, Object> findSumBetAndWinLossByThird(String startTime,String endTime,Long thirdProxy);

    @Query(value = "select COUNT(1) num,ifnull(SUM(validAmount),0) validAmount,ifnull(SUM(winLoss),0) winLoss from (select SUM(p.valid_amount) "
        + "validAmount,SUM(p.win_loss) winLoss from proxy_game_record_report p where p.first_proxy= ?1 GROUP BY p.user_id) a",nativeQuery = true)
    Map<String, Object> findSumBetAndWinLossByFirst(Long firstProxy);

    @Query(value = "select COUNT(1) num,ifnull(SUM(validAmount),0) validAmount,ifnull(SUM(winLoss),0) winLoss from (select SUM(p.valid_amount) "
        + "validAmount,SUM(p.win_loss) winLoss from proxy_game_record_report p where p.second_proxy= ?1  GROUP BY p.user_id) a",nativeQuery = true)
    Map<String, Object> findSumBetAndWinLossBySecond(Long secondProxy);

    @Query(value = "select COUNT(1) num,ifnull(SUM(validAmount),0) validAmount,ifnull(SUM(winLoss),0) winLoss from (select SUM(p.valid_amount) "
        + "validAmount,SUM(p.win_loss) winLoss from proxy_game_record_report p where p.third_proxy= ?1  GROUP BY p.user_id) a",nativeQuery = true)
    Map<String, Object> findSumBetAndWinLossByThird(Long thirdProxy);

    @Query(value = "select COUNT(1) num,ifnull(SUM(validAmount),0) validAmount,ifnull(SUM(winLoss),0) winLoss from (select SUM(p.valid_amount) "
        + "validAmount,SUM(p.win_loss) winLoss from proxy_game_record_report p where p.order_times "
        + "BETWEEN ?1 and ?2 GROUP BY p.user_id) a",nativeQuery = true)
    Map<String, Object> findSumBetAndWinLoss(String startTime,String endTime);

    @Query(value = "select COUNT(1) num,ifnull(SUM(validAmount),0) validAmount,ifnull(SUM(winLoss),0) winLoss from (select SUM(p.valid_amount) "
        + "validAmount,SUM(p.win_loss) winLoss from proxy_game_record_report p GROUP BY p.user_id) a",nativeQuery = true)
    Map<String, Object> findSumBetAndWinLoss();

    @Query(value = "select p.order_times as orderTimes ,SUM(p.valid_amount) validAmount,SUM(p.win_loss) winLoss from proxy_game_record_report p "
        + "where p.order_times BETWEEN ?1 and ?2 GROUP BY p.order_times",nativeQuery = true)
    List<Map<String, Object>> findBetAndWinLoss(String startTime,String endTime);

    @Query(value = "select ifnull(SUM(betting_number),0) from proxy_game_record_report u where  u.order_times >= ?1 and u.order_times <= ?2 ;",nativeQuery = true)
    Integer findBetNumber(String startTime,String endTime);

    @Modifying
    @Query(value = "DELETE from proxy_game_record_report where order_times = ?1 ;",nativeQuery = true)
    void deleteByOrderTimes(String orderTimes);

    @Modifying
    @Query(value = "DELETE from proxy_game_record_report where order_times >= ?1 and order_times <= ?2 ;",nativeQuery = true)
    void deleteByOrderTimes(String startTime,String endTime);

    @Query(value = "SELECT a.user_id user_id,a.first_proxy first_proxy,a.second_proxy second_proxy,a.third_proxy third_proxy,"
        + "a.num num,a.bet_amount bet_amount,a.validbet validbet,a.win_loss win_loss FROM (SELECT u.id user_id,u.first_proxy "
        + "first_proxy,u.second_proxy second_proxy,u.third_proxy third_proxy, " +
            "ifnull( main_t.num, 0 )+ ifnull( gre_t.num, 0 )+ ifnull( goldenf_t.num, 0 )+ ifnull( goldenf_sb.num, 0 )+ ifnull( grobdj_t.num, 0 )+ ifnull( grobty_t.num, 0 )+ ifnull( dmc.num, 0 )+ ifnull( dg.num, 0 ) num, " +
            "ifnull( main_t.bet_amount, 0 )+ ifnull( gre_t.bet_amount, 0 )+ ifnull( goldenf_t.bet_amount, 0 )+ ifnull( goldenf_sb.bet_amount, 0 )+ ifnull( grobdj_t.bet_amount, 0 )+ ifnull( grobty_t.bet_amount, 0 )+ ifnull( dmc.bet_amount, 0 )+ ifnull( dg.bet_amount, 0 ) bet_amount, " +
            "ifnull( main_t.validbet, 0 )+ ifnull( gre_t.validbet, 0 )+ ifnull( goldenf_t.bet_amount, 0 )+ ifnull( goldenf_sb.bet_amount, 0 )+ ifnull( grobdj_t.bet_amount, 0 ) + ifnull( grobty_t.bet_amount, 0 )+ ifnull( dmc.validbet, 0 )+ ifnull( dg.validbet, 0 ) validbet, " +
            "ifnull( main_t.win_loss, 0 )+ ifnull( gre_t.win_loss, 0 )+ ifnull( goldenf_t.win_loss, 0 )+ ifnull( goldenf_sb.win_loss, 0 ) + ifnull( grobdj_t.win_loss, 0 )+ ifnull( grobty_t.win_loss, 0 )+ ifnull( dmc.win_loss, 0 )+ ifnull( dg.win_loss, 0 ) win_loss " +
            "FROM USER u LEFT JOIN (SELECT user_id,count( 1 ) num,"
        + "sum( bet ) bet_amount,sum( validbet ) validbet,sum( win_loss ) win_loss FROM game_record gr WHERE  bet_time >= ?1 AND bet_time <= ?2 "
        + "GROUP BY user_id ) main_t ON u.id = main_t.user_id LEFT JOIN (SELECT user_id,count( 1 ) num,sum( bet_amount ) bet_amount,sum( turnover ) validbet,"
        + "sum( real_win_amount-real_bet_amount ) win_loss FROM game_record_ae gre WHERE gre.tx_status = 1 and gre.tx_time >= ?1 AND gre.tx_time <= ?2 GROUP BY user_id ) gre_t "
        + "ON u.id = gre_t.user_id LEFT JOIN (SELECT user_id,count( 1 ) num,sum( bet_amount ) bet_amount,sum( win_amount - bet_amount )"
        + " win_loss FROM game_record_goldenf grg WHERE create_at_str >= ?1 AND create_at_str <= ?2 And vendor_code in ('PG','CQ9') GROUP BY user_id ) goldenf_t "
        + "ON u.id = goldenf_t.user_id LEFT JOIN (SELECT off.user_id user_id,off.vendor_code vendor_code,count( DISTINCT sk.bet_id ) num,ifnull( SUM( sk.bet_amount ), 0 ) bet_amount,"
        + "ifnull( SUM( sk.bet_amount ), 0 ) validbet,ifnull(sum(off.win_amount), 0 )-ifnull(sum( sk.bet_amount ), 0 )+ifnull(sum(t3.win_amount), 0 ) win_loss FROM "
        + "(SELECT user_id user_id,vendor_code vendor_code,bet_id bet_id,SUM( win_amount ) win_amount FROM game_record_goldenf t1 WHERE t1.vendor_code = 'SABASPORT' "
        + "AND t1.trans_type = 'Payoff' AND t1.create_at_str BETWEEN ?1 AND ?2 GROUP BY t1.bet_id) off LEFT JOIN ( SELECT bet_amount, "
        + "bet_id FROM game_record_goldenf WHERE vendor_code = 'SABASPORT' AND trans_type = 'Stake' ) sk ON off.bet_id = sk.bet_id LEFT JOIN ( SELECT SUM(win_amount) win_amount,"
        + " bet_id FROM game_record_goldenf WHERE vendor_code = 'SABASPORT' AND trans_type = 'cancelPayoff' GROUP BY bet_id) t3 ON off.bet_id = t3.bet_id GROUP BY off.user_id) "
        + "goldenf_sb ON u.id = goldenf_sb.user_id " +
            " LEFT JOIN (SELECT user_id,count( 1 ) num,sum( bet_points ) bet_amount,sum( available_bet ) validbet,sum( win_money - bet_points ) win_loss FROM game_record_dg  WHERE is_revocation = '1' AND cal_time >= ?1  AND cal_time <= ?2  GROUP BY user_id ) dg ON u.id = dg.user_id " +
            " LEFT JOIN (SELECT user_id,count( 1 ) num,sum( bet_money ) bet_amount,sum( bet_money ) validbet,sum( win_money - bet_money ) win_loss FROM game_record_dmc  WHERE bet_time >= ?1 AND bet_time <= ?2 GROUP BY user_id ) dmc ON u.id = dmc.user_id " +
            " LEFT JOIN (SELECT user_id,count( 1 ) num,sum( bet_amount ) bet_amount,sum( win_amount - bet_amount ) win_loss "
        + "FROM game_record_obdj grobdj  WHERE bet_status IN ( 5, 6, 8, 9, 10 ) AND set_str_time >= ?1 AND set_str_time <= ?2 GROUP BY user_id ) grobdj_t "
        + "ON u.id = grobdj_t.user_id LEFT JOIN (SELECT user_id,count( 1 ) num,sum( order_amount ) bet_amount,sum( profit_amount ) win_loss FROM game_record_obty grobty "
        + "WHERE settle_str_time >= ?1 AND settle_str_time <= ?2 GROUP BY user_id ) grobty_t ON u.id = grobty_t.user_id ) a WHERE num > 0;",nativeQuery = true)
    List<Map<String, Object>> findTotal(String startTime,String endTime);

    @Query(value = "select ifnull( g.first_proxy, 0 ) first_proxy,ifnull( g.second_proxy, 0 ) second_proxy,ifnull( g.third_proxy, 0 ) third_proxy,"
        + "user_id user_id,count(1) num,sum(bet_amount) bet_amount,sum(real_bet_amount) validbet,sum(real_win_amount-real_bet_amount) win_loss "
        + "from game_record_ae grg where g.tx_status = 1 and tx_time >= ?1 and tx_time <= ?2 group by user_id ;",nativeQuery = true)
    List<Map<String, Object>> findTotalAe(String startTime,String endTime);

    @Query(value = "SELECT user_id user_id,count(1) num,ifnull( sum( bet_money ), 0 ) bet_amount,ifnull( sum( real_money ), 0 ) validbet,"
        + "ifnull( sum( win_money ), 0 )- ifnull( sum( real_money ), 0 ) win_loss FROM rpt_bet_info_detail grv WHERE settle_time BETWEEN ?1 AND ?2 "
        + "group by user_id ;",nativeQuery = true)
    List<Map<String, Object>> findVnc(String startTime,String endTime);
}
