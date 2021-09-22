package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.CollectionBankcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CollectionBankCardRepository extends JpaRepository<CollectionBankcard,Long>, JpaSpecificationExecutor<CollectionBankcard> {

    CollectionBankcard findByBankNo(String bankNo);

    List<CollectionBankcard> findByDisable(Integer disable);
}
