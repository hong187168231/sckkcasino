package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.WashCodeChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WashCodeChangeRepository extends JpaRepository<WashCodeChange,Long>, JpaSpecificationExecutor<WashCodeChange> {
}
