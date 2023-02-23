package com.qianyi.casinocore.service;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.qianyi.casinocore.model.ProxyGameRecordReport;
import com.qianyi.casinocore.repository.ProxyGameRecordReportRepository;
import com.qianyi.casinocore.util.CommonUtil;
import com.qianyi.casinocore.util.DTOUtil;
import com.qianyi.casinocore.util.SqlSumConst;
import com.qianyi.casinocore.vo.PersonReportTotalVo;
import com.qianyi.casinocore.vo.RebateReportTotalVo;
import com.qianyi.casinocore.vo.ReportTotalSumVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;

@Service
@Slf4j
public class ProxyGameRecordReportService {

    @Autowired
    private ProxyGameRecordReportRepository proxyGameRecordReportRepository;

    @Autowired
    private UserGameRecordReportService userGameRecordReportService;

    @PersistenceContext
    private EntityManager entityManager;

    public final static String start = " 12:00:00";

    public final static String end = " 11:59:59";

    public List<ProxyGameRecordReport> findAll() {
        return proxyGameRecordReportRepository.findAll();
    }

    public ProxyGameRecordReport save(ProxyGameRecordReport proxyGameRecordReport) {
        return proxyGameRecordReportRepository.save(proxyGameRecordReport);
    }

    public void saveAll(List<ProxyGameRecordReport> proxyGameRecordReports) {
        proxyGameRecordReportRepository.saveAll(proxyGameRecordReports);
    }

    @Transactional
    public void updateKey(Long gameRecordReportId, Long userId, String orderTimes, BigDecimal validAmount,
        BigDecimal winLoss, Long firstProxy, Long secondProxy, Long thirdProxy, BigDecimal betAmount) {
        proxyGameRecordReportRepository.updateKey(gameRecordReportId, userId, orderTimes, validAmount, winLoss,
            firstProxy, secondProxy, thirdProxy, betAmount);
    }

    @Transactional
    public void updateBet(Long gameRecordReportId, Long userId, String orderTimes, BigDecimal validAmount,
        BigDecimal winLoss, Long firstProxy, Long secondProxy, Long thirdProxy, BigDecimal betAmount) {
        proxyGameRecordReportRepository.updateBet(gameRecordReportId, userId, orderTimes, validAmount, winLoss,
            firstProxy, secondProxy, thirdProxy, betAmount);
    }

    @Transactional
    public void updateKey(Long gameRecordReportId, Long userId, String orderTimes, BigDecimal validAmount,
        BigDecimal winLoss, Long firstProxy, Long secondProxy, Long thirdProxy, BigDecimal betAmount,
        Integer bettingNumber) {
        proxyGameRecordReportRepository.updateKey(gameRecordReportId, userId, orderTimes, validAmount, winLoss,
            firstProxy, secondProxy, thirdProxy, betAmount, bettingNumber);
    }

    public Map<String, Object> findSumBetAndWinLossByFirst(String startTime, String endTime, Long firstProxy) {
        return proxyGameRecordReportRepository.findSumBetAndWinLossByFirst(startTime, endTime, firstProxy);
    }

    public Map<String, Object> findSumBetAndWinLossBySecond(String startTime, String endTime, Long secondProxy) {
        return proxyGameRecordReportRepository.findSumBetAndWinLossBySecond(startTime, endTime, secondProxy);
    }

    public Map<String, Object> findSumBetAndWinLossByThird(String startTime, String endTime, Long thirdProxy) {
        return proxyGameRecordReportRepository.findSumBetAndWinLossByThird(startTime, endTime, thirdProxy);
    }

    public Map<String, Object> findSumBetAndWinLossByFirst(Long firstProxy) {
        return proxyGameRecordReportRepository.findSumBetAndWinLossByFirst(firstProxy);
    }

    public Map<String, Object> findSumBetAndWinLossBySecond(Long secondProxy) {
        return proxyGameRecordReportRepository.findSumBetAndWinLossBySecond(secondProxy);
    }

    public Map<String, Object> findSumBetAndWinLossByThird(Long thirdProxy) {
        return proxyGameRecordReportRepository.findSumBetAndWinLossByThird(thirdProxy);
    }

    public Map<String, Object> findSumBetAndWinLoss(String startTime, String endTime) {
        return proxyGameRecordReportRepository.findSumBetAndWinLoss(startTime, endTime);
    }

    public Map<String, Object> findSumBetAndWinLoss() {
        return proxyGameRecordReportRepository.findSumBetAndWinLoss();
    }

