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
import com.qianyi.casinocore.vo.UserLevelVo;
import com.qianyi.modulecommon.executor.AsyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户钱包操作
 */
@Slf4j
@Service
@Transactional
public class UserLevelBusiness {

    @Autowired
    private LevelWaterChangeService levelWaterChangeService;
    @Autowired
    private PlatformConfigService platformConfigService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserLevelService userLevelService;
    @Autowired
    private UserMoneyBusiness userMoneyBusiness;
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
            LevelConfigDto levelConfigDto = JSON.parseObject(platformConfig.getVipConfigInfo(), LevelConfigDto.class);
            userLevelVo.setLevelConfig(levelConfigDto);
        }
        // 用户 奖励领取是否能领取,及领取金额
        Boolean TodayAwardFlag = getUserLevelAndSchedule(user, 1);
        Boolean riseAwardFlag = getUserLevelAndSchedule(user, 2);
        if (TodayAwardFlag) {
            userLevelVo.setTodayAwardFlag(true);
            userLevelVo.setTodayAward(new BigDecimal(levelConfig.get("todayAward")));
        }
        if (riseAwardFlag) {
            userLevelVo.setRiseAwardFlag(true);
            List<AwardReceiveRecord> upgradeAwardList = getMaxRiseAward(user.getId());
            if (CollUtil.isNotEmpty(upgradeAwardList)) {
                if (upgradeAwardList.get(upgradeAwardList.size() - 1).getReceiveStatus().equals(0)) {
                    userLevelVo.setRiseAward(upgradeAwardList.get(upgradeAwardList.size() - 1).getAmount());
                }
            } else {
                userLevelVo.setRiseAward(new BigDecimal(levelConfig.get("riseAward")));
            }
        }
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
                List<AwardReceiveRecord> upgradeAwardList = getMaxRiseAward(user.getId());
                if (CollUtil.isNotEmpty(upgradeAwardList)) {
                    if (upgradeAwardList.get(upgradeAwardList.size() - 1).getReceiveStatus().equals(0)) {
                        return true;
                    }
                } else {
                    if (levelConfigDto.getLevel1().getUpgradeAward().equals(0) && user.getLevel().equals(1) && awardType.equals(2)) {
                        return false;
                    }
                }
            }
        }
        return false;
    }


    public List<AwardReceiveRecord> getMaxRiseAward(Long userId) {
        List<AwardReceiveRecord> upgradeAwardList = awardReceiveRecordService.countUpgradeAward(userId);
        return upgradeAwardList;
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
    public boolean receiveAward(Long userId, Integer awardType) {
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
            // 处理打码量
            processCodeNum(userMoney, awardReceiveRecord.getAmount(), 1);
            // 记录账变
            vo.setChangeEnum(AccountChangeEnum.TODAY_AWARD);
            vo.setAmount(awardReceiveRecord.getAmount());
            vo.setAmountAfter(userMoney.getMoney().add(awardReceiveRecord.getAmount()));
            asyncService.executeAsync(vo);
        } else if (awardType == 2) { // 晋级奖励
            AwardReceiveRecord awardReceiveRecord = awardReceiveRecordService.queryMaxUpgradeAward(userId);
            if (ObjectUtil.isNull(awardReceiveRecord)) {
                throw new BusinessException("暂无可领取的升级奖励!");
            }
            awardReceiveRecord.setReceiveTime(new Date());
            awardReceiveRecord.setReceiveStatus(1);
            awardReceiveRecordService.save(awardReceiveRecord);
            awardReceiveRecordService.modifyIsReceive(awardReceiveRecord.getUserId());
            // 增加用户余额
            userMoneyService.addMoney(userId, awardReceiveRecord.getAmount());
            // 处理打码量
            processCodeNum(userMoney, awardReceiveRecord.getAmount(), 2);
            // 记录账变
            vo.setChangeEnum(AccountChangeEnum.UPGRADE_AWARD);
            vo.setAmount(awardReceiveRecord.getAmount());
            vo.setAmountAfter(userMoney.getMoney().add(awardReceiveRecord.getAmount()));
            asyncService.executeAsync(vo);
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
            // 保存登记变更记录
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
            // 插入升级奖励记录
            UserLevelRecord dropLevelRecord = userLevelService.findDropRecord(userId, upLevel);
            if (ObjectUtil.isNull(dropLevelRecord)) {
                int row = awardReceiveRecordService.countRiseAwardNum(userId, upLevel);
                if (row < 1) {
                    AwardReceiveRecord awardReceiveRecord = new AwardReceiveRecord();
                    awardReceiveRecord.setUserId(user.getId());
                    awardReceiveRecord.setReceiveStatus(0);
                    awardReceiveRecord.setAwardType(2);
                    awardReceiveRecord.setAmount(new BigDecimal(upgradeAward));
                    awardReceiveRecord.setLevel(upLevel);
                    awardReceiveRecordService.save(awardReceiveRecord);
                }
            }
            userMoneyService.modifyLevelWater(levelChangeCo.getUserId(), BigDecimal.ZERO);
            if (upLevel == 10) {
                userMoneyService.modifyRiseWater(userId, new BigDecimal(upgradeBet));
            }
            userService.updateLevel(levelChangeCo.getUserId(), upLevel);
        }
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
        platformConfig.setVipConfigInfo("{\"level5\":{\"upgradeBet\":25000,\"todayAward\":15,\"keepBet\":5000,\"upgradeAward\":20},\"level4\":{\"upgradeBet\":12000,\"todayAward\":10,\"keepBet\":4000,\"upgradeAward\":15},\"level7\":{\"upgradeBet\":200000,\"todayAward\":30,\"keepBet\":50000,\"upgradeAward\":50},\"level6\":{\"upgradeBet\":75000,\"todayAward\":20,\"keepBet\":25000,\"upgradeAward\":30},\"level9\":{\"upgradeBet\":1000000,\"todayAward\":70,\"keepBet\":200000,\"upgradeAward\":100},\"level8\":{\"upgradeBet\":500000,\"todayAward\":50,\"keepBet\":100000,\"upgradeAward\":70},\"todayCodeRate\":3,\"level10\":{\"upgradeBet\":5000000,\"todayAward\":100,\"keepBet\":500000,\"upgradeAward\":200},\"level1\":{\"upgradeBet\":0,\"todayAward\":0,\"keepBet\":0,\"upgradeAward\":0},\"level3\":{\"upgradeBet\":5000,\"todayAward\":6,\"keepBet\":1500,\"upgradeAward\":10},\"upgradeCodeRate\":3,\"level2\":{\"upgradeBet\":1500,\"todayAward\":3,\"keepBet\":500,\"upgradeAward\":6}}");
        return platformConfig;
    }


}
