package com.qianyi.casinocore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.qianyi.casinocore.model.BankInfo;

public interface BankInfoRepository extends JpaRepository<BankInfo,Long> {
}
