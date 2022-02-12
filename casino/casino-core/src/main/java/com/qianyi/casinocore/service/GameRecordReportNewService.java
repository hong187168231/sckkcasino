package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordEndIndex;
import com.qianyi.casinocore.model.GameRecordReportNew;
import com.qianyi.casinocore.repository.GameRecordReportNewRepository;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GameRecordReportNewService {
    @Autowired
    private GameRecordReportNewRepository gameRecordReport01Repository;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    private GameRecordEndIndexService gameRecordEndIndexService;

    @Transactional
    public void saveGameRecordReportWM(){
        GameRecordEndIndex first = gameRecordEndIndexService.findFirst();
        if (first == null){
            return;
        }
        log.info("得到wm注单下标{}",first.getGameRecordId());
        List<Map<String, Object>> reportResult = gameRecordService.queryGameRecords(first.getGameRecordId(), 13);
        try {
            if (reportResult == null || reportResult.size() == CommonConst.NUMBER_0){
                return;
            }
            if (reportResult.get(0).get("num") == null || Integer.parseInt(reportResult.get(0).get("num").toString()) == CommonConst.NUMBER_0){
                return;
            }
            Long max = 0L;
            for (Map<String, Object> map:reportResult){
                GameRecordReportNew gameRecordReport = new GameRecordReportNew();
                Long maxId= Long.parseLong(map.get("maxId").toString());
                if (maxId > max)
                    max = maxId;
                gameRecordReport.setStaticsTimes(map.get("set_time").toString());
                gameRecordReport.setAmount(new BigDecimal(map.get("amount").toString()));
                gameRecordReport.setBetAmount(new BigDecimal(map.get("bet").toString()));
                gameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                gameRecordReport.setWinLossAmount(new BigDecimal(map.get("win_loss").toString()));
                gameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                gameRecordReport.setPlatform("WM");
                gameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
                gameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
                gameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
                gameRecordReport.setGameRecordReportId(CommonUtil.toHash(gameRecordReport.getStaticsTimes()+gameRecordReport.getThirdProxy()+gameRecordReport.getPlatform()));
                gameRecordReport01Repository.updateKey(gameRecordReport.getGameRecordReportId(),gameRecordReport.getStaticsTimes(),
                    gameRecordReport.getBetAmount(),gameRecordReport.getValidAmount(),gameRecordReport.getWinLossAmount(),gameRecordReport.getAmount(),
                    gameRecordReport.getBettingNumber(),gameRecordReport.getFirstProxy(),gameRecordReport.getSecondProxy(),gameRecordReport.getThirdProxy(),gameRecordReport.getPlatform());
            }
            first.setGameRecordId(max);
            log.info("保存wm注单下标{}",first.getGameRecordId());
            gameRecordEndIndexService.save(first);
        }catch (Exception ex){
            log.error("每小时报表统计失败",ex);
        }
    }
    @Transactional
    public void saveGameRecordReportPG(){
        GameRecordEndIndex first = gameRecordEndIndexService.findFirst();
        if (first == null){
            return;
        }
        log.info("得到PG注单下标{}",first.getPGMaxId());
        List<Map<String, Object>> reportResult = gameRecordGoldenFService.queryGameRecords(first.getPGMaxId(), 13,"PG");
        try {
            if (reportResult == null || reportResult.size() == CommonConst.NUMBER_0){
                return;
            }
            if (reportResult.get(0).get("num") == null || Integer.parseInt(reportResult.get(0).get("num").toString()) == CommonConst.NUMBER_0){
                return;
            }
            Long max = 0L;
            for (Map<String, Object> map:reportResult){
                GameRecordReportNew gameRecordReport = new GameRecordReportNew();
                Long maxId= Long.parseLong(map.get("maxId").toString());
                if (maxId > max)
                    max = maxId;
                gameRecordReport.setStaticsTimes(map.get("set_time").toString());
                gameRecordReport.setAmount(new BigDecimal(map.get("amount").toString()));
                gameRecordReport.setBetAmount(new BigDecimal(map.get("bet").toString()));
                gameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                gameRecordReport.setWinLossAmount(new BigDecimal(map.get("win_loss").toString()).subtract(gameRecordReport.getBetAmount()));
                gameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                gameRecordReport.setPlatform("PG");
                gameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
                gameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
                gameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
                gameRecordReport.setGameRecordReportId(CommonUtil.toHash(gameRecordReport.getStaticsTimes()+gameRecordReport.getThirdProxy()+gameRecordReport.getPlatform()));
                gameRecordReport01Repository.updateKey(gameRecordReport.getGameRecordReportId(),gameRecordReport.getStaticsTimes(),
                    gameRecordReport.getBetAmount(),gameRecordReport.getValidAmount(),gameRecordReport.getWinLossAmount(),gameRecordReport.getAmount(),
                    gameRecordReport.getBettingNumber(),gameRecordReport.getFirstProxy(),gameRecordReport.getSecondProxy(),gameRecordReport.getThirdProxy(),gameRecordReport.getPlatform());
            }
            first.setPGMaxId(max);
            log.info("保存PG注单下标{}",first.getPGMaxId());
            gameRecordEndIndexService.save(first);
        }catch (Exception ex){
            log.error("每小时PG报表统计失败",ex);
        }
    }
    @Transactional
    public void saveGameRecordReportCQ9(){
        GameRecordEndIndex first = gameRecordEndIndexService.findFirst();
        if (first == null){
            return;
        }
        log.info("得到CQ9注单下标{}",first.getCQ9MaxId());
        List<Map<String, Object>> reportResult = gameRecordGoldenFService.queryGameRecords(first.getCQ9MaxId(), 13,"CQ9");
        try {
            if (reportResult == null || reportResult.size() == CommonConst.NUMBER_0){
                return;
            }
            if (reportResult.get(0).get("num") == null || Integer.parseInt(reportResult.get(0).get("num").toString()) == CommonConst.NUMBER_0){
                return;
            }
            Long max = 0L;
            for (Map<String, Object> map:reportResult){
                GameRecordReportNew gameRecordReport = new GameRecordReportNew();
                Long maxId= Long.parseLong(map.get("maxId").toString());
                if (maxId > max)
                    max = maxId;
                gameRecordReport.setStaticsTimes(map.get("set_time").toString());
                gameRecordReport.setAmount(new BigDecimal(map.get("amount").toString()));
                gameRecordReport.setBetAmount(new BigDecimal(map.get("bet").toString()));
                gameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                gameRecordReport.setWinLossAmount(new BigDecimal(map.get("win_loss").toString()).subtract(gameRecordReport.getBetAmount()));
                gameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                gameRecordReport.setPlatform("CQ9");
                gameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
                gameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
                gameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
                gameRecordReport.setGameRecordReportId(CommonUtil.toHash(gameRecordReport.getStaticsTimes()+gameRecordReport.getThirdProxy()+gameRecordReport.getPlatform()));
                gameRecordReport01Repository.updateKey(gameRecordReport.getGameRecordReportId(),gameRecordReport.getStaticsTimes(),
                    gameRecordReport.getBetAmount(),gameRecordReport.getValidAmount(),gameRecordReport.getWinLossAmount(),gameRecordReport.getAmount(),
                    gameRecordReport.getBettingNumber(),gameRecordReport.getFirstProxy(),gameRecordReport.getSecondProxy(),gameRecordReport.getThirdProxy(),gameRecordReport.getPlatform());
            }
            first.setCQ9MaxId(max);
            log.info("保存CQ9注单下标{}",first.getCQ9MaxId());
            gameRecordEndIndexService.save(first);
        }catch (Exception ex){
            log.error("每小时CQ9报表统计失败",ex);
        }
    }
}
