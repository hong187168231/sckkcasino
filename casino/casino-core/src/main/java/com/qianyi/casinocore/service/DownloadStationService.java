package com.qianyi.casinocore.service;

import com.qianyi.casinocore.repository.DownloadStationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class DownloadStationService {

    @Autowired
    private DownloadStationRepository downloadStationRepository;
}
