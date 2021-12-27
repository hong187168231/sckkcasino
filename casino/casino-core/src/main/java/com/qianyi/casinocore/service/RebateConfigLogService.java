package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ProxyRebateConfig;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.RebateConfigLog;
import com.qianyi.casinocore.repository.RebateConfigLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RebateConfigLogService {


    @Autowired
    private RebateConfigLogRepository rebateConfigLogRepository;


    /**
     * 查询当月全局的佣金配置
     * @param type
     * @param time
     * @return
     */
    public RebateConfigLog findByTypeAndStaticsTimes(Integer type, String time) {
        return rebateConfigLogRepository.findByTypeAndStaticsTimes(type,time);
    }

    public void delete(Long id) {
        rebateConfigLogRepository.deleteById(id);
    }

    public RebateConfigLog save(RebateConfigLog rebateConfigLog) {
        return rebateConfigLogRepository.save(rebateConfigLog);
    }


    /**
     * 查询当月个人的佣金配置
     * @param type
     * @param time
     * @param proxyUserId
     * @return
     */
    public RebateConfigLog findByTypeAndStaticsTimesAndProxyUserId(Integer type, String time,Long proxyUserId) {
        return rebateConfigLogRepository.findByTypeAndStaticsTimesAndProxyUserId(type,time,proxyUserId);
    }

}
