package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.DownloadStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DownloadStationRepository extends JpaRepository<DownloadStation,Long>, JpaSpecificationExecutor<DownloadStation> {
}
