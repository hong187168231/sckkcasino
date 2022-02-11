package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordEndIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GameRecordEndIndexRepository  extends JpaRepository<GameRecordEndIndex,Long>,
    JpaSpecificationExecutor<GameRecordEndIndex> {
}
