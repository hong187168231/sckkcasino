package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GameRecordTotalVo implements Serializable {
    private static final long serialVersionUID = -6975317983240305179L;

    @ApiModelProperty(value = "下注金额")
    private BigDecimal bet;
    @ApiModelProperty(value = "有效下注")
    private BigDecimal validbet;

    @ApiModelProperty(value = "输赢金额")
    private BigDecimal winLoss;
}
