package com.qianyi.casinocore.service;

import cn.hutool.core.collection.CollUtil;
import com.qianyi.casinocore.model.ProxyGameRecordReport;
import com.qianyi.casinocore.repository.ProxyGameRecordReportRepository;
import com.qianyi.casinocore.util.CommonUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

@Service
@Slf4j
public class ProxyGameRecordReportService {

    @Autowired
    private ProxyGameRecordReportRepository proxyGameRecordReportRepository;

    @Autowired
    private UserGameRecordReportService userGameRecordReportService;

    public final static String start = " 12:00:00";

    public final static String end = " 11:59:59";

    public List<ProxyGameRecordReport> findAll(){
        return proxyGameRecordReportRepository.findAll();
    }

    public ProxyGameRecordReport save(ProxyGameRecordReport proxyGameRecordReport){
        return proxyGameRecordReportRepository.save(proxyGameRecordReport);
    }

    @Transactional
    public void updateKey(Long gameRecordReportId,Long userId,String orderTimes, BigDecimal validAmount,BigDecimal winLoss,Long firstProxy,Long secondProxy,Long thirdProxy,BigDecimal betAmount){
        proxyGameRecordReportRepository.updateKey(gameRecordReportId,userId,orderTimes,validAmount,winLoss,firstProxy,secondProxy,thirdProxy,betAmount);
    }

    @Transactional
    public void updateBet(Long gameRecordReportId,Long userId,String orderTimes, BigDecimal validAmount,BigDecimal winLoss,Long firstProxy,Long secondProxy,Long thirdProxy,BigDecimal betAmount){
        proxyGameRecordReportRepository.updateBet(gameRecordReportId,userId,orderTimes,validAmount,winLoss,firstProxy,secondProxy,thirdProxy,betAmount);
    }

    public Map<String, Object> findSumBetAndWinLossByFirst(String startTime,String endTime,Long firstProxy){
        return proxyGameRecordReportRepository.findSumBetAndWinLossByFirst(startTime,endTime,firstProxy);
    }

    public Map<String, Object> findSumBetAndWinLossBySecond(String startTime,String endTime,Long secondProxy){
        return proxyGameRecordReportRepository.findSumBetAndWinLossBySecond(startTime,endTime,secondProxy);
    }

    public Map<String, Object> findSumBetAndWinLossByThird(String startTime,String endTime,Long thirdProxy){
        return proxyGameRecordReportRepository.findSumBetAndWinLossByThird(startTime,endTime,thirdProxy);
    }

    public Map<String, Object> findSumBetAndWinLossByFirst(Long firstProxy){
        return proxyGameRecordReportRepository.findSumBetAndWinLossByFirst(firstProxy);
    }

    public Map<String, Object> findSumBetAndWinLossBySecond(Long secondProxy){
        return proxyGameRecordReportRepository.findSumBetAndWinLossBySecond(secondProxy);
    }

    public Map<String, Object> findSumBetAndWinLossByThird(Long thirdProxy) {
        return proxyGameRecordReportRepository.findSumBetAndWinLossByThird(thirdProxy);
    }

    public Map<String, Object> findSumBetAndWinLoss(String startTime,String endTime){
        return proxyGameRecordReportRepository.findSumBetAndWinLoss(startTime,endTime);
    }

    public Map<String, Object> findSumBetAndWinLoss(){
        return proxyGameRecordReportRepository.findSumBetAndWinLoss();
    }

    public List<Map<String, Object>> findBetAndWinLoss(String startTime,String endTime){
        return proxyGameRecordReportRepository.findBetAndWinLoss(startTime,endTime);
    }

    @Transactional
    public void comparison(String dayTime) {// dayTime为一天yyyy-MM-dd
        String startTime = dayTime + start;
        Date date = null;
        try {
            date = DateUtil.getDate(dayTime);
        } catch (ParseException e) {
            log.error("时间格式错误{}",dayTime);
            e.printStackTrace();
        }
        Calendar nowTime = Calendar.getInstance();
        nowTime.setTime(date );
        nowTime.add(Calendar.DATE, 1);
        String tomorrow = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        String endTime = tomorrow + end;
        Integer betNumber = proxyGameRecordReportRepository.findBetNumber(dayTime, dayTime);
        Integer totalBetNumber = userGameRecordReportService.findTotalBetNumber(startTime, endTime);
        Integer totalBetNumberByAe = userGameRecordReportService.findTotalBetNumberByAe(startTime, endTime);
        totalBetNumber = totalBetNumber + totalBetNumberByAe;
        if (betNumber.intValue() != totalBetNumber.intValue()) {
            log.error("代理报表日期{}不相等开始重新计算betNumber:{}totalBetNumber:{}", dayTime, betNumber, totalBetNumber);
            proxyGameRecordReportRepository.deleteByOrderTimes(dayTime);
            List<Map<String, Object>> totalMap = proxyGameRecordReportRepository.findTotal(startTime, endTime);
            try {
                if (CollUtil.isNotEmpty(totalMap)) {
                    totalMap.forEach(map -> {
                        ProxyGameRecordReport proxyGameRecordReport = new ProxyGameRecordReport();
                        proxyGameRecordReport.setOrderTimes(dayTime);
                        proxyGameRecordReport.setUserId(Long.parseLong(map.get("user_id").toString()));
                        if (Objects.nonNull(map.get("first_proxy")) && StringUtils.hasLength(map.get("first_proxy").toString())){
                            proxyGameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
                        }else {
                            proxyGameRecordReport.setFirstProxy(0L);
                        }
                        if (Objects.nonNull(map.get("second_proxy")) && StringUtils.hasLength(map.get("second_proxy").toString())){
                            proxyGameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
                        }else {
                            proxyGameRecordReport.setSecondProxy(0L);
                        }
                        if (Objects.nonNull(map.get("third_proxy")) && StringUtils.hasLength(map.get("third_proxy").toString())){
                            proxyGameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
                        }else {
                            proxyGameRecordReport.setThirdProxy(0L);
                        }
                        proxyGameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                        proxyGameRecordReport.setBetAmount(new BigDecimal(map.get("bet_amount").toString()));
                        proxyGameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                        proxyGameRecordReport.setWinLoss(new BigDecimal(map.get("win_loss").toString()));
                        Long proxyGameRecordReportId =  CommonUtil.toHash(dayTime+proxyGameRecordReport.getUserId().toString());
                        proxyGameRecordReport.setProxyGameRecordReportId(proxyGameRecordReportId);
                        proxyGameRecordReport.setCreateTime(new Date());
                        proxyGameRecordReport.setUpdateTime(new Date());
                        this.save(proxyGameRecordReport);
                    });
                    totalMap.clear();
                }
            } catch (Exception ex) {
                log.error("代理报表计算失败日期{}", dayTime);
            }
        }
    }

    @Transactional
    public void deleteByOrderTimes(String startTime,String endTime){
        proxyGameRecordReportRepository.deleteByOrderTimes(startTime,endTime);
    }

    @Transactional
    public void deleteByOrderTimes(String orderTimes){
        proxyGameRecordReportRepository.deleteByOrderTimes(orderTimes);
    }
}
