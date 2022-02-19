package com.qianyi.casinoreport.business.shareprofit;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.ShareProfitBO;
import com.qianyi.casinocore.vo.ShareProfitMqVo;
import com.qianyi.casinoreport.util.ReportConstant;
import com.qianyi.casinoreport.util.ShareProfitUtils;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class LevelShareProfitBusiness {

    @Autowired
    private UserService userService;
    @Autowired
    private GameRecordService gameRecordService;


    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    private PlatformConfigService platformConfigService;
    @Autowired
    private PromoteCommissionConfigService promoteCommissionConfigService;

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
            Integer gameType=0;
            GameRecord record=new GameRecord();
            if (!shareProfitMqVo.getPlatform().equals("wm")){
                GameRecordGoldenF recordGoldenF = gameRecordGoldenFService.findGameRecordById(shareProfitMqVo.getGameRecordId());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = simpleDateFormat.parse(recordGoldenF.getCreateAtStr());

                GameRecord gameRecord=new GameRecord();
                gameRecord.setCreateTime(date);
                gameRecord.setBetId(recordGoldenF.getBetId());
                gameRecord.setUserId(recordGoldenF.getUserId());
                gameRecord.setValidbet(recordGoldenF.getBetAmount().toString());
                record=gameRecord;
                if (shareProfitMqVo.getPlatform().equals("PG")){
                    gameType=2;
                }
                if (shareProfitMqVo.getPlatform().equals("CQ9")){
                    gameType=3;
                }
            }else {
                gameType=1;
                record = gameRecordService.findGameRecordById(shareProfitMqVo.getGameRecordId());
            }


            //查询代理推广配置
            PromoteCommissionConfig byGameType = promoteCommissionConfigService.findByGameType(gameType);
            List<ShareProfitBO> shareProfitBOList = shareProfitOperator(byGameType, shareProfitMqVo,record);
            shareProfitTransactionService.processShareProfitMq(shareProfitBOList);
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
     * @param byGameType
     * @param shareProfitMqVo
     * @param record
     * @return
     */
    private List<ShareProfitBO> shareProfitOperator( PromoteCommissionConfig byGameType , ShareProfitMqVo shareProfitMqVo, GameRecord record) {
        Long startTime = System.currentTimeMillis();
        User user = userService.findById(shareProfitMqVo.getUserId());
        log.info("shareProfitOperator user:{}",user);
        String betTime = shareProfitMqVo.getBetTime().substring(0,10);
        List<ShareProfitBO> shareProfitBOList = new ArrayList<>();
        if(ShareProfitUtils.compareIntegerNotNull( user.getFirstPid()))
            shareProfitBOList.add(getShareProfitBO(user,user.getFirstPid(),new BigDecimal(record.getValidbet()),byGameType.getFirstCommission(),byGameType.getGameType(),getUserIsFirstBet(record,user.getId()),betTime,shareProfitMqVo.getBetTime(),true,1,record.getUserId(),shareProfitMqVo.getGameRecordId(),record.getBetId()));
        if(ShareProfitUtils.compareIntegerNotNull( user.getSecondPid()))
            shareProfitBOList.add(getShareProfitBO(user,user.getSecondPid(),new BigDecimal(record.getValidbet()),byGameType.getSecondCommission(),byGameType.getGameType(),getUserIsFirstBet(record,user.getId()),betTime,shareProfitMqVo.getBetTime(),false,2,record.getUserId(),shareProfitMqVo.getGameRecordId(),record.getBetId()));
        if(ShareProfitUtils.compareIntegerNotNull( user.getThirdPid()))
            shareProfitBOList.add(getShareProfitBO(user,user.getThirdPid(),new BigDecimal(record.getValidbet()),byGameType.getThirdCommission(),byGameType.getGameType(),getUserIsFirstBet(record,user.getId()),betTime,shareProfitMqVo.getBetTime(),false,3,record.getUserId(),shareProfitMqVo.getGameRecordId(),record.getBetId()));
        log.info("get list object is {}",shareProfitBOList);
        log.info("shareProfitOperator That took {} milliseconds",System.currentTimeMillis()-startTime);
        return shareProfitBOList;
    }

    private ShareProfitBO getShareProfitBO(User user,Long userId,BigDecimal betAmount,BigDecimal commission,Integer gameType,
                                           Boolean isFirst,String betTime,String betDate,boolean direct,Integer parentLevel,Long recordUserId,Long recordId,String recordBetId){
        ShareProfitBO shareProfitBO = new ShareProfitBO();
        shareProfitBO.setFromUserId(user.getId());
        shareProfitBO.setUserId(userId);
        shareProfitBO.setBetAmount(betAmount);
        shareProfitBO.setProfitAmount(betAmount.multiply(commission.divide(BigDecimal.valueOf(100))));
        shareProfitBO.setFirst(isFirst);
        shareProfitBO.setBetTime(betTime);
        Date startDate = null;
        try {
            startDate = DateUtil.getSimpleDateFormat().parse(betDate);
            shareProfitBO.setBetDate(startDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        shareProfitBO.setDirect(direct);
        shareProfitBO.setCommission(commission);
        shareProfitBO.setParentLevel(parentLevel);
        shareProfitBO.setRecordUserId(recordUserId);
        shareProfitBO.setRecordBetId(recordBetId);
        shareProfitBO.setRecordId(recordId);
        shareProfitBO.setGameType(gameType);
        log.info("user:{} \\n shareProfitBO{}",user,shareProfitBO);
        return shareProfitBO;
    }

    private boolean getUserIsFirstBet(GameRecord record,Long userId){
        //根据game_record表来判断是否是第一次下注
        int amount=0;
       int recordAmount= gameRecordService.countByIdLessThanEqualAndUserId(record.getCreateTime(),userId);
       int goldenFAmount= gameRecordGoldenFService.countByIdLessThanEqualAndUserId(record.getCreateTime(),userId);
        amount=recordAmount+goldenFAmount;
        if (amount==Constants.yes){
            return true;
        }
        return false;
    }
}
