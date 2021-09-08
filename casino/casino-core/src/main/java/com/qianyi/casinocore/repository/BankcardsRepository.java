//package com.qianyi.casinocore.repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
//
//import java.util.List;
//
//public interface BankcardsRepository extends JpaRepository<Bankcards,Long> , JpaSpecificationExecutor<Bankcards> {
//
//    List<Bankcards> findBankcardsByUserIdOrderByDefaultCardDesc(Long userId);
//
//    Bankcards findFirstByUserId(Long userId);
//
//    int countByUserId(Long userId);
//}
