package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.IpBlack;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IpBlackRepository extends JpaRepository<IpBlack,Long> {

    IpBlack findByIp(String ip);
}
