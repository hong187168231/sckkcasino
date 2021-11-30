package com.qianyi.casinoreport.business;

import com.qianyi.casinocore.model.ProxyReport;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyReportService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.vo.ProxyUserBO;
import com.qianyi.casinocore.vo.RechargeProxyBO;
import com.qianyi.casinocore.vo.ShareProfitBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class LevelProxyReportBusiness {
    @Autowired
    private ProxyReportService proxyReportService;

    @Autowired
    private UserService userService;
    /**
     * 处理分润报表
     * @param shareProfitBO
     */
    public void processReport(ShareProfitBO shareProfitBO){
        ProxyReport proxyReport = getProxyReport(shareProfitBO.getUserId());
        allProxy(proxyReport,shareProfitBO);
        if(shareProfitBO.isDirect())
            directProxy(proxyReport,shareProfitBO);
        else
            noDirectProxy(proxyReport,shareProfitBO);

       //proxyReportService.save(proxyReport);
    }

    public void allProxy(ProxyReport proxyReport,ShareProfitBO shareProfitBO){
        proxyReport.setAllBetAmount(proxyReport.getAllBetAmount().add(shareProfitBO.getBetAmount()));
        proxyReport.setAllProfitAmount(proxyReport.getAllProfitAmount().add(shareProfitBO.getProfitAmount()));
        proxyReport.setAllBetNum(proxyReport.getAllBetNum()+ (shareProfitBO.isFirst()?1:0));
        proxyReportService.save(proxyReport);
    }

    public void directProxy(ProxyReport proxyReport,ShareProfitBO shareProfitBO){
        proxyReport.setDirectBetAmount(proxyReport.getDirectBetAmount().add(shareProfitBO.getBetAmount()));
        proxyReport.setDirectProfitAmount(proxyReport.getDirectProfitAmount().add(shareProfitBO.getProfitAmount()));
        proxyReport.setDirectBetNum(proxyReport.getDirectBetNum()+ (shareProfitBO.isFirst()?1:0));
        proxyReportService.save(proxyReport);
    }

    public void noDirectProxy(ProxyReport proxyReport,ShareProfitBO shareProfitBO){
        proxyReport.setOtherBetAmount(proxyReport.getOtherBetAmount().add(shareProfitBO.getBetAmount()));
        proxyReport.setOtherProfitAmount(proxyReport.getOtherProfitAmount().add(shareProfitBO.getProfitAmount()));
        proxyReport.setOtherBetNum(proxyReport.getOtherBetNum()+ (shareProfitBO.isFirst()?1:0));
        proxyReportService.save(proxyReport);
    }

    public ProxyReport buildProxyReport(Long userId) {
        log.info("build proxy report user id is {}",userId);
        User user = userService.findById(userId);
        ProxyReport proxyReport = new ProxyReport();
        proxyReport.setUserId(userId);
        proxyReport.setAccount(user.getAccount());
        proxyReportService.save(proxyReport);
        return proxyReport;
    }

    public ProxyReport getProxyReport(Long userId) {
        log.info("user id is {}",userId);
        ProxyReport proxyReport = proxyReportService.findByUserId(userId);
        if(proxyReport == null)
            buildProxyReport(userId);
        return proxyReportService.findByUserIdWithLock(userId);
    }
}
