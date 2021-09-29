package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.DownloadStation;
import com.qianyi.casinocore.repository.DownloadStationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

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

    public Page<DownloadStation> findPage(Pageable pageable) {
        return downloadStationRepository.findAll(pageable);
    }

    public void save(DownloadStation downloadStation) {
        downloadStationRepository.save(downloadStation);

    }

    public DownloadStation findById(Long id) {
        Optional<DownloadStation> optional = downloadStationRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
}
