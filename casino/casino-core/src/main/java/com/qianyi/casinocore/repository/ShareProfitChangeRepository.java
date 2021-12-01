package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.ShareProfitChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface ShareProfitChangeRepository extends JpaRepository<ShareProfitChange,Long>, JpaSpecificationExecutor<ShareProfitChange> {


    ShareProfitChange findUserIdAndOrderOn(Long userId,String orderOn);
}