    public List<Map<String, Object>> findBetAndWinLoss(String startTime, String endTime) {
        return proxyGameRecordReportRepository.findBetAndWinLoss(startTime, endTime);
    }

    @Transactional
    public void comparison(String dayTime) {// dayTime为一天yyyy-MM-dd
        String startTime = dayTime + start;
        Date date = null;
        try {
            date = DateUtil.getDate(dayTime);
        } catch (ParseException e) {
            log.error("时间格式错误{}", dayTime);
            e.printStackTrace();
        }
        Calendar nowTime = Calendar.getInstance();
        nowTime.setTime(date);
        nowTime.add(Calendar.DATE, 1);
        String tomorrow = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        String endTime = tomorrow + end;
        Integer betNumber = proxyGameRecordReportRepository.findBetNumber(dayTime, dayTime);
        Integer totalBetNumber = userGameRecordReportService.findTotalBetNumber(startTime, endTime);
        Integer totalBetNumberByAe = userGameRecordReportService.findTotalBetNumberByAe(startTime, endTime);
        Integer totalBetNumberByVnc = userGameRecordReportService.findTotalBetNumberByVnc(startTime, endTime);
        Integer totalBetNumberByDmc = userGameRecordReportService.findTotalBetNumberByDmc(startTime, endTime);
        Integer totalBetNumberByDg = userGameRecordReportService.findTotalBetNumberByDg(startTime, endTime);
        Integer totalBetNumberByObzr = userGameRecordReportService.findTotalBetNumberByObzr(startTime, endTime);
        Integer total =
            totalBetNumber + totalBetNumberByAe + totalBetNumberByVnc + totalBetNumberByDmc + totalBetNumberByDg + totalBetNumberByObzr;
        log.info(
            "代理报表日期{} betNumber:{} total:{} totalBetNumber:{}  totalBetNumberByAe:{} totalBetNumberByVnc:{} totalBetNumberByDmc:{} totalBetNumberByDg:{} totalBetNumberByObzr:{}",
            dayTime, betNumber, total, totalBetNumber, totalBetNumberByAe, totalBetNumberByVnc, totalBetNumberByDmc,
            totalBetNumberByDg,totalBetNumberByObzr);
        if (betNumber.intValue() != total.intValue()) {
            log.error(
                "代理报表日期{}不相等开始重新计算betNumber:{}  total:{} totalBetNumber:{} totalBetNumberByAe:{} totalBetNumberByVnc:{} totalBetNumberByDmc:{} totalBetNumberByDg:{} totalBetNumberByObzr:{}",
                dayTime, betNumber, total, totalBetNumber, totalBetNumberByAe, totalBetNumberByVnc, totalBetNumberByDmc,
                totalBetNumberByDg,totalBetNumberByObzr);
            proxyGameRecordReportRepository.deleteByOrderTimes(dayTime);
            try {
                List<Map<String, Object>> totalMap = proxyGameRecordReportRepository.findTotal(startTime, endTime);
                if (CollUtil.isNotEmpty(totalMap)) {
                    List<ProxyGameRecordReport> proxyGameRecordReports = new ArrayList<>();
                    totalMap.forEach(map -> {
                        ProxyGameRecordReport proxyGameRecordReport = new ProxyGameRecordReport();
                        proxyGameRecordReport.setOrderTimes(dayTime);
                        proxyGameRecordReport.setUserId(Long.parseLong(map.get("user_id").toString()));
                        if (Objects.nonNull(map.get("first_proxy"))
                            && StringUtils.hasLength(map.get("first_proxy").toString())) {
                            proxyGameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
                        } else {
                            proxyGameRecordReport.setFirstProxy(0L);
                        }
                        if (Objects.nonNull(map.get("second_proxy"))
                            && StringUtils.hasLength(map.get("second_proxy").toString())) {
                            proxyGameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
                        } else {
                            proxyGameRecordReport.setSecondProxy(0L);
                        }
                        if (Objects.nonNull(map.get("third_proxy"))
                            && StringUtils.hasLength(map.get("third_proxy").toString())) {
                            proxyGameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
                        } else {
                            proxyGameRecordReport.setThirdProxy(0L);
                        }
                        proxyGameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                        proxyGameRecordReport.setBetAmount(new BigDecimal(map.get("bet_amount").toString()));
                        proxyGameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                        proxyGameRecordReport.setWinLoss(new BigDecimal(map.get("win_loss").toString()));
                        Long proxyGameRecordReportId =
                            CommonUtil.toHash(dayTime + proxyGameRecordReport.getUserId().toString());
                        proxyGameRecordReport.setProxyGameRecordReportId(proxyGameRecordReportId);
                        proxyGameRecordReport.setCreateTime(new Date());
                        proxyGameRecordReport.setUpdateTime(new Date());
                        proxyGameRecordReports.add(proxyGameRecordReport);
                    });
                    Lists.partition(proxyGameRecordReports, 200)
                        .forEach(proxyGameRecordReports1 -> this.saveAll(proxyGameRecordReports1));
                    totalMap.clear();
                    proxyGameRecordReports.clear();
                }
            } catch (Exception ex) {
                log.error("代理报表计算失败日期{}", dayTime);
            }

            try {
                List<Map<String, Object>> vncMap = proxyGameRecordReportRepository.findVnc(startTime, endTime);
                if (CollUtil.isNotEmpty(vncMap)) {
                    vncMap.forEach(map -> {
                        Long userId = Long.parseLong(map.get("user_id").toString());
                        Long firstProxy = 0L;
                        Long secondProxy = 0L;
                        Long thirdProxy = 0L;
                        if (Objects.nonNull(map.get("first_proxy"))
                            && StringUtils.hasLength(map.get("first_proxy").toString())) {
                            firstProxy = Long.parseLong(map.get("first_proxy").toString());
                        }
                        if (Objects.nonNull(map.get("second_proxy"))
                            && StringUtils.hasLength(map.get("second_proxy").toString())) {
                            secondProxy = Long.parseLong(map.get("second_proxy").toString());
                        }
                        if (Objects.nonNull(map.get("third_proxy"))
                            && StringUtils.hasLength(map.get("third_proxy").toString())) {
                            thirdProxy = Long.parseLong(map.get("third_proxy").toString());
                        }
                        Integer bettingNumber = Integer.parseInt(map.get("num").toString());
                        BigDecimal betAmount = new BigDecimal(map.get("bet_amount").toString());
                        BigDecimal validAmount = new BigDecimal(map.get("validbet").toString());
                        BigDecimal winLoss = new BigDecimal(map.get("win_loss").toString());
                        Long proxyGameRecordReportId = CommonUtil.toHash(dayTime + userId.toString());
                        this.updateKey(proxyGameRecordReportId, userId, dayTime, validAmount, winLoss, firstProxy,
                            secondProxy, thirdProxy, betAmount, bettingNumber);
                    });
                    vncMap.clear();
                }
            } catch (Exception ex) {
                log.error("代理报表计算VNC失败日期{}", dayTime);
            }

            try {
                List<Map<String, Object>> dmcMap = proxyGameRecordReportRepository.findDmc(startTime, endTime);
                if (CollUtil.isNotEmpty(dmcMap)) {
                    dmcMap.forEach(map -> {
                        Long userId = Long.parseLong(map.get("user_id").toString());
                        Long firstProxy = 0L;
                        Long secondProxy = 0L;
                        Long thirdProxy = 0L;
                        if (Objects.nonNull(map.get("first_proxy"))
                            && StringUtils.hasLength(map.get("first_proxy").toString())) {
                            firstProxy = Long.parseLong(map.get("first_proxy").toString());
                        }
                        if (Objects.nonNull(map.get("second_proxy"))
                            && StringUtils.hasLength(map.get("second_proxy").toString())) {
                            secondProxy = Long.parseLong(map.get("second_proxy").toString());
                        }
                        if (Objects.nonNull(map.get("third_proxy"))
                            && StringUtils.hasLength(map.get("third_proxy").toString())) {
                            thirdProxy = Long.parseLong(map.get("third_proxy").toString());
                        }
                        Integer bettingNumber = Integer.parseInt(map.get("num").toString());
                        BigDecimal betAmount = new BigDecimal(map.get("bet_amount").toString());
                        BigDecimal validAmount = new BigDecimal(map.get("validbet").toString());
                        BigDecimal winLoss = new BigDecimal(map.get("win_loss").toString());
                        Long proxyGameRecordReportId = CommonUtil.toHash(dayTime + userId.toString());
                        this.updateKey(proxyGameRecordReportId, userId, dayTime, validAmount, winLoss, firstProxy,
                            secondProxy, thirdProxy, betAmount, bettingNumber);
                    });
                    dmcMap.clear();
                }
            } catch (Exception ex) {
                log.error("代理报表计算DMC失败日期{}", dayTime);
            }

            try {
                List<Map<String, Object>> dgMap = proxyGameRecordReportRepository.findDg(startTime, endTime);
                if (CollUtil.isNotEmpty(dgMap)) {
                    dgMap.forEach(map -> {
                        Long userId = Long.parseLong(map.get("user_id").toString());
                        Long firstProxy = 0L;
                        Long secondProxy = 0L;
                        Long thirdProxy = 0L;
                        if (Objects.nonNull(map.get("first_proxy"))
                            && StringUtils.hasLength(map.get("first_proxy").toString())) {
                            firstProxy = Long.parseLong(map.get("first_proxy").toString());
                        }
                        if (Objects.nonNull(map.get("second_proxy"))
                            && StringUtils.hasLength(map.get("second_proxy").toString())) {
                            secondProxy = Long.parseLong(map.get("second_proxy").toString());
                        }
                        if (Objects.nonNull(map.get("third_proxy"))
                            && StringUtils.hasLength(map.get("third_proxy").toString())) {
                            thirdProxy = Long.parseLong(map.get("third_proxy").toString());
                        }
                        Integer bettingNumber = Integer.parseInt(map.get("num").toString());
                        BigDecimal betAmount = new BigDecimal(map.get("bet_amount").toString());
                        BigDecimal validAmount = new BigDecimal(map.get("validbet").toString());
                        BigDecimal winLoss = new BigDecimal(map.get("win_loss").toString());
                        Long proxyGameRecordReportId = CommonUtil.toHash(dayTime + userId.toString());
                        this.updateKey(proxyGameRecordReportId, userId, dayTime, validAmount, winLoss, firstProxy,
                            secondProxy, thirdProxy, betAmount, bettingNumber);
                    });
                    dgMap.clear();
                }
            } catch (Exception ex) {
                log.error("代理报表计算DG失败日期{}", dayTime);
            }
            try {
                List<Map<String, Object>> obzrMap = proxyGameRecordReportRepository.findObzr(startTime, endTime);
                if (CollUtil.isNotEmpty(obzrMap)) {
                    obzrMap.forEach(map -> {
                        Long userId = Long.parseLong(map.get("user_id").toString());
                        Long firstProxy = 0L;
                        Long secondProxy = 0L;
                        Long thirdProxy = 0L;
                        if (Objects.nonNull(map.get("first_proxy"))
                                && StringUtils.hasLength(map.get("first_proxy").toString())) {
                            firstProxy = Long.parseLong(map.get("first_proxy").toString());
                        }
                        if (Objects.nonNull(map.get("second_proxy"))
                                && StringUtils.hasLength(map.get("second_proxy").toString())) {
                            secondProxy = Long.parseLong(map.get("second_proxy").toString());
                        }
                        if (Objects.nonNull(map.get("third_proxy"))
                                && StringUtils.hasLength(map.get("third_proxy").toString())) {
                            thirdProxy = Long.parseLong(map.get("third_proxy").toString());
                        }
                        Integer bettingNumber = Integer.parseInt(map.get("num").toString());
                        BigDecimal betAmount = new BigDecimal(map.get("bet_amount").toString());
                        BigDecimal validAmount = new BigDecimal(map.get("validbet").toString());
                        BigDecimal winLoss = new BigDecimal(map.get("win_loss").toString());
                        Long proxyGameRecordReportId = CommonUtil.toHash(dayTime + userId.toString());
                        this.updateKey(proxyGameRecordReportId, userId, dayTime, validAmount, winLoss, firstProxy,
                                secondProxy, thirdProxy, betAmount, bettingNumber);
                    });
                    obzrMap.clear();
                }
            } catch (Exception ex) {
                log.error("代理报表计算obzr失败日期{}", dayTime);
            }
        }
    }

