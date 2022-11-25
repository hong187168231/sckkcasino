package com.qianyi.casinocore.service;

import cn.hutool.core.collection.CollUtil;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserRunningWater;
import com.qianyi.casinocore.repository.UserRunningWaterRepository;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
@Slf4j
public class UserRunningWaterService {

    @Autowired
    private UserRunningWaterRepository userRunningWaterRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ShareProfitChangeService shareProfitChangeService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserGameRecordReportService userGameRecordReportService;

    public final static String start = " 00:00:00";

    public final static String end = " 23:59:59";

    public void updateKey(Long userId, String staticsTimes , BigDecimal amount, BigDecimal commission,Long firstProxy,Long secondProxy,Long thirdProxy){
        userRunningWaterRepository.updateKey(userId,staticsTimes,amount,commission,firstProxy,secondProxy,thirdProxy);
    }

    public List<UserRunningWater> findByStaticsTimes(String staticsTimes){
        return userRunningWaterRepository.findByStaticsTimes(staticsTimes);
    }

    @Transactional
    public void updateFirstProxy(Long userId, Long firstProxy){
        userRunningWaterRepository.updateFirstProxy(userId,firstProxy);
    }

    @Transactional
    public void updateSecondProxy(Long userId, Long firstProxy){
        userRunningWaterRepository.updateSecondProxy(userId,firstProxy);
    }

    @Transactional
    public void updatetThirdProxy(Long userId, Long firstProxy){
        userRunningWaterRepository.updatetThirdProxy(userId,firstProxy);
    }

    public Page<UserRunningWater> findUserPage(Pageable pageable, UserRunningWater userRunningWater, String startTime, String endTime){
        Specification<UserRunningWater> condition = this.getCondition(userRunningWater,startTime,endTime);
        return userRunningWaterRepository.findAll(condition,pageable);
    }

    public List<UserRunningWater> findUserRunningWaters(Sort sort,UserRunningWater userRunningWater, String startTime, String endTime){
        Specification<UserRunningWater> condition = this.getCondition(userRunningWater,startTime,endTime);
        return userRunningWaterRepository.findAll(condition,sort);
    }

    private Specification<UserRunningWater> getCondition(UserRunningWater userRunningWater, String startTime, String endTime) {
        Specification<UserRunningWater> specification = new Specification<UserRunningWater>() {
            @Override
            public Predicate toPredicate(Root<UserRunningWater> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (userRunningWater.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), userRunningWater.getUserId()));
                }
                //                if (startDate != null) {
                //                    list.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), startDate));
                //                }
                //                if (endDate != null) {
                //                    list.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class),endDate));
                //                }
                if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                    list.add(
                        cb.between(root.get("staticsTimes").as(String.class), startTime, endTime)
                    );
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
    public List<UserRunningWater> findUserRunningWaterList(UserRunningWater userRunningWater, String startTime, String endTime) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserRunningWater> query = builder.createQuery(UserRunningWater.class);
        Root<UserRunningWater> root = query.from(UserRunningWater.class);
        query.multiselect(
            root.get("userId").as(Long.class)
        );

        List<Predicate> predicates = new ArrayList();

        if (userRunningWater.getUserId() != null) {
            predicates.add(builder.equal(root.get("userId").as(Long.class), userRunningWater.getUserId()));
        }
        if (userRunningWater.getFirstProxy() != null) {
            predicates.add(builder.equal(root.get("firstProxy").as(Long.class), userRunningWater.getFirstProxy()));
        }
        if (userRunningWater.getSecondProxy() != null) {
            predicates.add(builder.equal(root.get("secondProxy").as(Long.class), userRunningWater.getSecondProxy()));
        }
        if (userRunningWater.getThirdProxy() != null) {
            predicates.add(builder.equal(root.get("thirdProxy").as(Long.class), userRunningWater.getThirdProxy()));
        }
        if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
            predicates.add(builder.between(root.get("staticsTimes").as(String.class), startTime, endTime));
        }
        query.where(predicates.toArray(new Predicate[predicates.size()])).groupBy(root.get("userId"));

        List<UserRunningWater> list = entityManager.createQuery(query).getResultList();
        return list;
    }

    @Transactional
    public void deleteByStaticsTimes(String staticsTimes){
        userRunningWaterRepository.deleteByStaticsTimes(staticsTimes);
    }

    public void statistics(String format){
        log.info("每日会员流水报表统计开始start=============================================》");
        String today = DateUtil.getSimpleDateFormat1().format(new Date());
        if (today.equals(format)){//不计算今天
            return;
        }

        long currentTime = System.currentTimeMillis();
        try {
            this.deleteByStaticsTimes(format);
            String startTime = format + start;
            String endTime = format + end;
            this.sumUserRunningWater(format);
            this.shareProfitChange(format, startTime, endTime);
        } catch (Exception ex) {
            log.error("每日会员流水报表统计失败", ex);
        }
        log.info("每日会员流水报表统计结束end耗时{}==============================================>",System.currentTimeMillis()-currentTime);
    }

    public void sumUserRunningWater(String format) {
        try {
            List<Map<String, Object>> gameRecords = userGameRecordReportService.sumUserRunningWater(format, format);
            if (gameRecords == null || gameRecords.size() == CommonConst.NUMBER_0) {
                return;
            }
            gameRecords.stream().forEach(item -> {
                Long userId = Long.valueOf(item.get("userId").toString());
                BigDecimal validbet = new BigDecimal(item.get("validbet").toString());
                User user = userService.findById(userId);
                if (Objects.isNull(user) || Objects.isNull(user.getFirstProxy())) {
                    userRunningWaterRepository.updateKey(userId, format, validbet, BigDecimal.ZERO, CommonConst.LONG_0,
                        CommonConst.LONG_0, CommonConst.LONG_0);
                } else {
                    userRunningWaterRepository.updateKey(userId, format, validbet, BigDecimal.ZERO, user.getFirstProxy(),
                        user.getSecondProxy(), user.getThirdProxy());
                }
            });
            gameRecords.clear();
        } catch (Exception ex) {
            log.error("用户流水统计失败{}", ex);
        }
    }

    public void shareProfitChange(String format, String startDate, String endDate) {
        try {
            List<Map<String, Object>> sumAmount = shareProfitChangeService.findSumAmount(startDate, endDate);
            if (sumAmount == null || sumAmount.size() == CommonConst.NUMBER_0) {
                return;
            }
            sumAmount.stream().forEach(item -> {
                Long userId = Long.valueOf(item.get("fromUserId").toString());
                BigDecimal amount = new BigDecimal(item.get("amount").toString());
                User user = userService.findById(userId);
                if (Objects.isNull(user) || Objects.isNull(user.getFirstProxy())) {
                    userRunningWaterRepository.updateKey(userId, format, BigDecimal.ZERO, amount, CommonConst.LONG_0,
                        CommonConst.LONG_0, CommonConst.LONG_0);
                } else {
                    userRunningWaterRepository.updateKey(userId, format, BigDecimal.ZERO, amount, user.getFirstProxy(),
                        user.getSecondProxy(), user.getThirdProxy());
                }
            });
            sumAmount.clear();
        } catch (Exception ex) {
            log.error("用户流水统计人人代佣金失败{}", ex);
        }
    }
}
