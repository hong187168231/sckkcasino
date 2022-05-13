package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.VisitsDeviceAddressBook;
import com.qianyi.casinocore.repository.VisitsDeviceAddressBookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VisitsDeviceAddressBookService {

    @Autowired
    private VisitsDeviceAddressBookRepository visitsDeviceAddressBookRepository;

    public void saveAll(List<VisitsDeviceAddressBook> addressBook) {
        visitsDeviceAddressBookRepository.saveAll(addressBook);
    }
}
