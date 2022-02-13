package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GameRecordRepository extends JpaRepository<GameRecord, Long>, JpaSpecificationExecutor<GameRecord> {

    @Query(value = "select max(first_proxy) first_proxy ,max(second_proxy) second_proxy ,third_proxy third_proxy, user_id as  userId,count(distinct user_id) player_num ,max(bet_time) bet_time, sum(validbet) validbet,1 as gameType \n" +
            "from game_record gr\n" +
            "where\n" +
            "bet_time between ?1 and ?2\n" +
            "and third_proxy is not null \n" +
            "group by third_proxy,user_id ",nativeQuery = true)
    List<Map<String,Object>> getStatisticsResult(String startTime, String endTime);

    @Modifying
    @Query("update GameRecord u set u.codeNumStatus=?2 where u.id=?1")
    void updateCodeNumStatus(Long id, Integer codeNumStatus);

    @Modifying
    @Query("update GameRecord u set u.washCodeStatus=?2 where u.id=?1")
    void updateWashCodeStatus(Long id, Integer washCodeStatus);

    @Modifying
    @Query("update GameRecord u set u.shareProfitStatus= u.shareProfitStatus+?2 where u.id=?1")
    void updateProfitStatus(Long id, Integer washCodeStatus);

    List<GameRecord> findByCreateByAndIdGreaterThanEqualOrderByIdAsc(String createBy,Long id);

    @Query(value = "select count(1) as amount  from game_record rg where rg.id<=?1 and rg.user_id=?2",nativeQuery = true)
    int  countByIdLessThanEqualAndUserId(Long gameId,Long userId);

    GameRecord findByBetId(String betId);

    @Query(value = "select ifnull(g.first_proxy,0) first_proxy," +
            "ifnull(g.second_proxy,0) second_proxy,ifnull(g.third_proxy,0) third_proxy," +
            "g.gid gid,COUNT(1) num,SUM(g.bet) bet,SUM(g.validbet) validbet,SUM(g.win_loss) win_loss," +
            " ifnull(SUM(w.amount),0) amount from game_record g left join  " +
            "wash_code_change w  on  w.game_record_id = g.id where g.settime >= ?1 " +
            "and g.settime <= ?2 GROUP BY g.third_proxy,g.gid  ",nativeQuery = true)
    List<Map<String,Object>> queryGameRecords(String startTime,String endTime);

    @Query(value = "select LEFT(g.settime,?3) set_time,ifnull(g.first_proxy,0) first_proxy," +
        "ifnull(g.second_proxy,0) second_proxy,ifnull(g.third_proxy,0) third_proxy," +
        "g.gid gid,COUNT(1) num,SUM(g.bet) bet,SUM(g.validbet) validbet,SUM(g.win_loss) win_loss," +
        " ifnull(SUM(w.amount),0) amount from game_record g left join  " +
        "wash_code_change w  on  w.game_record_id = g.id where g.create_time >= ?1 " +
        "and g.create_time <= ?2 GROUP BY g.third_proxy,g.gid,LEFT(g.settime,?3)  ",nativeQuery = true)
    List<Map<String,Object>> queryGameRecords(String startTime,String endTime,Integer num);

    @Query(value = "select MAX(g.id) maxId,LEFT(g.settime,?2) set_time,ifnull(g.first_proxy,0) first_proxy," +
        "ifnull(g.second_proxy,0) second_proxy,ifnull(g.third_proxy,0) third_proxy," +
        "g.gid gid,COUNT(1) num,SUM(g.bet) bet,SUM(g.validbet) validbet,SUM(g.win_loss) win_loss," +
        " ifnull(SUM(w.amount),0) amount from game_record g left join  " +
        "wash_code_change w  on  w.game_record_id = g.id and w.platform = 'wm' where g.id > ?1 " +
        " GROUP BY g.third_proxy , LEFT(g.settime,?2)  ",nativeQuery = true)
    List<Map<String,Object>> queryGameRecords(Long id,Integer num);

    @Query(value = "select g.user_id userId,SUM(g.validbet) validbet from game_record g where g.settime BETWEEN ?1 and ?2 GROUP BY g.user_id;",nativeQuery = true)
    List<Map<String, Object>> findGameRecords(String startTime,String endTime);

    @Query(value = "select SUM(g.validbet) validbet from game_record g where g.user_id = ?1 and g.settime BETWEEN ?2 and ?3 ",nativeQuery = true)
    BigDecimal findGameRecords(Long userId,String startTime,String endTime);

    @Query(value = "select g.user_id  from game_record g where g.settime BETWEEN ?1 and ?2 GROUP BY g.user_id;",nativeQuery = true)
    Set<Long> findGroupByUser(String startTime,String endTime);

    @Query(value = "select SUM(g.validbet) validbet,SUM(g.win_loss) winLoss from game_record g where g.settime BETWEEN ?1 and ?2 ",nativeQuery = true)
    Map<String, Object> findSumBetAndWinLoss(String startTime,String endTime);

    @Query(value = "select g.user_id  from game_record g where g.settime BETWEEN ?1 and ?2 and g.first_proxy = ?3 GROUP BY g.user_id;",nativeQuery = true)
    Set<Long> findGroupByFirst(String startTime,String endTime,Long firstProxy);

    @Query(value = "select ifnull(SUM(g.validbet),0) validbet,ifnull(SUM(g.win_loss),0) winLoss from game_record g where g.settime BETWEEN ?1 and ?2 and g.first_proxy = ?3",nativeQuery = true)
    Map<String, Object> findSumBetAndWinLossByFirst(String startTime,String endTime,Long firstProxy);

    @Query(value = "select g.user_id  from game_record g where g.settime BETWEEN ?1 and ?2 and g.second_proxy = ?3 GROUP BY g.user_id;",nativeQuery = true)
    Set<Long> findGroupBySecond(String startTime,String endTime,Long secondProxy);

    @Query(value = "select ifnull(SUM(g.validbet),0) validbet,ifnull(SUM(g.win_loss),0) winLoss  from game_record g where g.settime BETWEEN ?1 and ?2 and g.second_proxy = ?3",nativeQuery = true)
    Map<String, Object> findSumBetAndWinLossBySecond(String startTime,String endTime,Long secondProxy);

    @Query(value = "select g.user_id  from game_record g where g.settime BETWEEN ?1 and ?2 and g.third_proxy = ?3 GROUP BY g.user_id;",nativeQuery = true)
    Set<Long> findGroupByThird(String startTime,String endTime,Long thirdProxy);

    @Query(value = "select ifnull(SUM(g.validbet),0) validbet,ifnull(SUM(g.win_loss),0) winLoss  from game_record g where g.settime BETWEEN ?1 and ?2 and g.third_proxy = ?3",nativeQuery = true)
    Map<String, Object> findSumBetAndWinLossByThird(String startTime,String endTime,Long thirdProxy);
}
