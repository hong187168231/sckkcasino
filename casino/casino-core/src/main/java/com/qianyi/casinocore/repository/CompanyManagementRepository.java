package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.CompanyManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface CompanyManagementRepository  extends JpaRepository<CompanyManagement,Long>,
    JpaSpecificationExecutor<CompanyManagement> {

    @Query(value = "select co.id as id, co.update_by as createName, co.company_name as companyName ,count(1) as proxyNum\n" +
            "from company_management co left JOIN proxy_user pr on co.id = pr.company_id and proxy_role = 1\n" +
            "where if(?1 is not null and 1!='', co.company_name=?1, 1=1)\n" +
            "GROUP BY co.id",nativeQuery = true)
    List<Map> findGroupByCount(String companyName);
}
