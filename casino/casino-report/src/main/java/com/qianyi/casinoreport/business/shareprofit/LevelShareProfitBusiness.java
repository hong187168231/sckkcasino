package com.qianyi.casinoreport.business.shareprofit;

import com.qianyi.casinocore.model.ConsumerError;
import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.ShareProfitBO;
import com.qianyi.casinocore.vo.ShareProfitMqVo;
import com.qianyi.casinoreport.util.ReportConstant;
import com.qianyi.casinoreport.util.ShareProfitUtils;
import com.qianyi.modulecommon.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LevelShareProfitBusiness {

    @Autowired
    private UserService userService;
    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private PlatformConfigService platformConfigService;

    @Autowired
    private ConsumerErrorService consumerErrorService;

    @Autowired
    private LevelShareProfitTransactionService shareProfitTransactionService;

    /**
     * 处理分润
     * @param shareProfitMqVo
     */
    public void procerssShareProfit(ShareProfitMqVo shareProfitMqVo){
        try {
            //根据游戏id、分润状态查询游戏记录(避免同一个游戏id执行多次)
            GameRecord record = gameRecordService.findGameRecordByIdAndShareProfitStatusIsNull(shareProfitMqVo.getGameRecordId());
            if (record!=null){
                PlatformConfig platformConfig = platformConfigService.findFirst();
                List<ShareProfitBO> shareProfitBOList = shareProfitOperator(platformConfig, shareProfitMqVo,record);
                shareProfitTransactionService.processShareProfitMq(shareProfitBOList);
            }
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

    /**
     * 各级代理数据组装
     * @param platformConfig
     * @param shareProfitMqVo
     * @param record
     * @return
     */
    private List<ShareProfitBO> shareProfitOperator(PlatformConfig platformConfig, ShareProfitMqVo shareProfitMqVo, GameRecord record) {
        Long startTime = System.currentTimeMillis();
        User user = userService.findById(shareProfitMqVo.getUserId());
        log.info("shareProfitOperator user:{}",user);
        String betTime = shareProfitMqVo.getBetTime().substring(0,10);
        List<ShareProfitBO> shareProfitBOList = new ArrayList<>();
        if(ShareProfitUtils.compareIntegerNotNull( user.getFirstPid()))
            shareProfitBOList.add(getShareProfitBO(user,user.getFirstPid(),shareProfitMqVo.getValidbet(),platformConfig.getFirstCommission(),getUserIsFirstBet(user),betTime,true,1,record.getUserId(),shareProfitMqVo.getGameRecordId(),record.getBetId()));
        if(ShareProfitUtils.compareIntegerNotNull( user.getSecondPid()))
            shareProfitBOList.add(getShareProfitBO(user,user.getSecondPid(),shareProfitMqVo.getValidbet(),platformConfig.getSecondCommission(),getUserIsFirstBet(user),betTime,false,2,record.getUserId(),shareProfitMqVo.getGameRecordId(),record.getBetId()));
        if(ShareProfitUtils.compareIntegerNotNull( user.getThirdPid()))
            shareProfitBOList.add(getShareProfitBO(user,user.getThirdPid(),shareProfitMqVo.getValidbet(),platformConfig.getThirdCommission(),getUserIsFirstBet(user),betTime,false,3,record.getUserId(),shareProfitMqVo.getGameRecordId(),record.getBetId()));
        log.info("get list object is {}",shareProfitBOList);
        log.info("shareProfitOperator That took {} milliseconds",System.currentTimeMillis()-startTime);
        return shareProfitBOList;
    }

    private ShareProfitBO getShareProfitBO(User user,Long userId,BigDecimal betAmount,BigDecimal commission,
                                           Boolean isFirst,String betTime,boolean direct,Integer parentLevel,Long recordUserId,Long recordId,String recordBetId){
        ShareProfitBO shareProfitBO = new ShareProfitBO();
        shareProfitBO.setFromUserId(user.getId());
        shareProfitBO.setUserId(userId);
        shareProfitBO.setBetAmount(betAmount);
        shareProfitBO.setProfitAmount(betAmount.multiply(commission.divide(BigDecimal.valueOf(100))));
        shareProfitBO.setFirst(isFirst);
        shareProfitBO.setBetTime(betTime);
        shareProfitBO.setDirect(direct);
        shareProfitBO.setCommission(commission);
        shareProfitBO.setParentLevel(parentLevel);
        shareProfitBO.setRecordUserId(recordUserId);
        shareProfitBO.setRecordBetId(recordBetId);
        shareProfitBO.setRecordId(recordId);
        log.info("user:{} \\n shareProfitBO{}",user,shareProfitBO);
        return shareProfitBO;
    }

    private boolean getUserIsFirstBet(User user){
        if (user.getIsFirstBet() != null && user.getIsFirstBet() == Constants.no)
            return true;
        return false;
    }
}
