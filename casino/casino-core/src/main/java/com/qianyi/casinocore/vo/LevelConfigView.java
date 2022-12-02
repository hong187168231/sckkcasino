package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel
@Data
public class LevelConfigView implements Serializable {

    @ApiModelProperty(value = "vip1信息")
    public Level1 level1;
    @ApiModelProperty(value = "vip2信息")
    private Level2 level2;
    @ApiModelProperty(value = "vip3信息")
    private Level3 level3;
    @ApiModelProperty(value = "vip4信息")
    private Level4 level4;
    @ApiModelProperty(value = "vip5信息")
    private Level5 level5;
    @ApiModelProperty(value = "vip6信息")
    private Level6 level6;
    @ApiModelProperty(value = "vip7信息")
    private Level7 level7;
    @ApiModelProperty(value = "vip8信息")
    private Level8 level8;
    @ApiModelProperty(value = "vip9信息")
    private Level9 level9;
    @ApiModelProperty(value = "vip10信息")
    private Level10 level10;

    @ApiModelProperty(value = "每日奖励打码倍率")
    private Float todayCodeRate;
    @ApiModelProperty(value = "升级奖励打码倍率")
    private Float upgradeCodeRate;

    @Data
    public class Level1 {
        @ApiModelProperty(value = "升级有效投注")
        public Integer upgradeBet;
        @ApiModelProperty(value = "保级有效投注")
        private Integer keepBet;
        @ApiModelProperty(value = "每日奖励")
        private Integer todayAward;
        @ApiModelProperty(value = "升级奖励")
        private Integer upgradeAward;
        @ApiModelProperty(value = "升级奖励是否可领取")
        private Boolean hasRiseFlag;
    }

    @Data
    public class Level2 {
        @ApiModelProperty(value = "升级有效投注")
        private Integer upgradeBet;
        @ApiModelProperty(value = "保级有效投注")
        private Integer keepBet;
        @ApiModelProperty(value = "每日奖励")
        private Integer todayAward;
        @ApiModelProperty(value = "升级奖励")
        private Integer upgradeAward;
        @ApiModelProperty(value = "升级奖励是否可领取")
        private Boolean hasRiseFlag;
    }

    @Data
    public class Level3 {
        @ApiModelProperty(value = "升级有效投注")
        private Integer upgradeBet;
        @ApiModelProperty(value = "保级有效投注")
        private Integer keepBet;
        @ApiModelProperty(value = "每日奖励")
        private Integer todayAward;
        @ApiModelProperty(value = "升级奖励")
        private Integer upgradeAward;
        @ApiModelProperty(value = "升级奖励是否可领取")
        private Boolean hasRiseFlag;
    }

    @Data
    public class Level4 {
        @ApiModelProperty(value = "升级有效投注")
        private Integer upgradeBet;
        @ApiModelProperty(value = "保级有效投注")
        private Integer keepBet;
        @ApiModelProperty(value = "每日奖励")
        private Integer todayAward;
        @ApiModelProperty(value = "升级奖励")
        private Integer upgradeAward;
        @ApiModelProperty(value = "升级奖励是否可领取")
        private Boolean hasRiseFlag = false;
    }

    @Data
    public class Level5 {
        @ApiModelProperty(value = "升级有效投注")
        private Integer upgradeBet;
        @ApiModelProperty(value = "保级有效投注")
        private Integer keepBet;
        @ApiModelProperty(value = "每日奖励")
        private Integer todayAward;
        @ApiModelProperty(value = "升级奖励")
        private Integer upgradeAward;
        @ApiModelProperty(value = "升级奖励是否可领取")
        private Boolean hasRiseFlag;
    }

    @Data
    public class Level6 {
        @ApiModelProperty(value = "升级有效投注")
        private Integer upgradeBet;
        @ApiModelProperty(value = "保级有效投注")
        private Integer keepBet;
        @ApiModelProperty(value = "每日奖励")
        private Integer todayAward;
        @ApiModelProperty(value = "升级奖励")
        private Integer upgradeAward;
        @ApiModelProperty(value = "升级奖励是否可领取")
        private Boolean hasRiseFlag;
    }

    @Data
    public class Level7 {
        @ApiModelProperty(value = "升级有效投注")
        private Integer upgradeBet;
        @ApiModelProperty(value = "保级有效投注")
        private Integer keepBet;
        @ApiModelProperty(value = "每日奖励")
        private Integer todayAward;
        @ApiModelProperty(value = "升级奖励")
        private Integer upgradeAward;
        @ApiModelProperty(value = "升级奖励是否可领取")
        private Boolean hasRiseFlag;
    }

    @Data
    public class Level8 {
        @ApiModelProperty(value = "升级有效投注")
        private Integer upgradeBet;
        @ApiModelProperty(value = "保级有效投注")
        private Integer keepBet;
        @ApiModelProperty(value = "每日奖励")
        private Integer todayAward;
        @ApiModelProperty(value = "升级奖励")
        private Integer upgradeAward;
        @ApiModelProperty(value = "升级奖励是否可领取")
        private Boolean hasRiseFlag;
    }

    @Data
    public class Level9 {
        @ApiModelProperty(value = "升级有效投注")
        private Integer upgradeBet;
        @ApiModelProperty(value = "保级有效投注")
        private Integer keepBet;
        @ApiModelProperty(value = "每日奖励")
        private Integer todayAward;
        @ApiModelProperty(value = "升级奖励")
        private Integer upgradeAward;
        @ApiModelProperty(value = "升级奖励是否可领取")
        private Boolean hasRiseFlag;
    }

    @Data
    public class Level10 {
        @ApiModelProperty(value = "升级有效投注")
        private Integer upgradeBet;
        @ApiModelProperty(value = "保级有效投注")
        private Integer keepBet;
        @ApiModelProperty(value = "每日奖励")
        private Integer todayAward;
        @ApiModelProperty(value = "升级奖励")
        private Integer upgradeAward;
        @ApiModelProperty(value = "升级奖励是否可领取")
        private Boolean hasRiseFlag;
    }

}
