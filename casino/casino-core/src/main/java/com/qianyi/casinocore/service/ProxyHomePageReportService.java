package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.model.ProxyHomePageReport;
import com.qianyi.casinocore.repository.ProxyHomePageReportRepository;
import com.qianyi.casinocore.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProxyHomePageReportService {
    @Autowired
    private ProxyHomePageReportRepository proxyHomePageReportRepository;

    @Autowired
    private ChargeOrderService chargeOrderService;

    @Autowired
    private WithdrawOrderService withdrawOrderService;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    public ProxyHomePageReport save(ProxyHomePageReport proxyHomePageReport){
        return proxyHomePageReportRepository.save(proxyHomePageReport);
    }

    public List<ProxyHomePageReport> findByStaticsTimes(String staticsTimes){
        return proxyHomePageReportRepository.findByStaticsTimes(staticsTimes);
    }

    public List<ProxyHomePageReport> findByProxyUserId(Long proxyUserId){
        return proxyHomePageReportRepository.findByProxyUserId(proxyUserId);
    }

    @Transactional
    public void updateFirstProxy(Long proxyUserId, Long firstProxy){
        proxyHomePageReportRepository.updateFirstProxy(proxyUserId,firstProxy);
    }

    @Transactional
    public void updateSecondProxy(Long proxyUserId, Long secondProxy){
        proxyHomePageReportRepository.updateSecondProxy(proxyUserId,secondProxy);
    }

    public ProxyHomePageReport findByProxyUserIdAndStaticsTimes(Long proxyUserId,String staticsTimes){
        return proxyHomePageReportRepository.findByProxyUserIdAndStaticsTimes(proxyUserId,staticsTimes);
    }

    public List<ProxyHomePageReport> findHomePageReports(ProxyHomePageReport proxyHomePageReport,String startTime, String endTime) {
        Specification<ProxyHomePageReport> condition = this.getCondition(proxyHomePageReport,startTime,endTime);
        return proxyHomePageReportRepository.findAll(condition);
    }

    public List<ProxyHomePageReport> findHomePageReports(List<Long> proxyUserId,ProxyHomePageReport proxyHomePageReport,String startTime, String endTime) {
        Specification<ProxyHomePageReport> condition = this.getCondition(proxyUserId,proxyHomePageReport,startTime,endTime);
        return proxyHomePageReportRepository.findAll(condition);
    }

    public List<ProxyHomePageReport> findHomePageReports(ProxyHomePageReport proxyHomePageReport,String startTime, String endTime, Sort sort) {
        Specification<ProxyHomePageReport> condition = this.getCondition(proxyHomePageReport,startTime,endTime);
        return proxyHomePageReportRepository.findAll(condition,sort);
    }

    private Specification<ProxyHomePageReport> getCondition(ProxyHomePageReport proxyHomePageReport,String startTime, String endTime) {
        Specification<ProxyHomePageReport> specification = new Specification<ProxyHomePageReport>() {
            @Override
            public Predicate toPredicate(Root<ProxyHomePageReport> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (proxyHomePageReport.getProxyUserId() != null) {
                    list.add(cb.equal(root.get("proxyUserId").as(Long.class), proxyHomePageReport.getProxyUserId()));
                }
                if (proxyHomePageReport.getProxyRole() != null) {
                    list.add(cb.equal(root.get("proxyRole").as(Long.class), proxyHomePageReport.getProxyRole()));
                }
                if (proxyHomePageReport.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), proxyHomePageReport.getFirstProxy()));
                }
                if (proxyHomePageReport.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), proxyHomePageReport.getSecondProxy()));
                }
                if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                    list.add(cb.between(root.get("staticsTimes").as(String.class), startTime, endTime));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
    private Specification<ProxyHomePageReport> getCondition(List<Long> proxyUserId,ProxyHomePageReport proxyHomePageReport,String startTime, String endTime) {
        Specification<ProxyHomePageReport> specification = new Specification<ProxyHomePageReport>() {
            @Override
            public Predicate toPredicate(Root<ProxyHomePageReport> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                CriteriaBuilder.In<Object> in = cb.in(root.get("proxyUserId"));
                for (Long id : proxyUserId) {
                    in.value(id);
                }
                list.add(cb.and(cb.and(in)));
                if (proxyHomePageReport.getProxyUserId() != null) {
                    list.add(cb.equal(root.get("proxyUserId").as(Long.class), proxyHomePageReport.getProxyUserId()));
                }
                if (proxyHomePageReport.getProxyRole() != null) {
                    list.add(cb.equal(root.get("proxyRole").as(Long.class), proxyHomePageReport.getProxyRole()));
                }
                if (proxyHomePageReport.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), proxyHomePageReport.getFirstProxy()));
                }
                if (proxyHomePageReport.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), proxyHomePageReport.getSecondProxy()));
                }
                if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                    list.add(cb.between(root.get("staticsTimes").as(String.class), startTime, endTime));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
    public void chargeOrder(ProxyUser proxyUser, Date startDate, Date endDate, ProxyHomePageReport proxyHomePageReport){
        try {
            ChargeOrder chargeOrder = new ChargeOrder();
            chargeOrder.setStatus(CommonConst.NUMBER_1);
            if (CommonUtil.setParameter(chargeOrder,proxyUser)){
                return;
            }
            List<ChargeOrder> chargeOrders = chargeOrderService.findListByUpdate(chargeOrder, startDate, endDate);
            if (chargeOrders == null || chargeOrders.size() == CommonConst.NUMBER_0){
                proxyHomePageReport.setChargeAmount(BigDecimal.ZERO);
                proxyHomePageReport.setChargeNums(CommonConst.NUMBER_0);
                return;
            }
            BigDecimal chargeAmount = chargeOrders.stream().map(ChargeOrder::getChargeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            proxyHomePageReport.setChargeAmount(chargeAmount);
            proxyHomePageReport.setChargeNums(chargeOrders.size());
            chargeOrders.clear();
        }catch (Exception ex){
            log.error("统计代理{}充值订单失败{}",proxyUser.getUserName(),ex);
        }
    }
    public void withdrawOrder(ProxyUser proxyUser,Date startDate,Date endDate,ProxyHomePageReport proxyHomePageReport){
        try {
            WithdrawOrder withdrawOrder = new WithdrawOrder();
            withdrawOrder.setStatus(CommonConst.NUMBER_1);
            if (CommonUtil.setParameter(withdrawOrder,proxyUser)){
                return;
            }
            List<WithdrawOrder> withdrawOrders = withdrawOrderService.findListByUpdate(withdrawOrder, startDate, endDate);
            if (withdrawOrders == null || withdrawOrders.size() == CommonConst.NUMBER_0){
                proxyHomePageReport.setWithdrawMoney(BigDecimal.ZERO);
                proxyHomePageReport.setWithdrawNums(CommonConst.NUMBER_0);
                return;
            }
            BigDecimal withdrawMoney = withdrawOrders.stream().map(WithdrawOrder::getWithdrawMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            proxyHomePageReport.setWithdrawMoney(withdrawMoney);
            proxyHomePageReport.setWithdrawNums(withdrawOrders.size());
            withdrawOrders.clear();
        }catch (Exception ex){
            log.error("统计代理{}提现订单失败{}",proxyUser.getUserName(),ex);
        }
    }
    public Set<Long> gameRecord(ProxyUser proxyUser,String startTime,String endTime,ProxyHomePageReport proxyHomePageReport){
        try {
            Set<Long> set = new HashSet<>();
            BigDecimal validbetAmount = BigDecimal.ZERO;
            BigDecimal winLoss = BigDecimal.ZERO;
            Map<String, Object> gameRecordMap = null;
            Map<String, Object> gameRecordGoldenFMap = null;
            if (proxyUser.getProxyRole() == CommonConst.NUMBER_1){
                Set<Long> gameRecordSet = gameRecordService.findGroupByFirst(startTime, endTime, proxyUser.getId());
                gameRecordMap = gameRecordService.findSumBetAndWinLossByFirst(startTime, endTime, proxyUser.getId());
                Set<Long> gameRecordGoldenFSet = gameRecordGoldenFService.findGroupByFirst(startTime, endTime, proxyUser.getId());
                gameRecordGoldenFMap = gameRecordGoldenFService.findSumBetAndWinLossByFirst(startTime, endTime, proxyUser.getId());
                set.addAll(gameRecordSet);
                set.addAll(gameRecordGoldenFSet);
            }else if (proxyUser.getProxyRole() == CommonConst.NUMBER_2){
                Set<Long> gameRecordSet = gameRecordService.findGroupBySecond(startTime, endTime, proxyUser.getId());
                gameRecordMap = gameRecordService.findSumBetAndWinLossBySecond(startTime, endTime, proxyUser.getId());
                Set<Long> gameRecordGoldenFSet = gameRecordGoldenFService.findGroupBySecond(startTime, endTime, proxyUser.getId());
                gameRecordGoldenFMap = gameRecordGoldenFService.findSumBetAndWinLossBySecond(startTime, endTime, proxyUser.getId());
                set.addAll(gameRecordSet);
                set.addAll(gameRecordGoldenFSet);
            }else {
                Set<Long> gameRecordSet = gameRecordService.findGroupByThird(startTime, endTime, proxyUser.getId());
                gameRecordMap = gameRecordService.findSumBetAndWinLossByThird(startTime, endTime, proxyUser.getId());
                Set<Long> gameRecordGoldenFSet = gameRecordGoldenFService.findGroupByThird(startTime, endTime, proxyUser.getId());
                gameRecordGoldenFMap = gameRecordGoldenFService.findSumBetAndWinLossByThird(startTime, endTime, proxyUser.getId());
                set.addAll(gameRecordSet);
                set.addAll(gameRecordGoldenFSet);
            }

            validbetAmount = new BigDecimal(gameRecordMap.get("validbet").toString()).add(new BigDecimal(gameRecordGoldenFMap.get("betAmount").toString()));
            winLoss = new BigDecimal(gameRecordMap.get("winLoss").toString()).add(new BigDecimal(gameRecordGoldenFMap.get("winAmount").toString()));
            proxyHomePageReport.setValidbetAmount(validbetAmount);
            proxyHomePageReport.setWinLossAmount(winLoss);
            proxyHomePageReport.setActiveUsers(set.size());
            gameRecordMap.clear();
            gameRecordGoldenFMap.clear();
            return set;
        }catch (Exception ex){
            log.error("统计代理{}三方游戏注单失败{}",proxyUser.getUserName(),ex);
        }
        return null;
    }
//    public Set<Long> gameRecordAndActive(ProxyUser proxyUser, String startTime, String endTime, ProxyHomePageReport proxyHomePageReport){
//        try {
//            GameRecord gameRecord = new GameRecord();
//            if (CommonUtil.setParameter(gameRecord,proxyUser)){
//                return null;
//            }
//            List<GameRecord> gameRecords = gameRecordService.findGameRecords(gameRecord, startTime, endTime);
//            if (gameRecord  == null || gameRecords.size() == CommonConst.NUMBER_0){
//                proxyHomePageReport.setValidbetAmount(BigDecimal.ZERO);
//                proxyHomePageReport.setWinLossAmount(BigDecimal.ZERO);
//                return null;
//            }
//            BigDecimal validbetAmount = BigDecimal.ZERO;
//            BigDecimal winLoss = BigDecimal.ZERO;
//            for (GameRecord g : gameRecords){
//                validbetAmount = validbetAmount.add(new BigDecimal(g.getValidbet()));
//                winLoss = winLoss.add(new BigDecimal(g.getWinLoss()));
//            }
//            proxyHomePageReport.setValidbetAmount(validbetAmount);
//            proxyHomePageReport.setWinLossAmount(winLoss);
//            Set<Long> set = new HashSet<>();
//            gameRecords.stream().filter(com.qianyi.modulecommon.util.CommonUtil.distinctByKey(GameRecord::getUserId)).forEach(game ->{
//                set.add(game.getUserId());
//            });
//            gameRecords.clear();
//            return set;
//        }catch (Exception ex){
//            log.error("统计代理{}三方游戏注单失败{}",proxyUser.getUserName(),ex);
//        }
//        return null;
//    }

    public void getNewUsers(ProxyUser proxyUser,Date startDate,Date endDate,ProxyHomePageReport proxyHomePageReport){
        try {
            User user = new User();
            if (CommonUtil.setParameter(user,proxyUser)){
                return;
            }
            Long userCount = userService.findUserCount(user, startDate, endDate);
            proxyHomePageReport.setNewUsers(Math.toIntExact(userCount));
        }catch (Exception ex){
            log.error("统计代理{}新增用户失败{}",proxyUser.getUserName(),ex);
        }
    }
    public void getNewThirdProxys(ProxyUser proxyUser,Date startDate,Date endDate,ProxyHomePageReport proxyHomePageReport){
        try {
            ProxyUser proxy = new ProxyUser();
            proxy.setProxyRole(CommonConst.NUMBER_3);
            if (CommonUtil.setParameter(proxy,proxyUser)){
                return;
            }
            Long  proxyUserCount = proxyUserService.findProxyUserCount(proxy,startDate,endDate);
            proxyHomePageReport.setNewThirdProxys(Math.toIntExact(proxyUserCount));
        }catch (Exception ex){
            log.error("统计代理{}新增基层代理失败{}",proxyUser.getUserName(),ex);
        }
    }
    public void getNewSecondProxys(ProxyUser proxyUser,Date startDate,Date endDate,ProxyHomePageReport proxyHomePageReport){
        try {
            ProxyUser proxy = new ProxyUser();
            proxy.setProxyRole(CommonConst.NUMBER_2);
            if (CommonUtil.setParameter(proxy,proxyUser)){
                return;
            }
            Long  proxyUserCount = proxyUserService.findProxyUserCount(proxy,startDate,endDate);
            proxyHomePageReport.setNewSecondProxys(Math.toIntExact(proxyUserCount));
        }catch (Exception ex){
            log.error("统计代理{}新增区域代理失败{}",proxyUser.getUserName(),ex);
        }
    }
}