    @Transactional
    public void deleteByOrderTimes(String startTime, String endTime) {
        proxyGameRecordReportRepository.deleteByOrderTimes(startTime, endTime);
    }

    @Transactional
    public void deleteByOrderTimes(String orderTimes) {
        proxyGameRecordReportRepository.deleteByOrderTimes(orderTimes);
    }

    @SuppressWarnings("unchecked")
    public ReportTotalSumVo findMapSum(String platform, String startTime, String endTime) {
        startTime = "'" + startTime + "'";
        endTime = "'" + endTime + "'";
        String sql = "";
        if (platform.equals(Constants.PLATFORM_WM_BIG)) {
            sql = MessageFormat.format(SqlSumConst.WMSumSql, startTime, endTime);
        } else if (platform.equals(Constants.PLATFORM_OBDJ)) {
            sql = MessageFormat.format(SqlSumConst.obdjSumSql, startTime, endTime);
        } else if (platform.equals(Constants.PLATFORM_OBTY)) {
            sql = MessageFormat.format(SqlSumConst.obtySumSql, startTime, endTime);
        } else if (platform.equals(Constants.PLATFORM_OBZR)) {
            sql = MessageFormat.format(SqlSumConst.obzrSumSql, startTime, endTime);
        } else if (platform.equals(Constants.PLATFORM_PG)) {
            sql = MessageFormat.format(SqlSumConst.PGAndCQ9SumSql, startTime, endTime, "'PG'");
        } else if (platform.equals(Constants.PLATFORM_AE)) {
            sql = MessageFormat.format(SqlSumConst.aeSumMergeSql, startTime, endTime);
        } else if (platform.equals(Constants.PLATFORM_SABASPORT)) {
            sql = MessageFormat.format(SqlSumConst.sabasportSumSql, startTime, endTime, "'SABASPORT'", "'Payoff'",
                "'Stake'", "'cancelPayoff'");
        } else if (platform.equals(Constants.PLATFORM_VNC)) {
            sql = MessageFormat.format(SqlSumConst.vncSumMergeSql, startTime, endTime);
        } else if (platform.equals(Constants.PLATFORM_DMC)) {
            sql = MessageFormat.format(SqlSumConst.dmcSumMergeSql, startTime, endTime);
        } else if (platform.equals(Constants.PLATFORM_DG)) {
            sql = MessageFormat.format(SqlSumConst.dgSumMergeSql, startTime, endTime);
        } else {
            sql = MessageFormat.format(SqlSumConst.PGAndCQ9SumSql, startTime, endTime, "'CQ9'");
        }
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        Object result = countQuery.getSingleResult();
        Map<String, Object> map = new HashMap<>();
        Object[] obj = (Object[])result;
        for (int i = 0; i < REPORT_TOTAL_FIELD_LIST.size(); i++) {
            String field = REPORT_TOTAL_FIELD_LIST.get(i);
            Object value = obj[i];
            map.put(field, value);
        }
        ReportTotalSumVo itemObject = DTOUtil.toDTO(map, ReportTotalSumVo.class);
        return itemObject;
    }

