package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.IpWhite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IpWhiteRepository extends JpaRepository<IpWhite,Long>, JpaSpecificationExecutor<IpWhite> {

    IpWhite findByIpAndType(String ip, Integer type);
}
