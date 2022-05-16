package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.VisitsDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VisitsDeviceRepository extends JpaRepository<VisitsDevice, Long>, JpaSpecificationExecutor<VisitsDevice> {

    VisitsDevice findByIpAndManufacturerAndModel(String ip, String manufacturer, String model);

    VisitsDevice findByUdid(String udid);
}
