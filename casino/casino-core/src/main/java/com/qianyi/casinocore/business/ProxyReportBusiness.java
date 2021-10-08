package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.ProxyReport;
import com.qianyi.casinocore.service.ProxyReportService;
import com.qianyi.casinocore.vo.ShareProfitBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProxyReportBusiness {
    @Autowired
    private ProxyReportService proxyReportService;

    public void processReport(ShareProfitBO shareProfitBO){
        ProxyReport proxyReport = getProxyDayReport(shareProfitBO);
        allProxy(proxyReport,shareProfitBO);
        if(shareProfitBO.isDirect())
            directProxy(proxyReport,shareProfitBO);
        else
            noDirectProxy(proxyReport,shareProfitBO);
        proxyReportService.save(proxyReport);
    }

    private void allProxy(ProxyReport proxyReport,ShareProfitBO shareProfitBO){
        proxyReport.setAllBetAmount(proxyReport.getAllBetAmount().add(shareProfitBO.getBetAmount()));
        proxyReport.setAllProfitAmount(proxyReport.getAllProfitAmount().add(shareProfitBO.getProfitAmount()));
        proxyReport.setAllBetNum(proxyReport.getAllBetNum()+ (shareProfitBO.isFirst()?1:0));
    }

    private void directProxy(ProxyReport proxyReport,ShareProfitBO shareProfitBO){
        proxyReport.setDirectBetAmount(proxyReport.getDirectBetAmount().add(shareProfitBO.getBetAmount()));
        proxyReport.setDirectProfitAmount(proxyReport.getDirectProfitAmount().add(shareProfitBO.getProfitAmount()));
        proxyReport.setDirectBetNum(proxyReport.getDirectBetNum()+ (shareProfitBO.isFirst()?1:0));
    }

    private void noDirectProxy(ProxyReport proxyReport,ShareProfitBO shareProfitBO){
        proxyReport.setOtherBetAmount(proxyReport.getOtherBetAmount().add(shareProfitBO.getBetAmount()));
        proxyReport.setOtherProfitAmount(proxyReport.getOtherProfitAmount().add(shareProfitBO.getProfitAmount()));
        proxyReport.setOtherBetNum(proxyReport.getOtherBetNum()+ (shareProfitBO.isFirst()?1:0));
    }

    private ProxyReport getProxyDayReport(ShareProfitBO shareProfitBO) {
        ProxyReport proxyDayReport = proxyReportService.findByUserId(shareProfitBO.getUserId());
        if(proxyDayReport == null)
            proxyDayReport = buildProxyDayReport(shareProfitBO);
        return proxyDayReport;
    }

    private ProxyReport buildProxyDayReport(ShareProfitBO shareProfitBO) {
        ProxyReport proxyDayReport = new ProxyReport();
        proxyDayReport.setUserId(shareProfitBO.getUserId());
        return proxyDayReport;
    }
}
