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

    @Query(value = "SELECT\n" +
            "\ta.user_id user_id,\n" +
            "\ta.first_proxy first_proxy,\n" +
            "\ta.second_proxy second_proxy,\n" +
            "\ta.third_proxy third_proxy,\n" +
            "\ta.num num,\n" +
            "\ta.bet_amount bet_amount,\n" +
            "\ta.validbet validbet,\n" +
            "\ta.win_loss win_loss \n" +
            "FROM\n" +
            "\t(\n" +
            "\tSELECT\n" +
            "\t\tu.id user_id,\n" +
            "\t\tu.first_proxy first_proxy,\n" +
            "\t\tu.second_proxy second_proxy,\n" +
            "\t\tu.third_proxy third_proxy,\n" +
            "\t\tifnull( main_t.num, 0 )+ ifnull( gre_t.num, 0 )+ ifnull( goldenf_t.num, 0 )+ ifnull( goldenf_sb.num, 0 )+ ifnull( grobdj_t.num, 0 )+ ifnull( grobty_t.num, 0 )+ ifnull( dmc.num, 0 )+ ifnull( dg.num, 0 ) num,\n" +
            "\t\tifnull( main_t.bet_amount, 0 )+ ifnull( gre_t.bet_amount, 0 )+ ifnull( goldenf_t.bet_amount, 0 )+ ifnull( goldenf_sb.bet_amount, 0 )+ ifnull( grobdj_t.bet_amount, 0 )+ ifnull( grobty_t.bet_amount, 0 )+ ifnull( dmc.bet_amount, 0 )+ ifnull( dg.bet_amount, 0 ) bet_amount,\n" +
            "\t\tifnull( main_t.validbet, 0 )+ ifnull( gre_t.validbet, 0 )+ ifnull( goldenf_t.bet_amount, 0 )+ ifnull( goldenf_sb.bet_amount, 0 )+ ifnull( grobdj_t.bet_amount, 0 ) + ifnull( grobty_t.bet_amount, 0 )+ ifnull( dmc.validbet, 0 )+ ifnull( dg.validbet, 0 ) validbet,\n" +
            "\t\tifnull( main_t.win_loss, 0 )+ ifnull( gre_t.win_loss, 0 )+ ifnull( goldenf_t.win_loss, 0 )+ ifnull( goldenf_sb.win_loss, 0 ) + ifnull( grobdj_t.win_loss, 0 )+ ifnull( grobty_t.win_loss, 0 )+ ifnull( dmc.win_loss, 0 )+ ifnull( dg.win_loss, 0 ) win_loss \n" +
            "\tFROM\n" +
            "\t\tUSER u\n" +
            "\t\tLEFT JOIN (\n" +
            "\t\tSELECT\n" +
            "\t\t\tuser_id,\n" +
            "\t\t\tcount( 1 ) num,\n" +
            "\t\t\tsum( bet ) bet_amount,\n" +
            "\t\t\tsum( validbet ) validbet,\n" +
            "\t\t\tsum( win_loss ) win_loss \n" +
            "\t\tFROM\n" +
            "\t\t\tgame_record gr \n" +
            "\t\tWHERE\n" +
            "\t\t\tbet_time >= ?1 \n" +
            "\t\t\tAND bet_time <= ?2 \n" +
            "\t\tGROUP BY\n" +
            "\t\t\tuser_id \n" +
            "\t\t) main_t ON u.id = main_t.user_id\n" +
            "\t\tLEFT JOIN (\n" +
            "\t\tSELECT\n" +
            "\t\t\tuser_id,\n" +
            "\t\t\tcount( 1 ) num,\n" +
            "\t\t\tsum( bet_amount ) bet_amount,\n" +
            "\t\t\tsum( turnover ) validbet,\n" +
            "\t\t\tsum( real_win_amount - real_bet_amount ) win_loss \n" +
            "\t\tFROM\n" +
            "\t\t\tgame_record_ae gre \n" +
            "\t\tWHERE\n" +
            "\t\t\tgre.tx_status = 1 \n" +
            "\t\t\tAND gre.tx_time >= ?1 \n" +
            "\t\t\tAND gre.tx_time <= ?2 \n" +
            "\t\tGROUP BY\n" +
            "\t\t\tuser_id \n" +
            "\t\t) gre_t ON u.id = gre_t.user_id\n" +
            "\t\tLEFT JOIN (\n" +
            "\t\tSELECT\n" +
            "\t\t\tuser_id,\n" +
            "\t\t\tcount( 1 ) num,\n" +
            "\t\t\tsum( bet_amount ) bet_amount,\n" +
            "\t\t\tsum( win_amount - bet_amount ) win_loss \n" +
            "\t\tFROM\n" +
            "\t\t\tgame_record_goldenf grg \n" +
            "\t\tWHERE\n" +
            "\t\t\tcreate_at_str >= ?1 \n" +
            "\t\t\tAND create_at_str <= ?2 \n" +
            "\t\t\tAND vendor_code IN ( 'PG', 'CQ9' ) \n" +
            "\t\tGROUP BY\n" +
            "\t\t\tuser_id \n" +
            "\t\t) goldenf_t ON u.id = goldenf_t.user_id\n" +
            "\t\tLEFT JOIN (\n" +
            "\t\tSELECT\n" +
            "\t\t\toff.user_id user_id,\n" +
            "\t\t\toff.vendor_code vendor_code,\n" +
            "\t\t\tcount( DISTINCT sk.bet_id ) num,\n" +
            "\t\t\tifnull( SUM( sk.bet_amount ), 0 ) bet_amount,\n" +
            "\t\t\tifnull( SUM( sk.bet_amount ), 0 ) validbet,\n" +
            "\t\t\tifnull( sum( off.win_amount ), 0 )- ifnull( sum( sk.bet_amount ), 0 )+ ifnull( sum( t3.win_amount ), 0 ) win_loss \n" +
            "\t\tFROM\n" +
            "\t\t\t(\n" +
            "\t\t\tSELECT\n" +
            "\t\t\t\tuser_id user_id,\n" +
            "\t\t\t\tvendor_code vendor_code,\n" +
            "\t\t\t\tbet_id bet_id,\n" +
            "\t\t\t\tSUM( win_amount ) win_amount \n" +
            "\t\t\tFROM\n" +
            "\t\t\t\tgame_record_goldenf t1 \n" +
            "\t\t\tWHERE\n" +
            "\t\t\t\tt1.vendor_code = 'SABASPORT' \n" +
            "\t\t\t\tAND t1.trans_type = 'Payoff' \n" +
            "\t\t\t\tAND t1.create_at_str BETWEEN ?1 \n" +
            "\t\t\t\tAND ?2 \n" +
            "\t\t\tGROUP BY\n" +
            "\t\t\t\tt1.bet_id \n" +
            "\t\t\t) off\n" +
            "\t\t\tLEFT JOIN ( SELECT bet_amount, bet_id FROM game_record_goldenf WHERE vendor_code = 'SABASPORT' AND trans_type = 'Stake' ) sk ON off.bet_id = sk.bet_id\n" +
            "\t\t\tLEFT JOIN ( SELECT SUM( win_amount ) win_amount, bet_id FROM game_record_goldenf WHERE vendor_code = 'SABASPORT' AND trans_type = 'cancelPayoff' GROUP BY bet_id ) t3 ON off.bet_id = t3.bet_id \n" +
            "\t\tGROUP BY\n" +
            "\t\t\toff.user_id \n" +
            "\t\t) goldenf_sb ON u.id = goldenf_sb.user_id\n" +
            "\t\tLEFT JOIN (\n" +
            "\t\tSELECT\n" +
            "\t\t\tuser_id,\n" +
            "\t\t\tcount( 1 ) num,\n" +
            "\t\t\tsum( bet_amount ) bet_amount,\n" +
            "\t\t\tsum( win_amount - bet_amount ) win_loss \n" +
            "\t\tFROM\n" +
            "\t\t\tgame_record_obdj grobdj \n" +
            "\t\tWHERE\n" +
            "\t\t\tbet_status IN ( 5, 6, 8, 9, 10 ) \n" +
            "\t\t\tAND set_str_time >= ?1 \n" +
            "\t\t\tAND set_str_time <= ?2 \n" +
            "\t\tGROUP BY\n" +
            "\t\t\tuser_id \n" +
            "\t\t) grobdj_t ON u.id = grobdj_t.user_id\n" +
            "\t\tLEFT JOIN (\n" +
            "\t\tSELECT\n" +
            "\t\t\tuser_id,\n" +
            "\t\t\tcount( 1 ) num,\n" +
            "\t\t\tsum( dg.bet_points ) bet_amount,\n" +
            "\t\t\tsum( dg.available_bet ) validbet,\n" +
            "\t\t\tsum( dg.win_money - dg.bet_points ) win_loss \n" +
            "\t\tFROM\n" +
            "\t\t\tgame_record_dg dg \n" +
            "\t\tWHERE\n" +
            "\t\t\tdg.is_revocation = '1' \n" +
            "\t\t\tAND dg.cal_time >= ?1 \n" +
            "\t\t\tAND dg.cal_time <= ?2 \n" +
            "\t\tGROUP BY\n" +
            "\t\t\tuser_id \n" +
            "\t\t) dg ON u.id = dg.user_id\n" +
            "\t\tLEFT JOIN (\n" +
            "\t\tSELECT\n" +
            "\t\t\tuser_id,\n" +
            "\t\t\tcount( 1 ) num,\n" +
            "\t\t\tsum( dmc.bet_money ) bet_amount,\n" +
            "\t\t\tsum( dmc.bet_money ) validbet,\n" +
            "\t\t\tsum( dmc.win_money - dmc.bet_money ) win_loss \n" +
            "\t\tFROM\n" +
            "\t\t\tgame_record_dmc dmc \n" +
            "\t\tWHERE\n" +
            "\t\t\tdmc.bet_time >= ?1\n" +
            "\t\t\tAND dmc.bet_time <= ?2 \n" +
            "\t\tGROUP BY\n" +
            "\t\t\tdmc.user_id \n" +
            "\t\t) dmc ON u.id = dmc.user_id\n" +
            "\t\tLEFT JOIN (\n" +
            "\t\tSELECT\n" +
            "\t\t\tuser_id,\n" +
            "\t\t\tcount( 1 ) num,\n" +
            "\t\t\tsum( order_amount ) bet_amount,\n" +
            "\t\t\tsum( profit_amount ) win_loss \n" +
            "\t\tFROM\n" +
            "\t\t\tgame_record_obty grobty \n" +
            "\t\tWHERE\n" +
            "\t\tsettle_str_time >= ?1 \n" +
            "\tAND settle_str_time <= ?2 GROUP BY user_id ) grobty_t ON u.id = grobty_t.user_id ) a WHERE num > 0;",nativeQuery = true)
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
