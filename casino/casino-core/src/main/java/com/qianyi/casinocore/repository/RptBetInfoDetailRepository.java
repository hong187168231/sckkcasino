package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.RptBetInfoDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RptBetInfoDetailRepository extends JpaRepository<RptBetInfoDetail,Long>, JpaSpecificationExecutor<RptBetInfoDetail> {
}
