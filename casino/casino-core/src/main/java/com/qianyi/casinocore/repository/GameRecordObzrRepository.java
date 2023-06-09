package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordDG;
import com.qianyi.casinocore.model.GameRecordObzr;
import com.qianyi.casinocore.model.GameRecordObzr;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface GameRecordObzrRepository extends JpaRepository<GameRecordObzr, Long>, JpaSpecificationExecutor<GameRecordObzr> {


    GameRecordObzr findByOrderNo(String orderNo);

@Query(value = "select first_proxy first_proxy ,second_proxy second_proxy ,third_proxy third_proxy, user_id as  userId,count(distinct user_id) player_num ,max(bet_str_time) bet_time, sum(valid_bet_amount) validbet,1 as gameType \n" +
            "from game_record_obzr gr\n" +
            "where\n" +
            "bet_time between ?1 and ?2\n" +
            "and third_proxy is not null \n" +
            "group by third_proxy,user_id,second_proxy,first_proxy ",nativeQuery = true)
    List<Map<String, Object>> getStatisticsResult(String startTime, String endTime);

    @Query(value = "select count(1) as amount  from game_record_obzr rg where rg.bet_time <=?1 and rg.user_id=?2",nativeQuery = true)
    int countByIdLessThanEqualAndUserId(Date createTime, Long userId);


    @Modifying(clearAutomatically = true)
    @Query("update GameRecordObzr u set u.codeNumStatus=?2 where u.id=?1")
    void updateCodeNumStatus(Long id, Integer codeNumStatus);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordObzr u set u.washCodeStatus=?2 where u.id=?1")
    void updateWashCodeStatus(Long id, Integer washCodeStatus);


    @Modifying(clearAutomatically = true)
    @Query("update GameRecordObzr u set u.levelWaterStatus=?2 where u.id=?1")
    void updateLevelWaterStatus(Long id, Integer levelWaterStatus);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordObzr u set u.rebateStatus=?2 where u.id=?1")
    void updateRebateStatus(Long id, Integer rebateStatus);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordObzr u set u.extractStatus=?2 where u.id=?1")
    void updateExtractStatus(Long id, Integer status);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordObzr u set u.shareProfitStatus= u.shareProfitStatus+?2 where u.id=?1")
    void updateProfitStatus(Long id, Integer shareProfitStatus);

    @Modifying(clearAutomatically = true)
    @Query("update GameRecordObzr u set u.gameRecordStatus=?2 where u.id=?1")
    void updateGameRecordStatus(Long id, Integer gameRecordStatus);

    @Query(value = "select MAX(g.id) maxId,ifnull(SUM(d.user_amount), 0 ) as user_amount,ifnull(SUM(d.surplus_amount), 0 ) as surplus_amount,LEFT(g.bet_time,?2) set_time,ifnull(g.first_proxy,0) first_proxy,\n"
        + "        ifnull(g.second_proxy,0) second_proxy,ifnull(g.third_proxy,0) third_proxy,\n"
        + "        COUNT(1) num,SUM(g.bet_amount) bet,SUM(g.valid_bet_amount) validbet,SUM(g.net_amount) win_loss,\n"
        + "         ifnull(SUM(w.amount),0) amount from game_record_obzr g left join  \n"
        + "        wash_code_change w  on  w.game_record_id = g.id and w.platform = 'OBZR'\n"
        + "        LEFT JOIN  rebate_detail d on d.game_record_id=g.id  and d.platform = 'OBZR'\n"
        + "         where g.id > ?1 \n" + "   GROUP BY g.third_proxy,LEFT(g.bet_time,?2) ",nativeQuery = true)
    List<Map<String,Object>> queryGameRecords(Long id,Integer num);
}