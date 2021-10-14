package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ProxyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface ProxyUserRepository extends JpaRepository<ProxyUser,Long>, JpaSpecificationExecutor<ProxyUser> {

    ProxyUser findByUserName(String userName);

    @Transactional
    @Query("update ProxyUser u set u.gaKey=?2 where u.id=?1")
    @Modifying
    void setSecretById(Long id, String gaKey);

    ProxyUser findAllById(Long id);

    ProxyUser findByProxyCode(String inviteCode);
}
