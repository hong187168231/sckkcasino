package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ProxyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProxyUserRepository extends JpaRepository<ProxyUser,Long>, JpaSpecificationExecutor<ProxyUser> {

    ProxyUser findByUserName(String userName);

    void setSecretById(Long id, String gaKey);

    ProxyUser findAllById(Long id);
}
