package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class UserLevelVo implements Serializable {

    @ApiModelProperty("当前等级")
    private Integer level;

    @ApiModelProperty("晋级等级")
    private Integer nextLevel;

    @ApiModelProperty("当前流水")
    private BigDecimal currentWater = BigDecimal.ZERO;

    @ApiModelProperty("晋级流水")
    private BigDecimal riseWater = BigDecimal.ZERO;

    @ApiModelProperty("晋级需要流水")
    private BigDecimal riseNeedWater = BigDecimal.ZERO;

    @ApiModelProperty("保级需要流水")
    private BigDecimal keepNeedBet = BigDecimal.ZERO;

    @ApiModelProperty("是否展示保级流水")
    private boolean showKeepFlag;

    @ApiModelProperty("每日奖励是否能领取")
    private Boolean todayAwardFlag = false;

    @ApiModelProperty("晋级奖励是否能领取")
    private Boolean riseAwardFlag = false;

    @ApiModelProperty("等级配置详细信息")
    private LevelConfigDto levelConfig;

    @ApiModelProperty("每日奖励")
    private BigDecimal todayAward = BigDecimal.ZERO;

    @ApiModelProperty("晋级奖励")
    private BigDecimal riseAward = BigDecimal.ZERO;
}
