package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordVNC;
import com.qianyi.casinocore.model.RptBetInfoDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface RptBetInfoDetailRepository extends JpaRepository<RptBetInfoDetail,Long>, JpaSpecificationExecutor<RptBetInfoDetail> {

    @Modifying(clearAutomatically = true)
    @Query("update RptBetInfoDetail u set u.codeNumStatus=?2 where u.id=?1")
    void updateCodeNumStatus(Long id, Integer codeNumStatus);

    @Modifying(clearAutomatically = true)
    @Query("update RptBetInfoDetail u set u.extractStatus=?2 where u.id=?1")
    void updateExtractStatus(Long id, Integer status);

    @Modifying(clearAutomatically = true)
    @Query("update RptBetInfoDetail u set u.washCodeStatus=?2 where u.id=?1")
    void updateWashCodeStatus(Long id, Integer washCodeStatus);

    @Modifying(clearAutomatically = true)
    @Query("update RptBetInfoDetail u set u.rebateStatus=?2 where u.id=?1")
    void updateRebateStatus(Long id, Integer rebateStatus);

    @Modifying(clearAutomatically = true)
    @Query("update RptBetInfoDetail u set u.gameRecordStatus=?2 where u.id=?1")
    void updateGameRecordStatus(Long id, Integer gameRecordStatus);

    @Query(value = "SELECT IFNULL(sum(real_money),0) turnover,count(1) betCount,IFNULL(sum(bet_money),0) betAmount,IFNULL(sum(win_money),0) winAmount,\n" +
        "from rpt_bet_info_detail where platform=?1 and bet_time_str BETWEEN ?2 and ?3",nativeQuery = true)
    Map<String,Object> findSumByPlatformAndTime(String platform, String startTime, String endTime);

    @Query(value = "select MAX(g.id) maxId,ifnull(SUM(d.user_amount), 0 ) as user_amount,ifnull(SUM(d.surplus_amount), 0 ) as surplus_amount,LEFT(g.settle_time,?2) set_time,ifnull(g.first_proxy,0) first_proxy,\n"
        + "        ifnull(g.second_proxy,0) second_proxy,ifnull(g.third_proxy,0) third_proxy,\n"
        + "        COUNT(1) num,ifnull( sum( g.bet_money ), 0 ) bet,ifnull(sum(g.real_money), 0) validbet,ifnull( sum( g.win_money ), 0 )- ifnull( sum( g.real_money ), 0 ) win_loss,\n"
        + "         ifnull(SUM(w.amount),0) amount from rpt_bet_info_detail g left join "
        + "        wash_code_change w  on  w.game_record_id = g.id and w.platform = ?3"
        + "        LEFT JOIN  rebate_detail d on d.game_record_id=g.id  and d.platform = ?3"
        + "         where g.id > ?1   GROUP BY g.third_proxy,LEFT(g.settle_time,?2) ;",nativeQuery = true)
    List<Map<String,Object>> queryGameRecords(Long id,Integer num,String platform);

    RptBetInfoDetail findByMerchantCodeAndBetOrder(String merchantCode, String betDetailOrder);

    @Modifying
    @Query(value = "update rpt_bet_info_detail r set r.game_play = 0 where r.game_play is null ",nativeQuery = true)
    void updateRptBetInfoDetailGamePlay();
}
