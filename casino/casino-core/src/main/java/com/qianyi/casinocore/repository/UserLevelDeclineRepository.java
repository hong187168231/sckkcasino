package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.UserLevelDecline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface UserLevelDeclineRepository extends JpaRepository<UserLevelDecline, Long>, JpaSpecificationExecutor<UserLevelDecline> {


    @Query(value = "select count(1) from user_level_decline  arr where user_id =?1  and arr.create_time BETWEEN ?2 and ?3  ", nativeQuery = true)
    int queryBonusAmount(Long userId,String startTime, String endTime);

}
