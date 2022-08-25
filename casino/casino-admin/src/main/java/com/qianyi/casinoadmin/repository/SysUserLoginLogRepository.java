package com.qianyi.casinoadmin.repository;

import com.qianyi.casinoadmin.model.SysUserLoginLog;
import com.qianyi.casinocore.model.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysUserLoginLogRepository extends JpaRepository<SysUserLoginLog, Long>, JpaSpecificationExecutor<SysUserLoginLog> {
}
