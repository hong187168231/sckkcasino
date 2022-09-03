package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordAe;
import com.qianyi.casinocore.vo.GameRecordAeSummaryVo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface GameRecordAeRepository extends JpaRepository<GameRecordAe, Long>, JpaSpecificationExecutor<GameRecordAe> {

    GameRecordAe findByPlatformAndPlatformTxId(String platform,String platformTxId);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordAe u set u.codeNumStatus=?2 where u.id=?1")
    void updateCodeNumStatus(Long id, Integer codeNumStatus);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordAe u set u.washCodeStatus=?2 where u.id=?1")
    void updateWashCodeStatus(Long id, Integer washCodeStatus);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordAe u set u.rebateStatus=?2 where u.id=?1")
    void updateRebateStatus(Long id, Integer rebateStatus);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordAe u set u.extractStatus=?2 where u.id=?1")
    void updateExtractStatus(Long id, Integer status);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordAe u set u.shareProfitStatus= u.shareProfitStatus+?2 where u.id=?1")
    void updateProfitStatus(Long id, Integer shareProfitStatus);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordAe u set u.gameRecordStatus=?2 where u.id=?1")
    void updateGameRecordStatus(Long id, Integer gameRecordStatus);

    @Query(value = "select count(1) as amount  from game_record_ae rg where rg.bet_time <=?1 and rg.user_id=?2",nativeQuery = true)
    int countByIdLessThanEqualAndUserId(Date createTime, Long userId);

    @Query(value = "select MAX(g.id) maxId,ifnull(SUM(d.user_amount), 0 ) as user_amount,ifnull(SUM(d.surplus_amount), 0 ) as surplus_amount,LEFT(g.bet_time,?2) set_time,ifnull(g.first_proxy,0) first_proxy,\n"
        + "        ifnull(g.second_proxy,0) second_proxy,ifnull(g.third_proxy,0) third_proxy,\n"
        + "        COUNT(1) num,SUM(g.bet_amount) bet,SUM(g.turnover) validbet,SUM(g.real_win_amount-g.real_bet_amount) win_loss,\n"
        + "         ifnull(SUM(w.amount),0) amount from game_record_ae g left join  \n"
        + "        wash_code_change w  on  w.game_record_id = g.id and w.platform = ?3\n"
        + "        LEFT JOIN  rebate_detail d on d.game_record_id=g.id  and d.platform = ?3\n"
        + "         where g.id > ?1 and g.platform = ?3 and g.tx_status = 1  \n" + "   GROUP BY g.third_proxy,LEFT(g.bet_time,?2) ",nativeQuery = true)
    List<Map<String,Object>> queryGameRecords(Long id,Integer num,String platform);

    @Query(value = "select MAX(g.id) maxId,ifnull(SUM(d.user_amount), 0 ) as user_amount,ifnull(SUM(d.surplus_amount), 0 ) as surplus_amount,LEFT(g.bet_time,?2) set_time,ifnull(g.first_proxy,0) first_proxy,\n"
        + "        ifnull(g.second_proxy,0) second_proxy,ifnull(g.third_proxy,0) third_proxy,\n"
        + "        COUNT(1) num,SUM(g.bet_amount) bet,SUM(g.turnover) validbet,SUM(g.real_win_amount-g.real_bet_amount) win_loss,\n"
        + "         ifnull(SUM(w.amount),0) amount from game_record_ae g left join  \n"
        + "        wash_code_change w  on  w.game_record_id = g.id and w.platform = ?3\n"
        + "        LEFT JOIN  rebate_detail d on d.game_record_id=g.id  and d.platform = ?3\n"
        + "         where g.id > ?1 and g.tx_status = 1  \n" + "   GROUP BY g.third_proxy,LEFT(g.bet_time,?2) ",nativeQuery = true)
    List<Map<String,Object>> queryGameRecordsMerge(Long id,Integer num,String platform);

    @Query(value = "SELECT IFNULL(sum(turnover),0) turnover,count(1) betCount,IFNULL(sum(bet_amount),0) betAmount,IFNULL(sum(win_Amount),0) winAmount,\n" +
            "IFNULL(sum(real_Win_Amount),0) realWinAmount,IFNULL(sum(real_Bet_Amount),0) realBetAmount,\n" +
            "IFNULL(sum(jackpot_Bet_Amount),0) jackpotBetAmountfrom ,IFNULL(sum(jackpot_Win_Amount),0) jackpotWinAmount \n" +
            "from game_record_ae where platform=?1 and bet_time BETWEEN ?2 and ?3",nativeQuery = true)
    Map<String,String> findSumByPlatformAndTime(String platform, String startTime, String endTime);
}
