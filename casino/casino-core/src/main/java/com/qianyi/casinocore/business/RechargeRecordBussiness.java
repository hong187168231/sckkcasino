package com.qianyi.casinocore.business;


import com.qianyi.casinocore.vo.RechargeProxyBO;
import com.qianyi.casinocore.vo.RechargeRecordVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RechargeRecordBussiness {

    @Autowired
    private ProxyDayReportBusiness proxyDayReportBusiness;

    @Autowired
    private ProxyReportBusiness proxyReportBusiness;

    /**
     * 内部调用，单线程
     * 统计新增金额，人数
     * @param rechargeRecordVo 充值消息对象
     */
    public void procerssShareProfit(RechargeRecordVo rechargeRecordVo){
        if(rechargeRecordVo == null || rechargeRecordVo.getUserId() == null || rechargeRecordVo.getFirstUserId() == null){
            return;
        }
        log.info("充值消息对象：{}", rechargeRecordVo);
        List<RechargeProxyBO> rechargeProxyList = getRechargeProxys(rechargeRecordVo);
        processList(rechargeProxyList);
    }

    @Transactional
    protected void processList(List<RechargeProxyBO> rechargeProxyList) {
        rechargeProxyList.forEach(item->processItem(item));
    }

    private void processItem(RechargeProxyBO rechargeProxy){
        proxyDayReportBusiness.processChargeAmount(rechargeProxy);
        proxyReportBusiness.processRechargeReport(rechargeProxy);
    }

    public List<RechargeProxyBO> getRechargeProxys(RechargeRecordVo rechargeRecordVo){
        List<RechargeProxyBO> rechargeProxyList = new ArrayList<>();
        if(rechargeRecordVo.getFirstUserId() != null)
            rechargeProxyList.add(getItemByVo(rechargeRecordVo.getFirstUserId(),rechargeRecordVo,true));
        if(rechargeRecordVo.getSecondUserId() != null)
            rechargeProxyList.add(getItemByVo(rechargeRecordVo.getSecondUserId(),rechargeRecordVo,false));
        if(rechargeRecordVo.getThirdUserId() != null)
            rechargeProxyList.add(getItemByVo(rechargeRecordVo.getThirdUserId(),rechargeRecordVo,false));
        return rechargeProxyList;
    }

    private RechargeProxyBO getItemByVo(Long proxyUserId, RechargeRecordVo rechargeRecordVo, boolean isDrect){
        RechargeProxyBO rechargeProxy = new RechargeProxyBO();
        rechargeProxy.setProxyUserId(proxyUserId);
        rechargeProxy.setAmount(rechargeRecordVo.getChargeAmount());
        rechargeProxy.setIsFirst(rechargeRecordVo.getIsFirst());
        rechargeProxy.setDirect(isDrect);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        rechargeProxy.setDayTime(format.format(rechargeRecordVo.getCreateTime()));
        return rechargeProxy;
    }
}
