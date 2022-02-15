package com.qianyi.casinoweb.job;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinoweb.util.DateUtil;
import com.qianyi.casinoweb.vo.GameRecordObj;
import com.qianyi.livegoldenf.api.PublicGoldenFApi;
import com.qianyi.modulecommon.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class GameRecordGoldenFJob {

    @Autowired
    private PublicGoldenFApi publicGoldenFApi;

    @Autowired
    private UserThirdService userThirdService;
    @Autowired
    private UserService userService;

    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    private GameRecordGoldenfEndTimeService gameRecordGoldenfEndTimeService;

    @Autowired
    PlatformConfigService platformConfigService;

    @Autowired
    AdGamesService adGamesService;

    @Autowired
    private GameRecordAsyncOper gameRecordAsyncOper;
    @Autowired
    private PlatformGameService platformGameService;

    public void pullGoldenF(){
        pullGameRecord(Constants.PLATFORM_PG);
        pullGameRecord(Constants.PLATFORM_CQ9);
    }


    private void pullGameRecord(String vendorCode){

        //从数据库获取最近的拉单时间和平台
        // 获取 starttime
//        Long startTime = 1644588300000l;
//        Long endTime = 1644588600000l;
        Long startTime = getGoldenStartTime(vendorCode);

        Long endTime = getGoldenEndTime(startTime);
        log.info("startime is {}  endtime is {}",startTime,endTime);
        Integer failCount = 0;

        int page =1;

        int pageSize=1000;

        while (true){
            // 获取数据
            PublicGoldenFApi.ResponseEntity responseEntity = publicGoldenFApi.getPlayerGameRecord(startTime, endTime, vendorCode, page, pageSize);

            if(checkRequestFail(responseEntity)){
                processFaildRequest(startTime,endTime,vendorCode,responseEntity);
                if(failCount>=2) break;
                failCount++;
                continue;
            }
            if(saveData(responseEntity))
                break;
            page++;
        }
        processSuccessRequest(startTime,endTime,vendorCode);
    }

    private Long getGoldenStartTime(String vendor) {
        GameRecordGoldenfEndTime gameRecordGoldenfEndTime = gameRecordGoldenfEndTimeService.findFirstByVendorCodeOrderByEndTimeDesc(vendor);
        Long startTime = 0l;
        if(gameRecordGoldenfEndTime==null){
            startTime = DateUtil.next5MinuteTime();
        }else
            startTime = gameRecordGoldenfEndTime.getEndTime();
        return startTime*1000;
    }

    private Long getGoldenEndTime(Long startTime){
        return startTime+(5*60*1000);
    }

    private void processSuccessRequest(Long startTime,Long endTime,String vendorCode) {
        GameRecordGoldenfEndTime gameRecordGoldenfEndTime = new GameRecordGoldenfEndTime();
        gameRecordGoldenfEndTime.setStartTime(startTime/1000);
        gameRecordGoldenfEndTime.setEndTime(endTime/1000);
        gameRecordGoldenfEndTime.setVendorCode(vendorCode);
        gameRecordGoldenfEndTimeService.save(gameRecordGoldenfEndTime);
    }

    private void processFaildRequest(Long startTime,Long endTime,String vendorCode,PublicGoldenFApi.ResponseEntity responseEntity) {

    }

    private boolean checkRequestFail(PublicGoldenFApi.ResponseEntity responseEntity) {
        return responseEntity.getErrorCode()!=null;
    }

    private Boolean saveData(PublicGoldenFApi.ResponseEntity responseEntity) {
        GameRecordObj gameRecordObj = JSON.parseObject(responseEntity.getData(), GameRecordObj.class);

        List<GameRecordGoldenF> recordGoldenFS = gameRecordObj.getBetlogs();
        processRecords(recordGoldenFS);

        return gameRecordObj.getPage() == gameRecordObj.getPageCount();
    }

    private void processRecords(List<GameRecordGoldenF> recordGoldenFS) {
        PlatformConfig platformConfig = platformConfigService.findFirst();
        recordGoldenFS.forEach(item->{
            UserThird userThird = userThirdService.findByGoldenfAccount(item.getPlayerName());
            User user = userService.findById(userThird.getUserId());
            item.setUserId(userThird.getUserId());
            item.setFirstProxy(user.getFirstProxy());
            item.setSecondProxy(user.getSecondProxy());
            item.setThirdProxy(user.getThirdProxy());
            if(item.getCreatedAt()==null)
                log.info("{}",item);
            item.setCreateAtStr(DateUtil.timeStamp2Date(item.getCreatedAt(),""));
            GameRecord gameRecord = combineGameRecord(item);
            GameRecordGoldenF gameRecordGoldenF = gameRecordGoldenFService.findGameRecordGoldenFByTraceId(item.getTraceId());
            if(gameRecordGoldenF==null)
                gameRecordGoldenFService.save(item);

            processBusiness(item,gameRecord,platformConfig);
        });
    }

    private void processBusiness(GameRecordGoldenF gameRecordGoldenF,GameRecord gameRecord, PlatformConfig platformConfig) {
        //洗码
        gameRecordAsyncOper.washCode(gameRecordGoldenF.getVendorCode(), gameRecord);
        //扣减打码量
        gameRecordAsyncOper.subCodeNum(gameRecordGoldenF.getVendorCode(),platformConfig, gameRecord);
        //代理分润
        gameRecordAsyncOper.shareProfit(gameRecordGoldenF.getVendorCode(),gameRecord);
    }


    private GameRecord combineGameRecord(GameRecordGoldenF item) {
        PlatformGame platformGame = platformGameService.findByGamePlatformName(item.getVendorCode());
        List<AdGame> adGameList = adGamesService.findByGameCode(item.getGameCode());
        GameRecord gameRecord = new GameRecord();
        gameRecord.setBetId(item.getBetId());
        gameRecord.setValidbet(item.getBetAmount().toString());
        gameRecord.setUserId(item.getUserId());
        gameRecord.setGid(platformGame.getGamePlatformId());
        gameRecord.setGname(adGameList.size()>0?adGameList.get(1).getGameName():null);
        return gameRecord;
    }


}
