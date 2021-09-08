package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.Bankcards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BankcardsRepository extends JpaRepository<Bankcards,Long> {

    List<Bankcards> findBankcardsByUserIdOrderByDefaultCardDesc(Long userId);

    Bankcards findFirstByUserId(Long userId);

    int countByUserId(Long userId);
}
