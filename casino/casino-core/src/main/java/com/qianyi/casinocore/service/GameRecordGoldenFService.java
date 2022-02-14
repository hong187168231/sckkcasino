package com.qianyi.casinocore.service;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.model.GameRecordGoldenF;
import com.qianyi.casinocore.repository.GameRecordGoldenFRepository;
import com.qianyi.casinocore.vo.CompanyOrderAmountVo;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

@Service
public class GameRecordGoldenFService {

    @Autowired
    private GameRecordGoldenFRepository gameRecordGoldenFRepository;

    public void updateCodeNumStatus(Long id,Integer codeNumStatus){
        gameRecordGoldenFRepository.updateCodeNumStatus(id,codeNumStatus);
    }

    public void updateWashCodeStatus(Long id,Integer washCodeStatus){
        gameRecordGoldenFRepository.updateWashCodeStatus(id,washCodeStatus);
    }

    public List<Map<String, Object>> findSumBetAmount(String startTime, String endTime){
        return gameRecordGoldenFRepository.findSumBetAmount(startTime,endTime);
    }

    public Set<Long> findGroupByUser(String startTime,String endTime){
        return gameRecordGoldenFRepository.findGroupByUser(startTime,endTime);
    }

    public Map<String, Object> findSumBetAndWinLoss(String startTime,String endTime){
        return gameRecordGoldenFRepository.findSumBetAndWinLoss(startTime,endTime);
    }

    public void updateProfitStatus(Long id,Integer shareProfitStatus){
        gameRecordGoldenFRepository.updateProfitStatus(id,shareProfitStatus);
    }

    public List<CompanyOrderAmountVo> getStatisticsResult(String startTime, String endTime){
        List<Map<String,Object>> orderAmountVoList = gameRecordGoldenFRepository.getStatisticsResult(startTime,endTime);
        String json = JSON.toJSONString(orderAmountVoList);
        return JSON.parseArray(json,CompanyOrderAmountVo.class);
    }

    public List<Map<String,Object>> queryGameRecords(Long id,Integer num,String platform){
        return gameRecordGoldenFRepository.queryGameRecords(id,num,platform);
    }

    public BigDecimal findSumBetAmount(Long userId,String startTime,String endTime){
        return gameRecordGoldenFRepository.findSumBetAmount(userId,startTime,endTime);
    }

    public Set<Long> findGroupByFirst(String startTime,String endTime,Long firstProxy){
        return gameRecordGoldenFRepository.findGroupByFirst(startTime,endTime,firstProxy);
    }

    public Map<String, Object> findSumBetAndWinLossByFirst(String startTime,String endTime,Long firstProxy){
        return gameRecordGoldenFRepository.findSumBetAndWinLossByFirst(startTime,endTime,firstProxy);
    }

    public Set<Long> findGroupBySecond(String startTime,String endTime,Long firstProxy){
        return gameRecordGoldenFRepository.findGroupBySecond(startTime,endTime,firstProxy);
    }

    public Map<String, Object> findSumBetAndWinLossBySecond(String startTime,String endTime,Long firstProxy){
        return gameRecordGoldenFRepository.findSumBetAndWinLossBySecond(startTime,endTime,firstProxy);
    }

    public Set<Long> findGroupByThird(String startTime,String endTime,Long firstProxy){
        return gameRecordGoldenFRepository.findGroupByThird(startTime,endTime,firstProxy);
    }

    public Map<String, Object> findSumBetAndWinLossByThird(String startTime,String endTime,Long firstProxy){
        return gameRecordGoldenFRepository.findSumBetAndWinLossByThird(startTime,endTime,firstProxy);
    }

    public int countByIdLessThanEqualAndUserId(Date createTime, Long UserId){
        return gameRecordGoldenFRepository.countByIdLessThanEqualAndUserId(createTime,UserId);
    }

    public GameRecordGoldenF findGameRecordById(Long gameId){return gameRecordGoldenFRepository.findById(gameId).orElse(null);}


}
