package com.qianyi.casinocore.business;

import com.qianyi.casinocore.constant.ShareProfitConstant;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.ShareProfitBO;
import com.qianyi.casinocore.vo.ShareProfitMqVo;
import com.qianyi.casinocore.vo.ShareProfitVo;
import com.qianyi.casinoreport.business.ProxyDayReportBusiness;
import com.qianyi.casinoreport.business.ProxyReportBusiness;
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
    private ProxyDayReportBusiness proxyDayReportBusiness;

    @Autowired
    private ProxyReportBusiness proxyReportBusiness;

    @Autowired
    private ShareProfitChangeService shareProfitChangeService;


    public void procerssShareProfit(ShareProfitMqVo shareProfitMqVo){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        GameRecord record = gameRecordService.findGameRecordById(shareProfitMqVo.getGameRecordId());
        List<ShareProfitBO> shareProfitBOList = shareProfitOperator(platformConfig,shareProfitMqVo);
        processShareProfitList(shareProfitBOList,record);
    }

    private List<ShareProfitBO> shareProfitOperator(PlatformConfig platformConfig, ShareProfitMqVo shareProfitMqVo) {
        User user = userService.findById(shareProfitMqVo.getUserId());
        String betTime = shareProfitMqVo.getBetTime().substring(0,10);
        List<ShareProfitBO> shareProfitBOList = new ArrayList<>();
        if(user.getFirstPid()!=null)
            shareProfitBOList.add(getShareProfitBO(user.getFirstPid(),shareProfitMqVo.getValidbet(),platformConfig.getFirstCommission(),getUserIsFirstBet(user),betTime,true));
        if(user.getSecondPid()!=null)
            shareProfitBOList.add(getShareProfitBO(user.getSecondPid(),shareProfitMqVo.getValidbet(),platformConfig.getSecondCommission(),getUserIsFirstBet(user),betTime,false));
        if(user.getThirdPid()!=null)
            shareProfitBOList.add(getShareProfitBO(user.getThirdPid(),shareProfitMqVo.getValidbet(),platformConfig.getThirdCommission(),getUserIsFirstBet(user),betTime,false));
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
        return shareProfitBO;
    }

    private boolean getUserIsFirstBet(User user){
        if (user.getIsFirstBet() != null && user.getIsFirstBet() == Constants.no)
            return true;
        return false;
    }

    @Transactional
    public void processShareProfitList(List<ShareProfitBO> shareProfitBOList,GameRecord record){
        shareProfitBOList.forEach(item->processItem(item,record));
        updateShareProfitStatus(record);
    }

    public void processItem(ShareProfitBO shareProfitBO,GameRecord record){
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(shareProfitBO.getUserId());
        if(userMoney==null)return;
        //明细入库
        processProfitDetail(shareProfitBO,userMoney,record);
        //进行分润
        userMoney.setShareProfit(userMoney.getShareProfit().add(shareProfitBO.getProfitAmount()));
        userMoneyService.save(userMoney);
        //进行日报表处理
        proxyDayReportBusiness.processReport(shareProfitBO);
        //进行总报表处理
        proxyReportBusiness.processReport(shareProfitBO);
    }

    private void processProfitDetail(ShareProfitBO shareProfitBO,UserMoney userMoney,GameRecord record) {
        log.info("gamerecord is {}",record);
        ShareProfitChange shareProfitChange = new ShareProfitChange();
        shareProfitChange.setAmount(shareProfitBO.getProfitAmount());
        shareProfitChange.setUserId(shareProfitBO.getUserId());
        shareProfitChange.setOrderNo(record.getBetId());
        shareProfitChange.setAmountBefore(userMoney.getShareProfit());
        shareProfitChange.setAmountAfter(getAfterAmount(shareProfitBO,userMoney));
        shareProfitChange.setType(ShareProfitConstant.SHARE_PROFIT_TYPE);
        shareProfitChangeService.save(shareProfitChange);
    }

    private BigDecimal getAfterAmount(ShareProfitBO shareProfitBO,UserMoney userMoney){
        return userMoney.getShareProfit().add(shareProfitBO.getProfitAmount());
    }


    @Transactional
    public void shareProfit(PlatformConfig platformConfig, GameRecord record) {
        log.info("开始三级分润={}", record.toString());
        BigDecimal validbet = new BigDecimal(record.getValidbet());
//        Long userId = record.getUserId();
        if (platformConfig == null) {
            updateShareProfitStatus(record);
            return;
        }
        User user = userService.findById(record.getUserId());
        if (user == null) {
            updateShareProfitStatus(record);
            return;
        }
        Long firstPid = user.getFirstPid();
        if (firstPid == null) {
            updateShareProfitStatus(record);
            return;
        }
        UserMoney firstUser = userMoneyService.findUserByUserIdUseLock(firstPid);
        if (firstUser == null) {
            updateShareProfitStatus(record);
            return;
        }
        BigDecimal firstRate = platformConfig.getFirstCommission() == null ? BigDecimal.ZERO : platformConfig.getFirstCommission();
        BigDecimal secondRate = platformConfig.getSecondCommission() == null ? BigDecimal.ZERO : platformConfig.getSecondCommission();
        BigDecimal thirdRate = platformConfig.getThirdCommission() == null ? BigDecimal.ZERO : platformConfig.getThirdCommission();
        ShareProfitVo shareProfitVo = new ShareProfitVo();
        //查询当前用户是否是首次下注
        if (user.getIsFirstBet() != null && user.getIsFirstBet() == Constants.no) {
            shareProfitVo.setIsFirst(true);
            user.setIsFirstBet(Constants.yes);
            userService.save(user);
        }
        //一级分润
        BigDecimal firstMoney = validbet.multiply(firstRate).setScale(2, BigDecimal.ROUND_HALF_UP);
        firstMoney = firstMoney == null ? BigDecimal.ZERO : firstMoney;
        shareProfitVo.setFirstUserId(firstPid);
        shareProfitVo.setFirstMoney(firstMoney);
        if (firstMoney.compareTo(BigDecimal.ZERO) == 1) {
            userMoneyService.addShareProfit(firstPid, firstMoney);
        }
        //二级分润
        Long secondPid = user.getSecondPid();
        UserMoney secondUser = null;
        if (secondPid != null) {
            secondUser = userMoneyService.findUserByUserIdUseLock(secondPid);
        }
        if (secondUser != null) {
            BigDecimal secondMoney = validbet.multiply(secondRate).setScale(2, BigDecimal.ROUND_HALF_UP);
            secondMoney = secondMoney == null ? BigDecimal.ZERO : secondMoney;
            shareProfitVo.setSecondUserId(secondPid);
            shareProfitVo.setSecondMoney(secondMoney);
            if (secondMoney.compareTo(BigDecimal.ZERO) == 1) {
                userMoneyService.addShareProfit(secondPid, secondMoney);
            }
        }
        //三级分润
        Long thirdPid = user.getThirdPid();
        UserMoney thirdUser = null;
        if (thirdPid != null) {
            thirdUser = userMoneyService.findUserByUserIdUseLock(thirdPid);
        }
        if (thirdUser != null) {
            BigDecimal thirdMoney = validbet.multiply(thirdRate).setScale(2, BigDecimal.ROUND_HALF_UP);
            thirdMoney = thirdMoney == null ? BigDecimal.ZERO : thirdMoney;
            shareProfitVo.setThirdUserId(thirdPid);
            shareProfitVo.setThirdMoney(thirdMoney);
            if (thirdMoney.compareTo(BigDecimal.ZERO) == 1) {
                userMoneyService.addShareProfit(thirdPid, thirdMoney);
            }
        }
        updateShareProfitStatus(record);
//        rabbitTemplate.convertAndSend(RabbitMqConstants.SHAREPROFIT_DIRECTQUEUE_DIRECTEXCHANGE, RabbitMqConstants.SHAREPROFIT_DIRECT, shareProfitVo, new CorrelationData(UUID.randomUUID().toString()));
        log.info("分润消息发送成功={}", shareProfitVo);
    }

    /**
     * 更新分润状态
     *
     * @param record
     */
    public void updateShareProfitStatus(GameRecord record) {
        record.setShareProfitStatus(Constants.yes);
        gameRecordService.save(record);
    }
}
