package com.qianyi.casinoreport.business.shareprofit;

import com.qianyi.casinocore.constant.ShareProfitConstant;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.casinocore.service.ShareProfitChangeService;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.vo.ShareProfitBO;
import com.qianyi.casinoreport.business.LevelProxyReportBusiness;
import com.qianyi.casinoreport.business.ProxyReportBusiness;
import com.qianyi.casinoreport.business.LevelProxyDayReportBusiness;
import com.qianyi.modulecommon.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
public class LevelShareprofitItemService {

    @Autowired
    private UserMoneyService userMoneyService;

    @Autowired
    private UserService userService;

    @Autowired
    private ShareProfitChangeService shareProfitChangeService;

    @Autowired
    private LevelProxyDayReportBusiness levelProxyDayReportBusiness;
    @Autowired
    private LevelProxyReportBusiness levelproxyReportBusiness;


    @Autowired
    private GameRecordService gameRecordService;

    /**
     *  处理各级代理分润入库
     * @param shareProfitBO
     */
    @Transactional(rollbackFor = Exception.class)
    public void levelProcessItem(ShareProfitBO shareProfitBO){
        ShareProfitChange ShareProfitChangeInfo = shareProfitChangeService.findUserIdAndOrderOn(shareProfitBO.getUserId(), shareProfitBO.getRecordBetId());
        if (ShareProfitChangeInfo==null){
            UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(shareProfitBO.getUserId());
            User user = userService.findUserByIdUseLock(shareProfitBO.getRecordUserId());
            if(userMoney==null)return;
            //明细入库
            levelProcessProfitDetail(shareProfitBO,userMoney);
            //进行分润
            userMoney.setShareProfit(userMoney.getShareProfit().add(shareProfitBO.getProfitAmount()));
            userMoneyService.changeProfit(userMoney.getUserId(),userMoney.getShareProfit());
            //进行日报表处理
            levelProxyDayReportBusiness.processReport(shareProfitBO);
            //进行总报表处理
            levelproxyReportBusiness.processReport(shareProfitBO);
            //设置第一次投注用户
            user.setIsFirstBet(1);
            userService.save(user);
            //更新分润状态
            gameRecordService.updateProfitStatus(shareProfitBO.getRecordId(), Constants.yes);
        }
    }


    private void levelProcessProfitDetail(ShareProfitBO shareProfitBO,UserMoney userMoney) {
        ShareProfitChange shareProfitChange = new ShareProfitChange();
        shareProfitChange.setAmount(shareProfitBO.getProfitAmount());
        shareProfitChange.setUserId(shareProfitBO.getUserId());
        shareProfitChange.setOrderNo(shareProfitBO.getRecordBetId());
        shareProfitChange.setAmountBefore(userMoney.getShareProfit());
        shareProfitChange.setAmountAfter(getAfterAmount(shareProfitBO,userMoney));
        shareProfitChange.setType(ShareProfitConstant.SHARE_PROFIT_TYPE);
        shareProfitChange.setFromUserId(shareProfitBO.getRecordUserId());
        shareProfitChange.setProfitRate(shareProfitBO.getCommission());
        shareProfitChange.setParentLevel(shareProfitBO.getParentLevel());
        shareProfitChange.setValidbet(shareProfitBO.getBetAmount());
        shareProfitChange.setBetTime(shareProfitBO.getBetTime());
        log.info("shareProfitBO:{}",shareProfitBO);
        shareProfitChangeService.save(shareProfitChange);
    }

    private BigDecimal getAfterAmount(ShareProfitBO shareProfitBO, UserMoney userMoney){
        return userMoney.getShareProfit().add(shareProfitBO.getProfitAmount());
    }
}
