package com.qianyi.casinocore.business;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.co.user.LevelChangeCo;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.exception.BusinessException;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.LevelUtil;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.casinocore.vo.LevelConfigDto;
import com.qianyi.casinocore.vo.LevelConfigView;
import com.qianyi.casinocore.vo.UserLevelVo;
import com.qianyi.modulecommon.executor.AsyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * 用户钱包操作
 */
@Slf4j
@Service
@Transactional
public class UserLevelBusiness {

    @Autowired
    private PlatformConfigService platformConfigService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserLevelService userLevelService;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    @Qualifier("accountChangeJob")
    AsyncService asyncService;
    @Autowired
    private AwardReceiveRecordService awardReceiveRecordService;
    @Autowired
    private CodeNumChangeService codeNumChangeService;


    /**
     * 用户等级页信息处理
     *
     * @param userId
     * @return
     */
    public UserLevelVo userLevelInfo(Long userId) {
        PlatformConfig platformConfig = platformConfig();
        UserLevelVo userLevelVo = new UserLevelVo();
        User user = userService.findById(userId);
        UserMoney userMoney = userMoneyService.findByUserId(userId);
        BigDecimal riseWater = userMoney.getRiseWater();
        BigDecimal levelWater = userMoney.getLevelWater();
        userLevelVo.setLevel(user.getLevel());
        Integer upgradeBet = LevelUtil.getUpgradeBet(platformConfig, user.getLevel());
        // 如果用户等级大于10级则当前流水只展示等级配置的最高值
        if (user.getLevel() >= 10) {
            upgradeBet = LevelUtil.getUpgradeBet(platformConfig, user.getLevel());
            riseWater = new BigDecimal(upgradeBet);
        }
        userLevelVo.setCurrentWater(riseWater);
        Map<String, Integer> levelConfig = LevelUtil.getLevelInfoByLevel(platformConfig, user.getLevel());
        Integer keepBet = levelConfig.get("keepBet");
        userLevelVo.setNextLevel(user.getLevel() + 1);
        userLevelVo.setRiseWater(new BigDecimal(upgradeBet));
        userLevelVo.setRiseNeedWater(new BigDecimal(upgradeBet).subtract(riseWater));
        // 如果用户已经满级处理
        if (user.getLevel() == 10) {
            userLevelVo.setNextLevel(10);
            userLevelVo.setRiseNeedWater(BigDecimal.ZERO);
        }
        // 用户流水小于保级流水才展示保级信息
        if (user.getLevel() > 1) {
            userLevelVo.setShowKeepFlag(true);
            BigDecimal keepNeedBet = new BigDecimal(keepBet).subtract(levelWater);
            if (keepNeedBet.intValue() < 0) {
                keepNeedBet = BigDecimal.ZERO;
            }
            userLevelVo.setKeepNeedBet(keepNeedBet);
        }
        String text = platformConfig.getVipConfigInfo();
        if (StringUtils.isNotBlank(text)) {
            LevelConfigView levelConfigView = JSON.parseObject(platformConfig.getVipConfigInfo(), LevelConfigView.class);
            userLevelVo.setLevelConfig(levelConfigView);
        }
        // 用户 奖励领取是否能领取,及领取金额
        Boolean TodayAwardFlag = getUserLevelAndSchedule(user, 1);
        if (TodayAwardFlag) {
            userLevelVo.setTodayAwardFlag(true);
        }
        userLevelVo.setTodayAward(new BigDecimal(levelConfig.get("todayAward")));
        AwardReceiveRecord todayAward = awardReceiveRecordService.selectAwardReceiveByTime(user.getId());
        if(ObjectUtil.isNotNull(todayAward)){
            userLevelVo.setTodayAward(todayAward.getAmount());
        }
        riseAwardIsReceive(user, userLevelVo);
        return userLevelVo;
    }

