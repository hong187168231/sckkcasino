package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("AE游戏摘要记录Vo")
public class GameRecordAeSummaryVo {
    @ApiModelProperty(value = "平台游戏类型")
    private String gameType;

    @ApiModelProperty(value = "返还金额 (包含下注金额)")
    private BigDecimal winAmount;

    @ApiModelProperty(value = "真实下注金额")
    private BigDecimal realBetAmount;

    @ApiModelProperty(value = "真实返还金额")
    private BigDecimal realWinAmount;

    @ApiModelProperty(value = "游戏平台名称")
    private String platform;

    @ApiModelProperty(value = "下注金额")
    private BigDecimal betAmount;

    @ApiModelProperty(value = "游戏名称")
    private String gameName;

    @ApiModelProperty(value = "平台游戏代码")
    private String gameCode;

    @ApiModelProperty(value = " 玩家货币代码")
    private String currency;

    @ApiModelProperty(value = " 累积奖金的下注金额")
    private BigDecimal jackpotBetAmount;

    @ApiModelProperty(value = " 累积奖金的获胜金额")
    private BigDecimal jackpotWinAmount;

    @ApiModelProperty(value = " 游戏平台有效投注")
    private BigDecimal turnover;

    @ApiModelProperty(value = " 注单笔数")
    private Integer betCount;
}
