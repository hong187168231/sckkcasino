package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.AccountChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AccountChangeRepository extends JpaRepository<AccountChange,Long>, JpaSpecificationExecutor<AccountChange> {
}
