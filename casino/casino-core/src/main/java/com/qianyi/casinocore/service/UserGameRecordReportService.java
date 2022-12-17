package com.qianyi.casinocore.service;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.qianyi.casinocore.model.UserGameRecordReport;
import com.qianyi.casinocore.repository.UserGameRecordReportRepository;
import com.qianyi.casinocore.util.CommonUtil;
import com.qianyi.modulecommon.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserGameRecordReportService {

    @Autowired
    private UserGameRecordReportRepository userGameRecordReportRepository;

    @Autowired
    private UserRunningWaterService userRunningWaterService;

    public final static String start = " 00:00:00";

    public final static String end = " 23:59:59";

    @Transactional
    public void updateKey(Long gameRecordReportId, Long userId, String orderTimes, BigDecimal validAmount,
        BigDecimal winLoss, BigDecimal betAmount, String platform) {
        userGameRecordReportRepository.updateKey(gameRecordReportId, userId, orderTimes, validAmount, winLoss,
            betAmount, platform);
    }

    @Transactional
    public void updateBet(Long gameRecordReportId, Long userId, String orderTimes, BigDecimal validAmount,
        BigDecimal winLoss, BigDecimal betAmount, String platform) {
        userGameRecordReportRepository.updateBet(gameRecordReportId, userId, orderTimes, validAmount, winLoss,
            betAmount, platform);
    }

    public UserGameRecordReport save(UserGameRecordReport userGameRecordReport) {
        return userGameRecordReportRepository.save(userGameRecordReport);
    }

    public void saveAll(List<UserGameRecordReport> userGameRecordReport) {
        userGameRecordReportRepository.saveAll(userGameRecordReport);
    }

    public List<Map<String, Object>> sumUserRunningWater(String startTime, String endTime) {
        return userGameRecordReportRepository.sumUserRunningWater(startTime, endTime);
    }

    public BigDecimal sumUserRunningWaterByUserId(String startTime, String endTime, Long userId) {
        return userGameRecordReportRepository.sumUserRunningWaterByUserId(startTime, endTime, userId);
    }

    public Integer findTotalBetNumber(String startTime, String endTime) {
        return userGameRecordReportRepository.findTotalBetNumber(startTime, endTime);
    }

    public Integer findTotalBetNumberByAe(String startTime,String endTime){
        return userGameRecordReportRepository.findTotalBetNumberByAe(startTime,endTime);
    }

    public Integer findTotalBetNumberByVnc(String startTime,String endTime){
        return userGameRecordReportRepository.findTotalBetNumberByVnc(startTime,endTime);
    }

    @Transactional
    public void comparison(String dayTime) {// dayTime为一天yyyy-MM-dd
        String startTime = dayTime + start;
        String endTime = dayTime + end;
        Integer betNumber = userGameRecordReportRepository.findBetNumber(dayTime, dayTime);
        Integer totalBetNumber = this.findTotalBetNumber(startTime, endTime);
        Integer totalBetNumberByAe = this.findTotalBetNumberByAe(startTime, endTime);
        Integer totalBetNumberByVnc = this.findTotalBetNumberByVnc(startTime, endTime);
        Integer total = totalBetNumber + totalBetNumberByAe + totalBetNumberByVnc;
        log.info("会员报表日期{} betNumber:{} total:{} totalBetNumber:{} totalBetNumberByAe:{} totalBetNumberByVnc:{}", dayTime, betNumber,total, totalBetNumber,totalBetNumberByAe,totalBetNumberByVnc);
        if (betNumber.intValue() != total.intValue()) {
            log.error("会员报表日期{}不相等开始重新计算betNumber:{} total:{} totalBetNumber:{} totalBetNumberByAe:{} totalBetNumberByVnc:{}", dayTime, betNumber,total, totalBetNumber,totalBetNumberByAe,totalBetNumberByVnc);
            userGameRecordReportRepository.deleteByOrderTimes(dayTime);

            List<Map<String, Object>> wm = userGameRecordReportRepository.findWm(startTime, endTime);
            this.addData(wm, dayTime, Constants.PLATFORM_WM);

            List<Map<String, Object>> pg = userGameRecordReportRepository.findPg(startTime, endTime);
            this.addData(pg, dayTime, Constants.PLATFORM_PG);

            List<Map<String, Object>> sb = userGameRecordReportRepository.findSb(startTime, endTime,Constants.PLATFORM_SABASPORT);
            this.addData(sb, dayTime, Constants.PLATFORM_PG);

            List<Map<String, Object>> obdj = userGameRecordReportRepository.findObdj(startTime, endTime);
            this.addData(obdj, dayTime, Constants.PLATFORM_OBDJ);

            List<Map<String, Object>> obty = userGameRecordReportRepository.findObty(startTime, endTime);
            this.addData(obty, dayTime, Constants.PLATFORM_OBTY);

            //            List<Map<String, Object>> HORSEBOOK = userGameRecordReportRepository.findAe(startTime, endTime, Constants.PLATFORM_AE_HORSEBOOK);
            //            this.addData(HORSEBOOK, dayTime, Constants.PLATFORM_AE_HORSEBOOK);
            //
            //            List<Map<String, Object>> SV388 = userGameRecordReportRepository.findAe(startTime, endTime, Constants.PLATFORM_AE_SV388);
            //            this.addData(SV388, dayTime, Constants.PLATFORM_AE_SV388);
            //
            //            List<Map<String, Object>> E1SPORT = userGameRecordReportRepository.findAe(startTime, endTime, Constants.PLATFORM_AE_E1SPORT);
            //            this.addData(E1SPORT, dayTime, Constants.PLATFORM_AE_E1SPORT);

            List<Map<String, Object>> AE = userGameRecordReportRepository.findAe(startTime, endTime);
            this.addData(AE, dayTime, Constants.PLATFORM_AE);

            List<Map<String, Object>> VNC = userGameRecordReportRepository.findVnc(startTime, endTime);
            this.addData(VNC, dayTime, Constants.PLATFORM_VNC);

            List<Map<String, Object>> DMC = userGameRecordReportRepository.findDmc(startTime, endTime);
            this.addData(DMC, dayTime, Constants.PLATFORM_DMC);

            List<Map<String, Object>> DG = userGameRecordReportRepository.findDg(startTime, endTime);
            this.addData(DG, dayTime, Constants.PLATFORM_DG);

            userRunningWaterService.statistics(dayTime);
        }
    }

    private void addData(List<Map<String, Object>> listMap, String dayTime, String platform) {
        try {
            if (CollUtil.isNotEmpty(listMap)) {
                List<UserGameRecordReport> userGameRecordReports = new ArrayList<>();
                listMap.forEach(map -> {
                    UserGameRecordReport userGameRecordReport = new UserGameRecordReport();
                    if (platform.equals(Constants.PLATFORM_PG)) {
                        userGameRecordReport.setPlatform(map.get("vendor_code").toString());
                    } else {
                        userGameRecordReport.setPlatform(platform);
                    }
                    userGameRecordReport.setOrderTimes(dayTime);
                    userGameRecordReport.setUserId(Long.parseLong(map.get("user_id").toString()));
                    userGameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                    userGameRecordReport.setBetAmount(new BigDecimal(map.get("bet_amount").toString()));
                    userGameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                    userGameRecordReport.setWinLoss(new BigDecimal(map.get("win_loss").toString()));
                    Long userGameRecordReportId = CommonUtil.toHash(
                        dayTime + userGameRecordReport.getUserId().toString() + userGameRecordReport.getPlatform());
                    userGameRecordReport.setUserGameRecordReportId(userGameRecordReportId);
                    userGameRecordReport.setCreateTime(new Date());
                    userGameRecordReport.setUpdateTime(new Date());
                    userGameRecordReports.add(userGameRecordReport);
                });
                Lists.partition(userGameRecordReports, 200)
                    .forEach(userGameRecordReportList -> this.saveAll(userGameRecordReportList));
                listMap.clear();
                userGameRecordReports.clear();
            }
        } catch (Exception ex) {
            log.error("会员报表计算失败日期{}Platform{}", dayTime, platform);
        }
    }

    @Transactional
    public void deleteByPlatform(String platform){
        userGameRecordReportRepository.deleteByPlatform(platform);
    }

    @Transactional
    public void deleteByOrderTimes(String orderTimes){
        userGameRecordReportRepository.deleteByOrderTimes(orderTimes);
    }
}