    /**
     * 查询用户奖励是否可领取
     *
     * @param user
     * @param awardType
     * @return
     */
    private boolean getUserLevelAndSchedule(User user, Integer awardType) {
        PlatformConfig platformConfig = platformConfig();
        String text = platformConfig.getVipConfigInfo();
        if (StringUtils.isNotBlank(text)) {
            LevelConfigDto levelConfigDto = JSON.parseObject(platformConfig.getVipConfigInfo(), LevelConfigDto.class);
            if (user.getLevel().equals(1)) {
                if (levelConfigDto.getLevel1().getTodayAward().equals(0) && awardType.equals(1)) {
                    return false;
                }
            }
            if (awardType == 1) {
                int award = awardReceiveRecordService.countTodayAward(user.getId());
                if (award < 1) {
                    return true;
                }
            } else {
                int riseNum = awardReceiveRecordService.countNotReceiveRiseAwardAll(user.getId());
                if (riseNum > 0) {
                    return true;
                } else {
                    if (levelConfigDto.getLevel1().getUpgradeAward().equals(0) && user.getLevel().equals(1) && awardType.equals(2)) {
                        return false;
                    }
                }
            }
        }
        return false;
    }


    private void riseAwardIsReceive(User user, UserLevelVo userLevelVo) {
        PlatformConfig platformConfig = platformConfig();
        String text = platformConfig.getVipConfigInfo();
        LevelConfigView levelConfigView = userLevelVo.getLevelConfig();
        if (StringUtils.isNotBlank(text)) {
            for (int i = 1; i <= 10; i++) {
                Boolean flag = awardReceiveRecordService.countNotReceiveRiseAwardNum(user.getId(), i) > 0;
                switch (i) {
                    case 1:
                        levelConfigView.getLevel1().setHasRiseFlag(flag);
                        break;
                    case 2:
                        levelConfigView.getLevel2().setHasRiseFlag(flag);
                        break;
                    case 3:
                        levelConfigView.getLevel3().setHasRiseFlag(flag);
                        break;
                    case 4:
                        levelConfigView.getLevel4().setHasRiseFlag(flag);
                        break;
                    case 5:
                        levelConfigView.getLevel5().setHasRiseFlag(flag);
                        break;
                    case 6:
                        levelConfigView.getLevel6().setHasRiseFlag(flag);
                        break;
                    case 7:
                        levelConfigView.getLevel7().setHasRiseFlag(flag);
                        break;
                    case 8:
                        levelConfigView.getLevel8().setHasRiseFlag(flag);
                        break;
                    case 9:
                        levelConfigView.getLevel9().setHasRiseFlag(flag);
                        break;
                    case 10:
                        levelConfigView.getLevel10().setHasRiseFlag(flag);
                        break;
                }
            }
        }
    }


    public boolean userHasAward(User user) {
        boolean todayFlag = getUserLevelAndSchedule(user, 1);
        boolean riseFlag = getUserLevelAndSchedule(user, 2);
        if (todayFlag || riseFlag) {
            return true;

        }
        return false;
    }