    private static final List<String> REPORT_TOTAL_FIELD_LIST =
        Arrays.asList("num", "bet_amount", "validbet", "win_loss");

    @SuppressWarnings("unchecked")
    public PersonReportTotalVo findMapSum(String startTime, String endTime) {
        startTime = "'" + startTime + "'";
        endTime = "'" + endTime + "'";
        String sql = MessageFormat.format(SqlSumConst.sumSql, startTime, endTime);
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        Object result = countQuery.getSingleResult();
        Map<String, Object> map = new HashMap<>();
        Object[] obj = (Object[])result;
        for (int i = 0; i < TOTAL_FIELD_LIST.size(); i++) {
            String field = TOTAL_FIELD_LIST.get(i);
            Object value = obj[i];
            map.put(field, value);
        }
        PersonReportTotalVo itemObject = DTOUtil.toDTO(map, PersonReportTotalVo.class);
        return itemObject;
    }

    private static final List<String> TOTAL_FIELD_LIST =
        Arrays.asList("wash_amount", "service_charge", "all_profit_amount", "all_water", "todayAward", "riseAward");

    @SuppressWarnings("unchecked")
    public RebateReportTotalVo findMapRebateSum(String startTime, String endTime) {
        startTime = "'" + startTime + "'";
        endTime = "'" + endTime + "'";
        String sql = MessageFormat.format(SqlSumConst.sumRebateSql, startTime, endTime);
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        Object result = countQuery.getSingleResult();
        Map<String, Object> map = new HashMap<>();
        Object[] obj = (Object[])result;
        for (int i = 0; i < REBATE_REPORT_TOTAL_FIELD_LIST.size(); i++) {
            String field = REBATE_REPORT_TOTAL_FIELD_LIST.get(i);
            Object value = obj[i];
            map.put(field, value);
        }
        RebateReportTotalVo itemObject = DTOUtil.toDTO(map, RebateReportTotalVo.class);
        return itemObject;
    }

    private static final List<String> REBATE_REPORT_TOTAL_FIELD_LIST =
        Arrays.asList("total_rebate", "user_amount", "surplus_amount", "service_charge");
}