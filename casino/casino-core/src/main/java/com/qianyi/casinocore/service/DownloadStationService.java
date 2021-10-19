package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.DownloadStation;
import com.qianyi.casinocore.repository.DownloadStationRepository;
import com.qianyi.modulecommon.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
@CacheConfig(cacheNames = {"downloadStation"})
public class DownloadStationService {

    @Autowired
    private DownloadStationRepository downloadStationRepository;

    /**
     * 根据终端类型查询最新版本
     * @param terminalType
     * @return
     */
    @Cacheable(key = "#terminalType")
    public DownloadStation getNewestVersion(Integer terminalType) {
        return downloadStationRepository.findFirstByTerminalTypeOrderByCreateTimeDesc(terminalType);
    }

    /**
     * 根据终端类型查询最新强制更新版本
     * @param terminalType
     * @param isForced
     * @return
     */
    @Cacheable(key = "#terminalType+'::'+#isForced")
    public DownloadStation getForcedNewestVersion(Integer terminalType,Integer isForced) {
        return downloadStationRepository.findFirstByTerminalTypeAndIsForcedOrderByCreateTimeDesc(terminalType,isForced);
    }

    public Page<DownloadStation> findPage(Pageable pageable) {
        return downloadStationRepository.findAll(pageable);
    }

    @CacheEvict(key = "#p0.terminalType",allEntries = true)
    public DownloadStation save(DownloadStation downloadStation) {
        return downloadStationRepository.save(downloadStation);

    }

    @Cacheable(key = "#id")
    public DownloadStation findById(Long id) {
        Optional<DownloadStation> optional = downloadStationRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
}
