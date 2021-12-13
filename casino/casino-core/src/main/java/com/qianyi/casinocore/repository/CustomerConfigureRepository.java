package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.CustomerConfigure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerConfigureRepository extends JpaRepository<CustomerConfigure,Long> {

    List<CustomerConfigure> findByState(Integer state);
}
