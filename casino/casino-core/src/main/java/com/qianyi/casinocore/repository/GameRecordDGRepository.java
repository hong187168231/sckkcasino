package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordDG;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface GameRecordDGRepository extends JpaRepository<GameRecordDG, Long>, JpaSpecificationExecutor<GameRecordDG> {

    GameRecordDG findByBetOrderNo(String betOrderNo);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordDG u set u.codeNumStatus=?2 where u.id=?1")
    void updateCodeNumStatus(Long id, Integer codeNumStatus);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordDG u set u.extractStatus=?2 where u.id=?1")
    void updateExtractStatus(Long id, Integer status);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordDG u set u.washCodeStatus=?2 where u.id=?1")
    void updateWashCodeStatus(Long id, Integer washCodeStatus);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordDG u set u.levelWaterStatus=?2 where u.id=?1")
    void updateLevelWaterStatus(Long id, Integer levelWaterStatus);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordDG u set u.rebateStatus=?2 where u.id=?1")
    void updateRebateStatus(Long id, Integer rebateStatus);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordDG u set u.gameRecordStatus=?2 where u.id=?1")
    void updateGameRecordStatus(Long id, Integer gameRecordStatus);

    @Query(value = "SELECT IFNULL(sum(available_bet),0) turnover,count(1) betCount,IFNULL(sum(bet_points),0) betAmount,IFNULL(sum(win_Amount),0) winAmount \n" +
            "from game_record_dg where is_revocation = 1 and bet_time BETWEEN ?2 and ?3",nativeQuery = true)
    Map<String,Object> findSumByPlatformAndTime(String startTime, String endTime);

    @Query(value = "select MAX(g.id) maxId,ifnull(SUM(d.user_amount), 0 ) as user_amount,ifnull(SUM(d.surplus_amount), 0 ) as surplus_amount,LEFT(g.bet_time,?2) set_time,ifnull(g.first_proxy,0) first_proxy,\n"
            + "        ifnull(g.second_proxy,0) second_proxy,ifnull(g.third_proxy,0) third_proxy,\n"
            + "        COUNT(1) num,SUM(g.bet_points) bet,SUM(g.available_bet) validbet,ifnull(sum(g.win_money),0)-ifnull(sum(g.real_money),0) win_loss,\n"
            + "         ifnull(SUM(w.amount),0) amount from game_record_dg g left join  \n"
            + "        wash_code_change w  on  w.game_record_id = g.id and w.platform = 'DG'\n"
            + "        LEFT JOIN  rebate_detail d on d.game_record_id=g.id  and d.platform = 'DG'\n"
            + "         where g.id > ?1 and g.is_revocation = 1  \n" + "   GROUP BY g.third_proxy,LEFT(g.bet_time,?2) ",nativeQuery = true)
    List<Map<String,Object>> queryGameRecords(Long id,Integer num);
}
