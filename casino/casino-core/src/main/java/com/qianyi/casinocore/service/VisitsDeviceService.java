package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.VisitsDevice;
import com.qianyi.casinocore.repository.VisitsDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VisitsDeviceService {

    @Autowired
    private VisitsDeviceRepository visitsDeviceRepository;

    public VisitsDevice findByIpAndManufacturerAndModel(String ip, String manufacturer, String model) {
        return visitsDeviceRepository.findByIpAndManufacturerAndModel(ip, manufacturer, model);
    }

    public void save(VisitsDevice device) {
        visitsDeviceRepository.save(device);
    }
}
