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

    @Query(value = "select b.id,b.bank_id ,b.bank_account ,bi.bank_name,b.real_name,b.default_card from bankcards b \n" +
            "join bank_info bi on b.bank_id = bi.id\n" +
            "where b.disable=0 and b.user_id = ?", nativeQuery = true)
    List<Map<String,Object>> findForBankcards(Long userId);

    Bankcards findByUserIdAndDefaultCard(Long userId, int defaultCard);
}
