package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoginLogRepository extends JpaRepository<LoginLog,Long>, JpaSpecificationExecutor<LoginLog> {
}
