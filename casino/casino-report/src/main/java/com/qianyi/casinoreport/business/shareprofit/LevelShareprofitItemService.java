package com.qianyi.casinoreport.business.shareprofit;

import com.qianyi.casinocore.constant.ShareProfitConstant;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.ShareProfitBO;
import com.qianyi.casinoreport.business.LevelProxyReportBusiness;
import com.qianyi.casinoreport.business.ProxyReportBusiness;
import com.qianyi.casinoreport.business.LevelProxyDayReportBusiness;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class LevelShareprofitItemService {

    @Autowired
    private UserMoneyService userMoneyService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserReportService userReportService;


    @Autowired
    private ShareProfitChangeService shareProfitChangeService;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private LevelProxyDayReportBusiness levelProxyDayReportBusiness;
    @Autowired
    private LevelProxyReportBusiness levelproxyReportBusiness;

    /**
     *  处理各级代理分润入库
     * @param shareProfitBO
     */
    @Transactional(rollbackFor = Exception.class)
    public void levelProcessItem(ShareProfitBO shareProfitBO){
        Long startTime = System.currentTimeMillis();
        ShareProfitChange ShareProfitChangeInfo = shareProfitChangeService.findByUserIdAndOrderNo(shareProfitBO.getUserId(), shareProfitBO.getRecordBetId());
        if (ShareProfitChangeInfo==null){
            UserMoney userMoney = userMoneyService.findUserByUserIdUse(shareProfitBO.getUserId());
            User user = userReportService.findUserByUserIdUse(shareProfitBO.getRecordUserId());
            if(userMoney==null)return;
            log.info("shareProfitBOList processItem That took {} milliseconds",System.currentTimeMillis()-startTime);
            //明细入库
            levelProcessProfitDetail(shareProfitBO,userMoney);
            //进行分润
            //userMoney.setShareProfit(userMoney.getShareProfit().add(shareProfitBO.getProfitAmount()));
            userMoneyService.changeProfit(userMoney.getUserId(),shareProfitBO.getProfitAmount());

            //进行日报表处理
            levelProxyDayReportBusiness.processReport(shareProfitBO);
            //进行总报表处理
            levelproxyReportBusiness.processReport(shareProfitBO);

            if(user.getIsFirstBet()==Constants.no){
                //设置第一次投注用户
                userService.updateIsFirstBet(user.getId(), Constants.yes);
            }
            log.info("all store That took {} milliseconds",System.currentTimeMillis()-startTime);
            //更新分润状态
             gameRecordService.updateProfitStatus(shareProfitBO.getRecordId(), Constants.yes);
            log.info("processShareProfitList That took {} milliseconds",System.currentTimeMillis()-startTime);
/*            //报表处理mq
            reportMq(shareProfitBO);*/

        }
    }


    /**
     * 分发各级代理分润报表处理mq
     * @param shareProfitBO
     */
    public void reportMq(ShareProfitBO shareProfitBO) {
        if (shareProfitBO != null) {
            String routingKey = null;
            String routingKeyDay = null;
            long remainder = shareProfitBO.getUserId() % 6;
            if (remainder == 1) {
                routingKeyDay = RabbitMqConstants.ONE_REPORTDAY_PROFIT_DIRECT;
                routingKey = RabbitMqConstants.ONE_REPORT_PROFIT_DIRECT;
            }
            if (remainder == 2) {
                routingKeyDay = RabbitMqConstants.TWO_REPORTDAY_PROFIT_DIRECT;
                routingKey = RabbitMqConstants.TWO_REPORT_PROFIT_DIRECT;
            }
            if (remainder == 3) {
                routingKeyDay = RabbitMqConstants.THREE_REPORTDAY_PROFIT_DIRECT;
                routingKey = RabbitMqConstants.THREE_REPORT_PROFIT_DIRECT;
            }
            if (remainder == 4) {
                routingKeyDay = RabbitMqConstants.FOUR_REPORTDAY_PROFIT_DIRECT;
                routingKey = RabbitMqConstants.FOUR_REPORT_PROFIT_DIRECT;
            }
            if (remainder == 5 ) {
                routingKeyDay = RabbitMqConstants.FIVE_REPORTDAY_PROFIT_DIRECT;
                routingKey = RabbitMqConstants.FIVE_REPORT_PROFIT_DIRECT;
            }
            if (remainder == 6 || remainder == 0) {
                routingKeyDay = RabbitMqConstants.SIX_REPORTDAY_PROFIT_DIRECT;
                routingKey = RabbitMqConstants.SIX_REPORT_PROFIT_DIRECT;
            }
            //进行日报表处理
            rabbitTemplate.convertAndSend(RabbitMqConstants.REPORTDAY_PROFIT_DIRECTEXCHANGE,
                    routingKeyDay, shareProfitBO, new CorrelationData(UUID.randomUUID().toString()));

            //进行总报表处理
            rabbitTemplate.convertAndSend(RabbitMqConstants.REPORT_PROFIT_DIRECTEXCHANGE,
                    routingKey, shareProfitBO, new CorrelationData(UUID.randomUUID().toString()));
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
        shareProfitChange.setBetTime(shareProfitBO.getBetDate());
        log.info("shareProfitBO:{}",shareProfitBO);
        shareProfitChangeService.save(shareProfitChange);
    }

    private BigDecimal getAfterAmount(ShareProfitBO shareProfitBO, UserMoney userMoney){
        return userMoney.getShareProfit().add(shareProfitBO.getProfitAmount()).setScale(6,BigDecimal.ROUND_HALF_UP);
    }
}
