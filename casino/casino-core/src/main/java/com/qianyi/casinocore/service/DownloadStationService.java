package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.DownloadStation;
import com.qianyi.casinocore.repository.DownloadStationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class DownloadStationService {

    @Autowired
    private DownloadStationRepository downloadStationRepository;

    /**
     * 根据终端类型查询最新版本
     * @param terminalType
     * @return
     */
    public DownloadStation getNewestVersion(Integer terminalType) {
        return downloadStationRepository.findFirstByTerminalTypeOrderByCreateTimeDesc(terminalType);
    }

    /**
     * 根据终端类型查询最新强制更新版本
     * @param terminalType
     * @param isForced
     * @return
     */
    public DownloadStation getForcedNewestVersion(Integer terminalType,Integer isForced) {
        return downloadStationRepository.findFirstByTerminalTypeAndIsForcedOrderByCreateTimeDesc(terminalType,isForced);
    }
}
