package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.CompanyManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface CompanyManagementRepository  extends JpaRepository<CompanyManagement,Long>,
    JpaSpecificationExecutor<CompanyManagement> {

    @Query(value = "SELECT\n" +
            "\tco.id AS id,\n" +
            "\tsy.user_name AS createName,\n" +
            "\tco.company_name AS companyName,\n" +
            "\tco.create_time as createDate,\n" +
            "\tcount(pr.id) AS proxyNum \n" +
            "FROM\n" +
            "\tcompany_management co\n" +
            "\tLEFT JOIN sys_user sy on co.update_by = sy.id \n" +
            "\tLEFT JOIN proxy_user pr ON co.id = pr.company_id \n" +
            "\tAND proxy_role = 1 \n" +
            "WHERE\n" +
            "IF\n" +
            "\t( ?1 IS NOT NULL AND 1!= '', co.company_name =?1, 1 = 1 ) \n" +
            "GROUP BY\n" +
            "\tco.id",nativeQuery = true)
    List<Map> findGroupByCount(String companyName);
}
