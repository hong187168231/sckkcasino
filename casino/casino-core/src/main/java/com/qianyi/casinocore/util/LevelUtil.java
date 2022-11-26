package com.qianyi.casinocore.util;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.exception.BusinessException;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.vo.LevelConfigDto;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class LevelUtil {


    public static Map<String, Object> getLevelInfoByBet(PlatformConfig platformConfig,Integer ValidBet) {
        String text = platformConfig.getVipConfigInfo();
        Map result = new HashMap();
        if (StringUtils.isNotBlank(text)) {
            LevelConfigDto levelConfigDto = JSON.parseObject(platformConfig.getVipConfigInfo(), LevelConfigDto.class);
            if (ValidBet >= levelConfigDto.getLevel1().getUpgradeBet()) {
                LevelConfigDto.Level1 level1 = levelConfigDto.getLevel1();
                result.put("upLevel", 1);
                result.put("upgradeBet", level1.getUpgradeBet());
                result.put("keepBet", level1.getKeepBet());
                result.put("upgradeAward", level1.getUpgradeAward());
            }
            if (ValidBet >= levelConfigDto.getLevel2().getUpgradeBet()) {
                LevelConfigDto.Level2 level2 = levelConfigDto.getLevel2();
                result.put("upLevel", 2);
                result.put("upgradeBet", level2.getUpgradeBet());
                result.put("keepBet", level2.getKeepBet());
                result.put("upgradeAward", level2.getUpgradeAward());
            }
            if (ValidBet >= levelConfigDto.getLevel3().getUpgradeBet()) {
                LevelConfigDto.Level3 level3 = levelConfigDto.getLevel3();
                result.put("upLevel", 3);
                result.put("upgradeBet", level3.getUpgradeBet());
                result.put("keepBet", level3.getKeepBet());
                result.put("upgradeAward", level3.getUpgradeAward());
            }
            if (ValidBet >= levelConfigDto.getLevel4().getUpgradeBet()) {
                LevelConfigDto.Level4 level4 = levelConfigDto.getLevel4();
                result.put("upLevel", 4);
                result.put("upgradeBet", level4.getUpgradeBet());
                result.put("keepBet", level4.getKeepBet());
                result.put("upgradeAward", level4.getUpgradeAward());
            }
            if (ValidBet >= levelConfigDto.getLevel5().getUpgradeBet()) {
                LevelConfigDto.Level5 level5 = levelConfigDto.getLevel5();
                result.put("upLevel", 5);
                result.put("upgradeBet", level5.getUpgradeBet());
                result.put("keepBet", level5.getKeepBet());
                result.put("upgradeAward", level5.getUpgradeAward());
            }
            if (ValidBet >= levelConfigDto.getLevel6().getUpgradeBet()) {
                LevelConfigDto.Level6 level6 = levelConfigDto.getLevel6();
                result.put("upLevel", 6);
                result.put("upgradeBet", level6.getUpgradeBet());
                result.put("keepBet", level6.getKeepBet());
                result.put("upgradeAward", level6.getUpgradeAward());
            }
            if (ValidBet >= levelConfigDto.getLevel7().getUpgradeBet()) {
                LevelConfigDto.Level7 level7 = levelConfigDto.getLevel7();
                result.put("upLevel", 7);
                result.put("upgradeBet", level7.getUpgradeBet());
                result.put("keepBet", level7.getKeepBet());
                result.put("upgradeAward", level7.getUpgradeAward());
            }
            if (ValidBet >= levelConfigDto.getLevel8().getUpgradeBet()) {
                LevelConfigDto.Level8 level8 = levelConfigDto.getLevel8();
                result.put("upLevel", 8);
                result.put("upgradeBet", level8.getUpgradeBet());
                result.put("keepBet", level8.getKeepBet());
                result.put("upgradeAward", level8.getUpgradeAward());
            }
            if (ValidBet >= levelConfigDto.getLevel9().getUpgradeBet()) {
                LevelConfigDto.Level9 level9 = levelConfigDto.getLevel9();
                result.put("upLevel", 9);
                result.put("upgradeBet", level9.getUpgradeBet());
                result.put("keepBet", level9.getKeepBet());
                result.put("upgradeAward", level9.getUpgradeAward());
            }
            if (ValidBet >= levelConfigDto.getLevel10().getUpgradeBet()) {
                LevelConfigDto.Level10 level10 = levelConfigDto.getLevel10();
                result.put("upLevel", 10);
                result.put("upgradeBet", level10.getUpgradeBet());
                result.put("keepBet", level10.getKeepBet());
                result.put("upgradeAward", level10.getUpgradeAward());
            }
        }
        return result;
    }

    public static Map<String, Integer> getLevelInfoByLevel(PlatformConfig platformConfig,Integer level) {
        String text = platformConfig.getVipConfigInfo();
        Map result = new HashMap();
        Integer upgradeBet = 0;
        Integer keepBet = 0;
        Integer todayAward = 0;
        Integer riseAward = 0;
        if (StringUtils.isNotBlank(text)) {
            LevelConfigDto levelConfigDto = JSON.parseObject(platformConfig.getVipConfigInfo(), LevelConfigDto.class);
            if(level>=10){
                level =10;
            }
            switch (level) {
                case 0:
                case 1:
                    LevelConfigDto.Level1 level1 = levelConfigDto.getLevel1();
                    upgradeBet = level1.getUpgradeBet();
                    keepBet = level1.getKeepBet();
                    todayAward = level1.getTodayAward();
                    riseAward = level1.getUpgradeAward();
                    break;
                case 2:
                    LevelConfigDto.Level2 level2 = levelConfigDto.getLevel2();
                    upgradeBet = level2.getUpgradeBet();
                    keepBet = level2.getKeepBet();
                    todayAward = level2.getTodayAward();
                    riseAward = level2.getUpgradeAward();
                    break;
                case 3:
                    LevelConfigDto.Level3 level3 = levelConfigDto.getLevel3();
                    upgradeBet = level3.getUpgradeBet();
                    keepBet = level3.getKeepBet();
                    todayAward = level3.getTodayAward();
                    riseAward = level3.getUpgradeAward();
                    break;
                case 4:
                    LevelConfigDto.Level4 level4 = levelConfigDto.getLevel4();
                    upgradeBet = level4.getUpgradeBet();
                    keepBet = level4.getKeepBet();
                    todayAward = level4.getTodayAward();
                    riseAward = level4.getUpgradeAward();
                    break;
                case 5:
                    LevelConfigDto.Level5 level5 = levelConfigDto.getLevel5();
                    upgradeBet = level5.getUpgradeBet();
                    keepBet = level5.getKeepBet();
                    todayAward = level5.getTodayAward();
                    riseAward = level5.getUpgradeAward();
                    break;
                case 6:
                    LevelConfigDto.Level6 level6 = levelConfigDto.getLevel6();
                    upgradeBet = level6.getUpgradeBet();
                    keepBet = level6.getKeepBet();
                    todayAward = level6.getTodayAward();
                    riseAward = level6.getUpgradeAward();
                    break;
                case 7:
                    LevelConfigDto.Level7 level7 = levelConfigDto.getLevel7();
                    upgradeBet = level7.getUpgradeBet();
                    keepBet = level7.getKeepBet();
                    todayAward = level7.getTodayAward();
                    riseAward = level7.getUpgradeAward();
                    break;
                case 8:
                    LevelConfigDto.Level8 level8 = levelConfigDto.getLevel8();
                    upgradeBet = level8.getUpgradeBet();
                    keepBet = level8.getKeepBet();
                    todayAward = level8.getTodayAward();
                    riseAward = level8.getUpgradeAward();
                    break;
                case 9:
                    LevelConfigDto.Level9 level9 = levelConfigDto.getLevel9();
                    upgradeBet = level9.getUpgradeBet();
                    keepBet = level9.getKeepBet();
                    todayAward = level9.getTodayAward();
                    riseAward = level9.getUpgradeAward();
                    break;
                case 10:
                    LevelConfigDto.Level10 level10 = levelConfigDto.getLevel10();
                    upgradeBet = level10.getUpgradeBet();
                    keepBet = level10.getKeepBet();
                    todayAward = level10.getTodayAward();
                    riseAward = level10.getUpgradeAward();
                    break;
                default:
                    throw new BusinessException("参数不合法");
            }
        }
        result.put("upgradeBet", upgradeBet);
        result.put("keepBet", keepBet);
        result.put("todayAward", todayAward);
        result.put("riseAward", riseAward);
        return result;
    }

    public static Integer getUpgradeBet(PlatformConfig platformConfig,Integer level) {
        Map<String, Integer>  result =  getLevelInfoByLevel( platformConfig, level+1);
        if(CollUtil.isEmpty(result)){
            return  Integer.MAX_VALUE;
        }
        return result.get("upgradeBet");
    }

    public static Integer getKeepBet(PlatformConfig platformConfig,Integer level) {
        Map<String, Integer>  result =  getLevelInfoByLevel( platformConfig, level);
        if(CollUtil.isEmpty(result)){
            return  Integer.MAX_VALUE;
        }
        return result.get("keepBet");
    }



    public static Map<String, Float> getLevelInfoCodeNum(PlatformConfig platformConfig) {
        String text = platformConfig.getVipConfigInfo();
        Map result = new HashMap();
        Float todayCodeRate = 0F;
        Float upgradeCodeRate = 0F;

        if (StringUtils.isNotBlank(text)) {
            LevelConfigDto levelConfigDto = JSON.parseObject(platformConfig.getVipConfigInfo(), LevelConfigDto.class);
            todayCodeRate = levelConfigDto.getTodayCodeRate();
            upgradeCodeRate = levelConfigDto.getUpgradeCodeRate();
        }
        result.put("todayCodeRate", todayCodeRate);
        result.put("upgradeCodeRate", upgradeCodeRate);
        return result;
    }

}
