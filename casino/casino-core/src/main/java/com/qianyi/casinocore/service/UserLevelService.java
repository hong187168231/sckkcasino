package com.qianyi.casinocore.service;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.co.user.LevelChangeCo;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.repository.UserLevelRepository;
import com.qianyi.casinocore.util.LevelUtil;
import com.qianyi.casinocore.vo.UserVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@CacheConfig(cacheNames = {"userLevel"})
@Slf4j
public class UserLevelService {

    @Autowired
    UserLevelRepository userLevelRepository;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private PlatformConfigService platformConfigService;
    @Autowired
    private LevelWaterChangeService levelWaterChangeService;
    @Autowired
    private UserService userService;
    @Autowired
    private WashCodeChangeService washCodeChangeService;

    @PersistenceContext
    private EntityManager entityManager;

    public UserLevelRecord save(UserLevelRecord userLevel) {
        return userLevelRepository.save(userLevel);
    }


    public UserLevelRecord queryKeepLevel(UserLevelRecord userLevel) {
        return userLevelRepository.save(userLevel);
    }

    public UserLevelRecord findByUserId(Long userId) {
        return userLevelRepository.findByUserId(userId);
    }

    public List<UserLevelRecord> findUserLevel(List<Long> userIds) {
        Specification<UserLevelRecord> condition = getConditionId(userIds);
        List<UserLevelRecord> proxyUserList = userLevelRepository.findAll(condition);
        proxyUserList.stream().collect(Collectors.groupingBy(UserLevelRecord::getUserId, Collectors.maxBy(Comparator.comparing(UserLevelRecord::getUpdateTime)))).forEach((k, v) -> {
            System.out.println("K:" + k + "-V:" + v.get());
        });
        return proxyUserList;
    }

    public void processLevelWater(User user, UserVo userVo) {
        PlatformConfig platformConfig = platformConfigService.findFirst();
        Integer upgradeBet = LevelUtil.getUpgradeBet(platformConfig, user.getLevel());
        Integer keepBet = LevelUtil.getKeepBet(platformConfig, user.getLevel());
        UserMoney userMoney = userMoneyService.findByUserId(user.getId());
        BigDecimal riseWater = userMoney.getRiseWater();
        if (user.getLevel() >= 10) {
            riseWater = new BigDecimal(upgradeBet);
        }
        userVo.setPromote(riseWater + "/" + new BigDecimal(upgradeBet));
        BigDecimal levelWater = userVo.getLevelWater() == null ? BigDecimal.ZERO : userVo.getLevelWater();
        if (levelWater.subtract(new BigDecimal(keepBet)).intValue() > 0) {
            levelWater = new BigDecimal(keepBet);
        }
        userVo.setKeep(levelWater + "/" + new BigDecimal(keepBet));
    }

    public void processLevelWater2(UserLevelRecord change) {
//        PlatformConfig platformConfig = platformConfigService.findFirst();
//        Integer upgradeBet = LevelUtil.getUpgradeBet(platformConfig, change.getLevel());
//        Integer keepBet = LevelUtil.getKeepBet(platformConfig, change.getLevel());
        if (change.getChangeType().equals(1)) {
            change.setSchedule(change.getSchedule());
        } else {
            change.setSchedule(change.getSchedule());
        }

    }


    public UserLevelRecord findDropRecord(Long userId, Integer level) {
        return userLevelRepository.findDropRecord(userId, level);
    }

    public List<Map<String, Object>> findLastRiseUser(Date startTime, Date endTime) {
        SimpleDateFormat format =  DateUtil.getSimpleDateFormat();
        return userLevelRepository.findLastRiseUser(format.format(startTime),format.format(endTime));
    }


    public Page<UserLevelRecord> findLevelChangePage(Pageable pageable, UserLevelRecord userLevel) {
        Specification<UserLevelRecord> condition = this.getCondition(userLevel);
        Page<UserLevelRecord> all = userLevelRepository.findAll(condition, pageable);
        return all;
    }

    private Specification<UserLevelRecord> getConditionId(List<Long> userIds) {
        Specification<UserLevelRecord> specification = (root, criteriaQuery, cb) -> {
            List<Predicate> list = new ArrayList<>();
            if (userIds != null && userIds.size() > 0) {
                Path<Object> userId = root.get("id");
                CriteriaBuilder.In<Object> in = cb.in(userId);
                for (Long id : userIds) {
                    in.value(id);
                }
                list.add(cb.and(cb.and(in)));
            }
            return cb.and(list.toArray(new Predicate[list.size()]));
        };
        return specification;
    }


    /**
     * 查询条件拼接，灵活添加条件
     *
     * @param
     * @return
     */
    private Specification<UserLevelRecord> getCondition(UserLevelRecord userLevel) {
        Specification<UserLevelRecord> specification = (root, criteriaQuery, cb) -> {
            Predicate predicate;
            List<Predicate> list = new ArrayList<>();
            if (userLevel.getUserId() != null) {
                list.add(cb.equal(root.get("userId").as(Long.class), userLevel.getUserId()));
            }
            predicate = cb.and(list.toArray(new Predicate[list.size()]));
            return predicate;
        };
        return specification;
    }

    public void levelChange(LevelChangeCo levelChangeCo) {
        try {
            LevelWaterChange levelWaterChange = new LevelWaterChange();
            levelWaterChange.setBetWater(levelChangeCo.getBetWater());
            levelWaterChange.setUserId(levelChangeCo.getUserId());
            GameRecord gameRecord = levelChangeCo.getGameRecord();
            if (Constants.PLATFORM_WM.equals(levelChangeCo.getPlatform())) {
                levelWaterChange.setGameId(gameRecord.getGid().toString());
            } else {
                levelWaterChange.setGameId(gameRecord.getGameCode());
            }
            levelWaterChange.setGameName(gameRecord.getGname());
            levelWaterChange.setGameRecordId(gameRecord.getId());
            levelWaterChange.setUserId(levelChangeCo.getUserId());
            levelWaterChangeService.save(levelWaterChange);
            if (levelChangeCo.getTradeType() == 1) {
                userMoneyService.addRiseWater(levelChangeCo.getUserId(), levelChangeCo.getBetWater());
                userMoneyService.addLevelWater(levelChangeCo.getUserId(), levelChangeCo.getBetWater());
            } else {
                userMoneyService.subRiseWater(levelChangeCo.getUserId(), levelChangeCo.getBetWater());
                userMoneyService.subLevelWater(levelChangeCo.getUserId(), levelChangeCo.getBetWater());
            }
        } catch (Exception e) {
            log.error("等级流水变动异常 ===== >> levelChangeCo {} ,错误信息 {} ", JSON.toJSONString(levelChangeCo), e);
            throw new RuntimeException("等级流水变动异常");
        }

    }

    public void updateTodayKeepStatusById(UserLevelRecord userLevelRecord) {
        userLevelRepository.updateTodayKeepStatusById(userLevelRecord.getTodayKeepStatus(), new Date(), userLevelRecord.getId());
    }

    public void modifyUserLevel(UserLevelRecord userLevelRecord) {
        userLevelRepository.save(userLevelRecord);
    }

}
