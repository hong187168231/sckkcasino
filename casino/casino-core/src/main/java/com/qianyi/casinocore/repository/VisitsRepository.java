package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.Visits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VisitsRepository extends JpaRepository<Visits,Long>, JpaSpecificationExecutor<Visits> {
}
