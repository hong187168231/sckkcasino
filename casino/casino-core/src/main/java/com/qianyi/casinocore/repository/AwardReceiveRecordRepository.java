package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.AwardReceiveRecord;
import com.qianyi.casinocore.model.UserLevelRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AwardReceiveRecordRepository extends JpaRepository<AwardReceiveRecord, Long>, JpaSpecificationExecutor<AwardReceiveRecord> {


    @Query(value = "select count(1) from award_receive_record arr where " + "  arr.user_id =?1   and arr.award_type !=2 and arr.create_time BETWEEN ?2 and ?3  ", nativeQuery = true)
    int countAwardReceiveByTime(Long userId, String startTime, String endTime);

    @Query(value = "select * from award_receive_record arr where " + "  arr.user_id =?1   and arr.award_type !=2 and arr.create_time BETWEEN ?2 and ?3 order by create_time desc limit 1  ", nativeQuery = true)
    AwardReceiveRecord selectAwardReceiveByTime(Long userId, String startTime, String endTime);

    @Query(value = "select sum(amount) from award_receive_record arr where    arr.create_time BETWEEN ?1 and ?2  ", nativeQuery = true)
    BigDecimal totalAwardByTime(String startTime, String endTime);

    @Modifying
    @Query(value = "update AwardReceiveRecord ar set ar.receiveStatus= 1 where ar.userId=?1 and ar.awardType =2")
    void modifyIsReceive(Long userId);

    @Query(value = "select  ifnull(sum(amount),0) from award_receive_record  arr where arr.award_type = 2 and  arr.receive_time BETWEEN ?1 and ?2  ", nativeQuery = true)
    BigDecimal queryBonusAmount(String startTime, String endTime);

    @Query(value = "select  ifnull(sum(amount),0) from award_receive_record  arr where arr.award_type = 1  and  arr.create_time BETWEEN ?1 and ?2  ", nativeQuery = true)
    BigDecimal queryBonusAmount2(String startTime, String endTime);

    @Query(value = " select  count(1) from award_receive_record  arr where arr.award_type =2 " +
            " and  arr.user_id = ?1  and  arr.level =?2 ", nativeQuery = true)
    int  countRiseAwardNum(Long userId, Integer level);

    @Query(value = " select  * from award_receive_record  arr where arr.award_type =2 and   arr.receive_status = 0 " +
            " and  arr.user_id = ?1  and  arr.level =?2 ", nativeQuery = true)
    AwardReceiveRecord  selectNotReceiveRiseAward(Long userId, Integer level);

    @Query(value = " select  count(1) from award_receive_record  arr where arr.award_type =2 and   arr.receive_status = 0 " +
            " and  arr.user_id = ?1  and  arr.level =?2 ", nativeQuery = true)
    int  countNotReceiveRiseAwardNum(Long userId, Integer level);

    @Query(value = " select  count(1) from award_receive_record  arr where arr.award_type =2 and   arr.receive_status = 0 " +
            " and  arr.user_id = ?1 ", nativeQuery = true)
    int  countNotReceiveRiseAwardAll(Long userId);

    @Query(value = "select a.user_id from award_receive_record a where a.third_proxy is null GROUP BY a.user_id", nativeQuery = true)
    Set<Long> findUserIds();

    @Modifying
    @Query(value = "update award_receive_record a set a.first_proxy= ?2,a.second_proxy= ?3,a.third_proxy= ?4 where a.user_id = ?1 ",nativeQuery = true)
    void updateProxyAffiliation(Long userId,Long firstProxy,Long secondProxy,Long thirdProxy);

    @Modifying
    @Query(value = "update award_receive_record a set a.receive_time = a.create_time where a.receive_time is null ",nativeQuery = true)
    void updateReceiveTime();

    @Query(value = "SELECT IFNULL( SUM( amount ), 0 ) AS amount,IFNULL(first_proxy, 0 ) first_proxy,IFNULL(second_proxy, 0 ) second_proxy,"
        + "IFNULL( third_proxy, 0) third_proxy FROM award_receive_record  WHERE create_time BETWEEN ?1 AND ?2 AND award_type = 1 GROUP BY third_proxy ",nativeQuery = true)
    List<Map<String, Object>> getMapSumTodayAward(String startTime, String endTime);

    @Query(value = "SELECT IFNULL( SUM( amount ), 0 ) AS amount,IFNULL(first_proxy, 0 ) first_proxy,IFNULL(second_proxy, 0 ) second_proxy,"
        + "IFNULL(third_proxy, 0 ) third_proxy FROM award_receive_record  WHERE receive_time BETWEEN ?1 AND ?2 AND award_type = 2 AND receive_status = 1 GROUP BY third_proxy; ",nativeQuery = true)
    List<Map<String, Object>> getMapSumRiseAward(String startTime, String endTime);
}