    /**
     * 领取奖励
     *
     * @param userId
     * @param awardType
     * @return
     */
    @Transactional
    public boolean receiveAward(Long userId, Integer awardType, Integer level) {
        User user = userService.findById(userId);
        boolean flag = getUserLevelAndSchedule(user, awardType);
        if (!flag) {
            throw new BusinessException("暂无领取资格!");
        }
        UserMoney userMoney = userMoneyService.findByUserId(userId);
        Map<String, Integer> result = LevelUtil.getLevelInfoByLevel(platformConfig(), user.getLevel());
        //账变中心记录账变
        AccountChangeVo vo = new AccountChangeVo();
        vo.setUserId(userId);
        vo.setAmountBefore(userMoney.getMoney());
        // 每日奖励
        if (awardType == 1) {
            AwardReceiveRecord awardReceiveRecord = new AwardReceiveRecord();
            awardReceiveRecord.setUserId(userId);
            awardReceiveRecord.setReceiveStatus(1);
            awardReceiveRecord.setAwardType(1);
            awardReceiveRecord.setLevel(user.getLevel());
            awardReceiveRecord.setAmount(new BigDecimal(result.get("todayAward")));
            awardReceiveRecordService.save(awardReceiveRecord);
            // 增加用户余额
            userMoneyService.addMoney(userId, awardReceiveRecord.getAmount());
            userMoneyService.addBalance(userId, awardReceiveRecord.getAmount());
            // 处理打码量
            processCodeNum(userMoney, awardReceiveRecord.getAmount(), 1);
            // 记录账变
            vo.setChangeEnum(AccountChangeEnum.TODAY_AWARD);
            vo.setAmount(awardReceiveRecord.getAmount());
            vo.setAmountAfter(userMoney.getMoney().add(awardReceiveRecord.getAmount()));
            asyncService.executeAsync(vo);
        } else if (awardType == 2) { // 晋级奖励
            AwardReceiveRecord awardReceiveRecord = awardReceiveRecordService.selectNotReceiveRiseAward(userId, level);
            if (ObjectUtil.isNull(awardReceiveRecord)) {
                throw new BusinessException("暂无可领取的升级奖励!");
            }
            // 增加用户余额
            userMoneyService.addMoney(userId, awardReceiveRecord.getAmount());
            userMoneyService.addBalance(userId, awardReceiveRecord.getAmount());
            // 处理打码量
            processCodeNum(userMoney, awardReceiveRecord.getAmount(), 2);
            // 记录账变
            vo.setChangeEnum(AccountChangeEnum.UPGRADE_AWARD);
            vo.setAmount(awardReceiveRecord.getAmount());
            vo.setAmountAfter(userMoney.getMoney().add(awardReceiveRecord.getAmount()));
            asyncService.executeAsync(vo);
            awardReceiveRecord.setReceiveTime(new Date());
            awardReceiveRecord.setReceiveStatus(1);
            awardReceiveRecordService.save(awardReceiveRecord);
        }
        return true;
    }


    private void processCodeNum(UserMoney userMoney, BigDecimal awardAmount, Integer awardType) {
        Map<String, Float> result = LevelUtil.getLevelInfoCodeNum(platformConfig());
        BigDecimal codeNumBefore = userMoney.getCodeNum();
        Float todayCodeRate = result.get("todayCodeRate");
        Float upgradeCodeRate = result.get("upgradeCodeRate");
        Float rate;
        if (awardType == 1) {
            rate = todayCodeRate;
        } else {
            rate = upgradeCodeRate;
        }
        BigDecimal codeNum = awardAmount.multiply(new BigDecimal(rate));
        userMoneyService.addCodeNum(userMoney.getUserId(), codeNum);
        BigDecimal codeNumAfter = codeNumBefore.add(codeNum);
        CodeNumChange codeNumChange = CodeNumChange.setCodeNumChange(userMoney.getUserId(), null, codeNum, codeNumBefore, codeNumAfter);
        if (awardType.equals(1)) {
            codeNumChange.setType(6);
        } else {
            codeNumChange.setType(7);
        }
        codeNumChange.setPlatform("award");
        codeNumChangeService.save(codeNumChange);
    }


