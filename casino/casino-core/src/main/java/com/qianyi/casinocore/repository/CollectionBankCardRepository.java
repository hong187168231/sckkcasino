package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.CollectionBankcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface CollectionBankCardRepository extends JpaRepository<CollectionBankcard,Long>, JpaSpecificationExecutor<CollectionBankcard> {

    List<CollectionBankcard> findByBankNo(String bankNo);

    List<CollectionBankcard> findByDisableOrderBySortIdAsc(Integer disable);

    @Modifying
    @Query(value = "update collection_bankcard t set t.sort_id = ?2 where t.id = ?1 ",nativeQuery = true)
    void updateSortIdById(Long id,Integer sortId);

    @Query(value = "select t.id id,t.sort_id sort_id from collection_bankcard t ORDER BY t.sort_id ASC",nativeQuery = true)
    List<Map<String,Object>> find();
}
