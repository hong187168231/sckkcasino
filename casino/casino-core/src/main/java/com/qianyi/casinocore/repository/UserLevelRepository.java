package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.UserLevelRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface UserLevelRepository extends JpaRepository<UserLevelRecord, Long>, JpaSpecificationExecutor<UserLevelRecord> {

    @Query(value = "select  * from user_level ul where  ul.change_type = 2 ", nativeQuery = true)
    UserLevelRecord queryKeepLevel(String account);


    @Query
    UserLevelRecord findByUserId(Long userId);


    @Query(value = "select  * from user_level_record ul where  ul.change_type = 2  " +
            " and  ul.user_id =  ?1 and  ul.level =  ?2 order by create_time desc  limit 1", nativeQuery = true)
    UserLevelRecord findDropRecord(Long userId, Integer level);


    @Query(value = " SELECT  " +
            "  id,  " +
            "  userId  " +
            "FROM  " +
            "  (  " +
            "    SELECT  " +
            "      id AS id,  " +
            "      user_id AS userId,  " +
            "      update_time AS updateTime  " +
            "    FROM  " +
            "      user_level_record  " +
            "    WHERE  id  in " +
            "       ( SELECT MAX(id) AS id FROM user_level_record WHERE `level` > 1 GROUP BY user_id ) " +
            "  ) k  " +
            "WHERE  " +
            " 1=1 and   updateTime BETWEEN ?1  " +
            "AND ?2  AND updateTime <= now() - INTERVAL 360 HOUR  " +
            "AND userId NOT IN (  " +
            "  SELECT  " +
            "    user_id AS userId  " +
            "  FROM  " +
            "    user_level_decline  " +
            "  WHERE  " +
            "    today_decline_status = 1  " +
            "  AND user_id = k.userId  " +
            "  AND to_days(create_time) = to_days(now()) " +
            "  order by updateTime desc  " +
            ")  " +
            "LIMIT 50 ", nativeQuery = true)
    List<Map<String, Object>> findLastRiseUser(String startTime, String endTime);


    @Modifying
    @Query("update UserLevelRecord s set s.todayKeepStatus =:todayKeepStatus,s.updateTime =:updateTime where s.id=:id")
    void updateTodayKeepStatusById(Integer todayKeepStatus, Date updateTime, Long id);
}