    /**
     * 处理升级
     *
     * @param userId
     */
    public void processUserLevel(Long userId, String platform, GameRecord gameRecord) {
        BigDecimal betAmount = new BigDecimal(gameRecord.getValidbet());
        // 保存等级投注流水和处理等级进度
        LevelChangeCo levelChangeCo = new LevelChangeCo();
        levelChangeCo.setUserId(userId);
        levelChangeCo.setPlatform(platform);
        levelChangeCo.setBetWater(betAmount);
        levelChangeCo.setGameRecord(gameRecord);
        userLevelService.levelChange(levelChangeCo);

        UserMoney userMoney = userMoneyService.findByUserId(userId);
        BigDecimal riseWater = userMoney.getRiseWater();
        Map<String, Object> result = LevelUtil.getLevelInfoByBet(platformConfig(), riseWater.intValue());
        if (CollUtil.isEmpty(result)) {
            return;
        }

        User user = userService.findById(userId);
        Integer beforeLevel = user.getLevel();
        Integer upLevel = (Integer) result.get("upLevel");
        Integer upgradeBet = (Integer) result.get("upgradeBet");
        Integer upgradeAward = (Integer) result.get("upgradeAward");
        // 下一次 升级等级
        Map<String, Integer> upResult = LevelUtil.getLevelInfoByLevel(platformConfig(), upLevel + 1);
        if (upLevel > beforeLevel) {
            if (upLevel > 10) {
                return;
            }
            Integer diss = upLevel - beforeLevel;
            if (diss >= 1) {
                // 保存等级变更记录
                UserLevelRecord userLevelRecord = new UserLevelRecord();
                userLevelRecord.setUserId(user.getId());
                userLevelRecord.setBeforeLevel(user.getLevel());
                userLevelRecord.setChangeType(1);
                userLevelRecord.setCreateBy("system");
                userLevelRecord.setLevel(upLevel);
                userLevelRecord.setSchedule(riseWater + "/" + new BigDecimal(upResult.get("upgradeBet")) + ".00");
                userLevelRecord.setRiseTime(new Date());
                userLevelRecord.setTodayKeepStatus(0);
                userLevelService.save(userLevelRecord);

                for (int i = 1; i <= diss; i++) {
                    int level = upLevel - i + 1;
                    if (level <= 1) {
                        return;
                    }
                    int row = awardReceiveRecordService.countRiseAwardNum2(userId, level);
                    if (row < 1) {
                        AwardReceiveRecord awardReceiveRecord = new AwardReceiveRecord();
                        awardReceiveRecord.setUserId(user.getId());
                        awardReceiveRecord.setReceiveStatus(0);
                        awardReceiveRecord.setAwardType(2);
                        awardReceiveRecord.setLevel(level);
                        awardReceiveRecord.setAmount(new BigDecimal(getRiseAward(level)));
                        awardReceiveRecordService.save(awardReceiveRecord);
                    }
                }
            }
            userMoneyService.modifyLevelWater(levelChangeCo.getUserId(), BigDecimal.ZERO);
            if (upLevel == 10) {
                userMoneyService.modifyRiseWater(userId, new BigDecimal(upgradeBet));
            }
            userService.updateLevel(levelChangeCo.getUserId(), upLevel);
        }
    }

    public Integer getRiseAward(Integer level) {
        Map<String, Integer> result = LevelUtil.getLevelInfoByLevel(platformConfig(), level);
        Integer riseAward = result.get("riseAward");
        return riseAward;
    }


    public void processUserKeepLevel(Long userId) {
        UserMoney userMoney = userMoneyService.findByUserId(userId);
        User user = userService.findById(userId);
        BigDecimal levelWater = userMoney.getLevelWater();
        Map<String, Integer> result = LevelUtil.getLevelInfoByLevel(platformConfig(), user.getLevel());
        if (CollUtil.isEmpty(result)) {
            return;
        }
        Map<String, Integer> preResult = LevelUtil.getLevelInfoByLevel(platformConfig(), user.getLevel() - 1);
        if (CollUtil.isEmpty(preResult)) {
            return;
        }

        Integer keepBet = result.get("keepBet");
        if (userMoney.getLevelWater().intValue() < keepBet) {
            Integer preUpgradeBet = preResult.get("upgradeBet");

            Integer beforeLevel = user.getLevel();
            UserLevelRecord userLevelRecord = new UserLevelRecord();
            userLevelRecord.setUserId(user.getId());
            userLevelRecord.setBeforeLevel(beforeLevel);
            userLevelRecord.setChangeType(2);
            userLevelRecord.setCreateBy("system");
            userLevelRecord.setLevel(beforeLevel - 1);
            userLevelRecord.setSchedule(levelWater + "/" + keepBet + ".00");
            userLevelRecord.setDropTime(new Date());
            userLevelRecord.setTodayKeepStatus(0);
            userLevelService.save(userLevelRecord);
            userMoneyService.modifyLevelWater(userId, BigDecimal.ZERO);
            userMoneyService.modifyRiseWater(userId, new BigDecimal(preUpgradeBet));
            userService.updateLevel(userId, beforeLevel - 1);
        }
    }


    public PlatformConfig platformConfig() {
        PlatformConfig platformConfig = platformConfigService.findFirst();
        return platformConfig;
    }


    public static void main(String[] args) {
        Integer upLevel = 10;
        Integer beforeLevel = 1;
        Integer diss = upLevel - beforeLevel;
        if (diss > 1) {
            for (int i = 1; i <= diss; i++) {
                System.out.println(upLevel - i + 1);
            }
        }
    }


}
