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

import java.math.BigDecimal;

@Slf4j
@Service
//@Transactional(rollbackFor = Exception.class)
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
        //        allProxy(proxyReport,shareProfitBO);
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
        //        proxyReport.setDirectBetAmount(proxyReport.getDirectBetAmount().add(shareProfitBO.getBetAmount()));
        //        proxyReport.setDirectProfitAmount(proxyReport.getDirectProfitAmount().add(shareProfitBO.getProfitAmount()));
        //        proxyReport.setDirectBetNum(proxyReport.getDirectBetNum()+ (shareProfitBO.isFirst()?1:0));
        log.info("处理直属报表id{} ProxyReport========================》:{}",proxyReport.getId(),proxyReport.toString());
        //        proxyReport = proxyReportService.save(proxyReport);
        //        log.info("处理直属报表ProxyReport========================》:{}",proxyReport.toString());
        boolean first = shareProfitBO.isFirst();
        log.info("处理直属报表first:{} betAmount:{} profitAmount:{}========================》",first,shareProfitBO.getBetAmount(),shareProfitBO.getProfitAmount());
        proxyReportService.updateDirect(shareProfitBO.getBetAmount(),shareProfitBO.getProfitAmount(),first?1:0,shareProfitBO.getUserId());
    }

    public void noDirectProxy(ProxyReport proxyReport,ShareProfitBO shareProfitBO){
        //        proxyReport.setOtherBetAmount(proxyReport.getOtherBetAmount().add(shareProfitBO.getBetAmount()));
        //        proxyReport.setOtherProfitAmount(proxyReport.getOtherProfitAmount().add(shareProfitBO.getProfitAmount()));
        //        proxyReport.setOtherBetNum(proxyReport.getOtherBetNum()+ (shareProfitBO.isFirst()?1:0));
        log.info("处理非直属报表id{} ProxyReport========================》:{}",proxyReport.getId(),proxyReport.toString());
        //        proxyReport = proxyReportService.save(proxyReport);
        //        log.info("处理非直属报表ProxyReport========================》:{}",proxyReport.toString());
        boolean first = shareProfitBO.isFirst();
        log.info("处理非直属报表first:{} betAmount:{} profitAmount:{}========================》",first,shareProfitBO.getBetAmount(),shareProfitBO.getProfitAmount());
        proxyReportService.updateOther(shareProfitBO.getBetAmount(),shareProfitBO.getProfitAmount(),shareProfitBO.isFirst()?1:0,shareProfitBO.getUserId());
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
        log.info("处理报表查询ProxyReport========================》:{}",proxyReport);
        if(proxyReport == null)
            buildProxyReport(userId);
        return proxyReportService.findByUserId(userId);
    }

    /**
     * 处理充值报表
     */
    public void processRechargeReport(RechargeProxyBO rechargeProxy){
        log.info("process recharge proxy bo {}",rechargeProxy);
        // 判断是否首充
        if(rechargeProxy.getIsFirst()==1){
            log.info("is not first charge");
        }else {
            ProxyReport proxyReport = getProxyReport(rechargeProxy.getProxyUserId());
            proxyReport.setAllChargeNum(proxyReport.getAllChargeNum()+1);
            if(rechargeProxy.isDirect())
                proxyReport.setDirectChargeNum(proxyReport.getDirectChargeNum()+1);
            else
                proxyReport.setOtherChargeNum(proxyReport.getOtherChargeNum()+1);
            proxyReportService.save(proxyReport);
        }
    }




    /**
     * 处理团队用户数量
     * @param proxyUserBO
     */
    public void processUser(ProxyUserBO proxyUserBO){
        log.info("query proxy report from db");
        ProxyReport proxyReport = getProxyReport(proxyUserBO.getProxyUserId());
        log.info("set proxy report userId {} group num value {} {} {}", proxyReport.getUserId(),proxyReport.getAllGroupNum(), proxyReport.getDirectGroupNum(), proxyReport.getOtherGroupNum());
        proxyReport.setAllGroupNum(proxyReport.getAllGroupNum()+1);
        if(proxyUserBO.isDrect())
            proxyReport.setDirectGroupNum(proxyReport.getDirectGroupNum()+1);
        else
            proxyReport.setOtherGroupNum(proxyReport.getOtherGroupNum()+1);
        proxyReportService.save(proxyReport);
    }

}
