package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.ProxyDayReport;
import com.qianyi.casinocore.service.ProxyDayReportService;
import com.qianyi.casinocore.vo.ShareProfitBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProxyDayReportBusiness {
    @Autowired
    private ProxyDayReportService proxyDayReportService;

    public void processReport(ShareProfitBO shareProfitBO){

        ProxyDayReport proxyDayReport = getProxyDayReport(shareProfitBO);
        proxyDayReport.setProfitAmount(proxyDayReport.getProfitAmount().add(shareProfitBO.getProfitAmount()));
        proxyDayReport.setBetAmount(proxyDayReport.getBetAmount().add(shareProfitBO.getBetAmount()));
        proxyDayReportService.save(proxyDayReport);

    }

    private ProxyDayReport getProxyDayReport(ShareProfitBO shareProfitBO) {
        String dayTime = shareProfitBO.getBetTime().substring(0,11);
        ProxyDayReport proxyDayReport = proxyDayReportService.findByUserIdAndDay(shareProfitBO.getUserId(),dayTime);
        if(proxyDayReport == null)
            proxyDayReport = buildProxyDayReport(shareProfitBO,dayTime);
        return proxyDayReport;
    }

    private ProxyDayReport buildProxyDayReport(ShareProfitBO shareProfitBO,String dayTime) {
        ProxyDayReport proxyDayReport = new ProxyDayReport();
        proxyDayReport.setUserId(shareProfitBO.getUserId());
        proxyDayReport.setDayTime(dayTime);
        return proxyDayReport;
    }
}
