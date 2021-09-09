package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.Bankcards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface BankcardsRepository extends JpaRepository<Bankcards,Long> , JpaSpecificationExecutor<Bankcards> {

    List<Bankcards> findBankcardsByUserIdOrderByDefaultCardDesc(Long userId);

    Bankcards findFirstByUserId(Long userId);

    int countByUserId(Long userId);

    @Query(value = "select b.bank_account ,bi.bank_name from bankcards b \n" +
            "join bank_info bi on b.bank_id = bi.id\n" +
            "where b.user_id = ?", nativeQuery = true)
    List<Map<String,Object>> findForBankcards(Long userId);
}
