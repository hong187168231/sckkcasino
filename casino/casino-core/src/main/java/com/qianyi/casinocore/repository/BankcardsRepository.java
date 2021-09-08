package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.Bankcards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BankcardsRepository extends JpaRepository<Bankcards,Long> , JpaSpecificationExecutor<Bankcards> {

    @Query(value = "select * from bankcards where user_id = ? order by default_card desc", nativeQuery = true)
    List<Bankcards> findBankcardsById(Long userId);

    @Query(value = "select * from bankcards where user_id = ? limit 1", nativeQuery = true)
    Bankcards findBankcardsInUserCardByUserId(Long userId);
}
