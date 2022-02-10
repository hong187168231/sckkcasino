package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordGoldenF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GameRecordGoldenFRepository extends JpaRepository<GameRecordGoldenF,Long>, JpaSpecificationExecutor<GameRecordGoldenF> {
}
