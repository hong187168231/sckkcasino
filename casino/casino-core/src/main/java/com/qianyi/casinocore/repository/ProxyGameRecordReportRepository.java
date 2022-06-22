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
        + "first_proxy,second_proxy,third_proxy,betting_number,bet_amount) " +
        "VALUES (?1,?2,?3,?4,?5,?6,?7,?8,1,?9) ON DUPLICATE KEY UPDATE valid_amount=valid_amount + ?4,"
        + "win_loss=win_loss + ?5,betting_number = betting_number +1,bet_amount=bet_amount + ?9 ;",nativeQuery = true)
    void updateKey(Long gameRecordReportId,Long userId,String orderTimes, BigDecimal validAmount,BigDecimal winLoss,Long firstProxy,Long secondProxy,Long thirdProxy,BigDecimal betAmount);

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

    @Query(value = "SELECT a.user_id user_id,a.first_proxy first_proxy,a.second_proxy second_proxy,a.third_proxy third_proxy,a.num num,a.bet_amount bet_amount,"
        + "a.validbet validbet,a.win_loss win_loss from (select u.id user_id,u.first_proxy first_proxy,u.second_proxy second_proxy,u.third_proxy third_proxy,"
        + "ifnull(main_t.num,0)+ifnull(goldenf_t.num,0)+ifnull(grobdj_t.num,0)+ifnull(grobty_t.num,0) num,ifnull(main_t.bet_amount,0)+ifnull(goldenf_t.bet_amount,0)"
        + "+ifnull(grobdj_t.bet_amount,0)+ifnull(grobty_t.bet_amount,0) bet_amount ,ifnull(main_t.validbet,0)+ifnull(goldenf_t.bet_amount,0)"
        + "+ifnull(grobdj_t.bet_amount,0)+ifnull(grobty_t.bet_amount,0) validbet ,ifnull(main_t.win_loss,0)+ifnull(goldenf_t.win_loss,0)+ifnull(grobdj_t.win_loss,0)"
        + "+ifnull(grobty_t.win_loss,0) win_loss from user u left join (select user_id ,count(1) num,sum(bet)   bet_amount,sum(validbet) validbet,sum(win_loss) win_loss "
        + "from game_record gr where bet_time >= ?1 and bet_time <= ?2 group by user_id)main_t on u.id = main_t.user_id "
        + "left join (select user_id ,count(1) num,sum(bet_amount) bet_amount,sum(win_amount-bet_amount) win_loss from game_record_goldenf grg "
        + "where create_at_str >= ?1 and create_at_str <= ?2 group by user_id) goldenf_t on u.id = goldenf_t.user_id "
        + "left join (select user_id ,count(1) num,sum(bet_amount) bet_amount,sum(win_amount-bet_amount) win_loss from game_record_obdj grobdj "
        + "where bet_status in (5,6,8,9,10) and set_str_time >= ?1 and set_str_time <= ?2 group by user_id) grobdj_t "
        + "on u.id = grobdj_t.user_id left join (select user_id,count(1) num,sum(order_amount) bet_amount,sum(profit_amount) win_loss from game_record_obty grobty "
        + "where settle_str_time >= ?1 and settle_str_time <= ?2 group by user_id) grobty_t on u.id = grobty_t.user_id) a "
        + "where num > 0; ;",nativeQuery = true)
    List<Map<String, Object>> findTotal(String startTime,String endTime);
}
