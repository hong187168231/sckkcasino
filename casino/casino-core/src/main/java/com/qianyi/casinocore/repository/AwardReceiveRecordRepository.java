package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.AwardReceiveRecord;
import com.qianyi.casinocore.model.UserLevelRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

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




}
