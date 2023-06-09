package com.qianyi.casinoreport.business;

import com.qianyi.casinocore.model.ProxyDayReport;
import com.qianyi.casinocore.service.ProxyDayReportService;
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
public class LevelProxyDayReportBusiness {
    @Autowired
    private ProxyDayReportService proxyDayReportService;

    /**
     * 处理分润
     * @param shareProfitBO
     */
    public void processReport(ShareProfitBO shareProfitBO){
        ProxyDayReport proxyDayReport = getProxyDayReport(shareProfitBO);
        //        proxyDayReport.setProfitAmount(proxyDayReport.getProfitAmount().add(shareProfitBO.getProfitAmount()));
        //        proxyDayReport.setBetAmount(proxyDayReport.getBetAmount().add(shareProfitBO.getBetAmount()));
        log.info("处理日报表id{} ProxyDayReport========================》:{}",proxyDayReport.getId(),proxyDayReport.toString());
        //        proxyDayReport = proxyDayReportService.save(proxyDayReport);
        //        log.info("处理日报表ProxyDayReport========================》:{}",proxyDayReport.toString());
        proxyDayReportService.updateProxyDayReport(shareProfitBO.getUserId(),shareProfitBO.getProfitAmount(),shareProfitBO.getBetAmount(),shareProfitBO.getBetTime());
    }

    private ProxyDayReport getProxyDayReport(ShareProfitBO shareProfitBO) {
        return getProxyDayReport(shareProfitBO.getUserId(),shareProfitBO.getBetTime());
    }

    private ProxyDayReport buildProxyDayReport(Long userId,String dayTime) {
        ProxyDayReport proxyDayReport = new ProxyDayReport();
        proxyDayReport.setUserId(userId);
        proxyDayReport.setDayTime(dayTime);
        return proxyDayReportService.save(proxyDayReport);
    }

    private ProxyDayReport getProxyDayReport(Long userId,String dayTime) {
        log.info("getProxyDayReport user id is {}, dayTIme is {}",userId, dayTime);
        ProxyDayReport proxyDayReport = proxyDayReportService.findByUserIdAndDay(userId,dayTime);
        log.info("处理日报表查询ProxyDayReport========================》:{}",proxyDayReport);
        if(proxyDayReport == null)
            buildProxyDayReport(userId,dayTime);
        return proxyDayReportService.findByUserIdAndDay(userId,dayTime);
    }



    /**
     * 处理充值
     * @param rechargeProxy
     */
    public void processChargeAmount(RechargeProxyBO rechargeProxy){
        log.info("process single charge amount {}",rechargeProxy);
        ProxyDayReport proxyDayReport = getProxyDayReport(rechargeProxy);
        proxyDayReport.setDeppositeAmount(proxyDayReport.getDeppositeAmount().add(rechargeProxy.getAmount()));
        proxyDayReportService.save(proxyDayReport);
    }

    private ProxyDayReport getProxyDayReport(RechargeProxyBO rechargeProxy) {
        return getProxyDayReport(rechargeProxy.getProxyUserId(),rechargeProxy.getDayTime());
    }



    /**
     * 处理团队用户数量
     * @param proxyUserBO
     */
    public void processUser(ProxyUserBO proxyUserBO){
        ProxyDayReport proxyDayReport = getProxyDayReport(proxyUserBO.getProxyUserId(),proxyUserBO.getDayTime());
        proxyDayReport.setNewNum(proxyDayReport.getNewNum()+1);
        proxyDayReportService.save(proxyDayReport);
    }

}
