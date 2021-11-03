package com.qianyi.casinoreport.business;

import com.qianyi.casinocore.constant.ShareProfitConstant;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.vo.ShareProfitBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class ShareprofitItemService {

    @Autowired
    private UserMoneyService userMoneyService;

    @Autowired
    private ProxyDayReportBusiness proxyDayReportBusiness;

    @Autowired
    private ProxyReportBusiness proxyReportBusiness;

    public void processItem(ShareProfitBO shareProfitBO, GameRecord record, List<ProxyDayReport> proxyDayReportList, List<ProxyReport> proxyReportList, List<UserMoney> userMoneyList, List<ShareProfitChange> shareProfitChangeList){
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(shareProfitBO.getUserId());
        if(userMoney==null)return;
        //明细入库
        ShareProfitChange shareProfitChange = processProfitDetail(shareProfitBO,userMoney,record);
        //进行分润
        userMoney.setShareProfit(userMoney.getShareProfit().add(shareProfitBO.getProfitAmount()));
        //进行日报表处理
        ProxyDayReport proxyDayReport = proxyDayReportBusiness.processReport(shareProfitBO);
        //进行总报表处理
        ProxyReport proxyReport = proxyReportBusiness.processReport(shareProfitBO);

        log.info("userMoney:{} \n shareProfitChange:{} \n proxyDayReport:{} \n proxyReport:{}",userMoney, shareProfitChange,proxyDayReport,proxyReport);

        proxyDayReportList.add(proxyDayReport);
        proxyReportList.add(proxyReport);
        userMoneyList.add(userMoney);
        shareProfitChangeList.add(shareProfitChange);

    }

    private ShareProfitChange processProfitDetail(ShareProfitBO shareProfitBO,UserMoney userMoney,GameRecord record) {
        log.info("gamerecord is {}",record);
        ShareProfitChange shareProfitChange = new ShareProfitChange();
        shareProfitChange.setAmount(shareProfitBO.getProfitAmount());
        shareProfitChange.setUserId(shareProfitBO.getUserId());
        shareProfitChange.setOrderNo(record.getBetId());
        shareProfitChange.setAmountBefore(userMoney.getShareProfit());
        shareProfitChange.setAmountAfter(getAfterAmount(shareProfitBO,userMoney));
        shareProfitChange.setType(ShareProfitConstant.SHARE_PROFIT_TYPE);
        shareProfitChange.setFromUserId(record.getUserId());
        shareProfitChange.setProfitRate(shareProfitBO.getCommission());
        return shareProfitChange;
//        shareProfitChangeService.save(shareProfitChange);
    }

    private BigDecimal getAfterAmount(ShareProfitBO shareProfitBO, UserMoney userMoney){
        return userMoney.getShareProfit().add(shareProfitBO.getProfitAmount());
    }
}
