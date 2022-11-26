package com.qianyi.casinocore.co.user;

import com.qianyi.casinocore.model.GameRecord;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LevelChangeCo {

    @ApiModelProperty("游戏平台")
    private String platform;
    @ApiModelProperty("游戏记录")
    private GameRecord gameRecord;
    @ApiModelProperty("用户id")
    private Long userId;
    @ApiModelProperty("有效投注流水")
    private BigDecimal betWater;
    @ApiModelProperty("交易类型 1增加流水 2 减少流水")
    private Integer tradeType = 1;

}
