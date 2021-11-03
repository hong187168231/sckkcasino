package com.qianyi.casinoreport.business;

import com.qianyi.casinocore.constant.ShareProfitConstant;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.ShareProfitBO;
import com.qianyi.casinocore.vo.ShareProfitMqVo;
import com.qianyi.casinocore.vo.ShareProfitVo;
import com.qianyi.casinoreport.business.ProxyDayReportBusiness;
import com.qianyi.casinoreport.business.ProxyReportBusiness;
import com.qianyi.casinoreport.util.ReportConstant;
import com.qianyi.casinoreport.util.ShareProfitUtils;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ShareProfitBusiness {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMoneyService userMoneyService;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private PlatformConfigService platformConfigService;

    @Autowired
    private ConsumerErrorService consumerErrorService;

    @Autowired
    private ShareProfitTransactionService shareProfitTransactionService;

    public void procerssShareProfit(ShareProfitMqVo shareProfitMqVo){
        try {
            PlatformConfig platformConfig = platformConfigService.findFirst();
            GameRecord record = gameRecordService.findGameRecordById(shareProfitMqVo.getGameRecordId());
            List<ShareProfitBO> shareProfitBOList = shareProfitOperator(platformConfig, shareProfitMqVo);
            shareProfitTransactionService.processShareProfitList(shareProfitBOList, record);
        }catch (Exception e){
            log.error("share profit error : {}",e);
            recordFailVo(shareProfitMqVo);
        }
    }

    private void recordFailVo(ShareProfitMqVo shareProfitMqVo){
        ConsumerError consumerError = new ConsumerError();
        consumerError.setConsumerType(ReportConstant.SHAREPOINT);
        consumerError.setMainId(shareProfitMqVo.getGameRecordId());
        consumerError.setRepairStatus(0);
        consumerErrorService.save(consumerError);
    }

    private List<ShareProfitBO> shareProfitOperator(PlatformConfig platformConfig, ShareProfitMqVo shareProfitMqVo) {
        User user = userService.findById(shareProfitMqVo.getUserId());
        log.info("shareProfitOperator user:{}",user);
        String betTime = shareProfitMqVo.getBetTime().substring(0,10);
        List<ShareProfitBO> shareProfitBOList = new ArrayList<>();
        if(ShareProfitUtils.compareIntegerNotNull( user.getFirstPid()))
            shareProfitBOList.add(getShareProfitBO(user.getFirstPid(),shareProfitMqVo.getValidbet(),platformConfig.getFirstCommission(),getUserIsFirstBet(user),betTime,true));
        if(ShareProfitUtils.compareIntegerNotNull( user.getSecondPid()))
            shareProfitBOList.add(getShareProfitBO(user.getSecondPid(),shareProfitMqVo.getValidbet(),platformConfig.getSecondCommission(),getUserIsFirstBet(user),betTime,false));
        if(ShareProfitUtils.compareIntegerNotNull( user.getThirdPid()))
            shareProfitBOList.add(getShareProfitBO(user.getThirdPid(),shareProfitMqVo.getValidbet(),platformConfig.getThirdCommission(),getUserIsFirstBet(user),betTime,false));
        log.info("get list object is {}",shareProfitBOList);
        return shareProfitBOList;
    }

    private ShareProfitBO getShareProfitBO(Long userId,BigDecimal betAmount,BigDecimal commission,Boolean isFirst,String betTime,boolean direct){
        ShareProfitBO shareProfitBO = new ShareProfitBO();
        shareProfitBO.setUserId(userId);
        shareProfitBO.setBetAmount(betAmount);
        shareProfitBO.setProfitAmount(betAmount.multiply(commission));
        shareProfitBO.setFirst(isFirst);
        shareProfitBO.setBetTime(betTime);
        shareProfitBO.setDirect(direct);
        shareProfitBO.setCommission(commission);
        return shareProfitBO;
    }

    private boolean getUserIsFirstBet(User user){
        if (user.getIsFirstBet() != null && user.getIsFirstBet() == Constants.no)
            return true;
        return false;
    }
}
