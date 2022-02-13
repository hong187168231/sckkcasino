package com.qianyi.casinocore.service;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.repository.GameRecordGoldenFRepository;
import com.qianyi.casinocore.vo.CompanyOrderAmountVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
}
