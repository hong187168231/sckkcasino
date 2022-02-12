package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordGoldenF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface GameRecordGoldenFRepository extends JpaRepository<GameRecordGoldenF,Long>, JpaSpecificationExecutor<GameRecordGoldenF> {


    @Modifying
    @Query("update GameRecordGoldenF u set u.codeNumStatus=?2 where u.id=?1")
    void updateCodeNumStatus(Long id, Integer codeNumStatus);

    @Modifying
    @Query("update GameRecordGoldenF u set u.washCodeStatus=?2 where u.id=?1")
    void updateWashCodeStatus(Long id, Integer washCodeStatus);

    @Modifying
    @Query("update GameRecordGoldenF u set u.shareProfitStatus= u.shareProfitStatus+?2 where u.id=?1")
    void updateProfitStatus(Long id, Integer shareProfitStatus);
}
