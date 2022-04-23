package com.qianyi.casinocore.service;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.model.GameRecordObty;
import com.qianyi.casinocore.repository.GameRecordObtyRepository;
import com.qianyi.casinocore.vo.CompanyOrderAmountVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class GameRecordObtyService {
    @Autowired
    private GameRecordObtyRepository gameRecordObtyRepository;

    public List<CompanyOrderAmountVo> getStatisticsResult(String startTime, String endTime) {
        List<Map<String,Object>> orderAmountVoList = gameRecordObtyRepository.getStatisticsResult(startTime,endTime);
        String json = JSON.toJSONString(orderAmountVoList);
        return JSON.parseArray(json,CompanyOrderAmountVo.class);

    }

 public int countByIdLessThanEqualAndUserId(Date createTime, Long userId) {
        return gameRecordObtyRepository.countByIdLessThanEqualAndUserId(createTime,userId);
    }


    public void updateCodeNumStatus(Long id, Integer codeNumStatus) {
        gameRecordObtyRepository.updateCodeNumStatus(id, codeNumStatus);
    }

    public void updateWashCodeStatus(Long id, Integer washCodeStatus) {
        gameRecordObtyRepository.updateWashCodeStatus(id, washCodeStatus);
    }

    public void updateRebateStatus(Long id, Integer rebateStatus) {
        gameRecordObtyRepository.updateRebateStatus(id, rebateStatus);
    }

    public void updateGameRecordStatus(Long id, Integer gameRecordStatus) {
        gameRecordObtyRepository.updateGameRecordStatus(id, gameRecordStatus);
    }

    public void updateProfitStatus(Long id, Integer shareProfitStatus) {
        gameRecordObtyRepository.updateProfitStatus(id, shareProfitStatus);
    }

    public void updateExtractStatus(Long id, Integer extractStatus) {
        gameRecordObtyRepository.updateExtractStatus(id,extractStatus);
    }

    public List<Map<String,Object>> queryGameRecords(Long id,Integer num){
        return gameRecordObtyRepository.queryGameRecords(id,num);
    }

    public GameRecordObty findGameRecordById(Long gameId){return gameRecordObtyRepository.findById(gameId).orElse(null);}

    public GameRecordObty save(GameRecordObty gameRecord) {
        return gameRecordObtyRepository.save(gameRecord);
    }
}
