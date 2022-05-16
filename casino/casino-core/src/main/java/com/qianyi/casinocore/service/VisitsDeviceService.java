package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.VisitsDevice;
import com.qianyi.casinocore.repository.VisitsDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VisitsDeviceService {

    @Autowired
    private VisitsDeviceRepository visitsDeviceRepository;

    public void save(VisitsDevice device) {
        visitsDeviceRepository.save(device);
    }
    public void deleteById(Long id) {
        visitsDeviceRepository.deleteById(id);
    }

    public VisitsDevice findByManufacturerAndUdid(String manufacturer,String udid) {
        return visitsDeviceRepository.findByManufacturerAndUdid(manufacturer,udid);
    }
}
