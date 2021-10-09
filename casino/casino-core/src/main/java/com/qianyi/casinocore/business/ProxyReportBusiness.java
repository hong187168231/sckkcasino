package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.ProxyReport;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyReportService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.vo.ProxyUserBO;
import com.qianyi.casinocore.vo.RechargeProxyBO;
import com.qianyi.casinocore.vo.ShareProfitBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProxyReportBusiness {
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

    private ProxyReport buildProxyReport(Long userId) {
        User user = userService.findById(userId);
        ProxyReport proxyReport = new ProxyReport();
        proxyReport.setUserId(userId);
        proxyReport.setAccount(user.getAccount());
        return proxyReport;
    }


    /**
     * 处理充值报表
     */
    public void processRechargeReport(RechargeProxyBO rechargeProxy){
        // 判断是否首充
        if(rechargeProxy.getIsFirst()==1)
            return;
        ProxyReport proxyReport = getProxyReport(rechargeProxy.getProxyUserId());
        proxyReport.setAllChargeNum(proxyReport.getAllChargeNum()+1);
        if(rechargeProxy.isDirect())
            proxyReport.setDirectChargeNum(proxyReport.getDirectChargeNum()+1);
        else
            proxyReport.setOtherChargeNum(proxyReport.getOtherChargeNum()+1);
        proxyReportService.save(proxyReport);
    }



    /**
     * 处理团队用户数量
     * @param proxyUserBO
     */
    public void processUser(ProxyUserBO proxyUserBO){
        ProxyReport proxyReport = getProxyReport(proxyUserBO.getProxyUserId());
        proxyReport.setAllGroupNum(proxyReport.getAllGroupNum()+1);
        if(proxyUserBO.isDrect())
            proxyReport.setDirectGroupNum(proxyReport.getDirectGroupNum()+1);
        else
            proxyReport.setOtherGroupNum(proxyReport.getOtherGroupNum()+1);

    }

    private ProxyReport getProxyReport(Long userId) {
        ProxyReport proxyReport = proxyReportService.findByUserId(userId);
        if(proxyReport == null)
            proxyReport = buildProxyReport(userId);
        return proxyReport;
    }
}
