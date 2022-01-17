package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GameRecordReportVo implements Serializable {
    private static final long serialVersionUID = -6875674563240305179L;
    @ApiModelProperty(value = "代理账号")
    private String account;
    @ApiModelProperty("投注笔数")
    private Integer bettingNumber;
    @ApiModelProperty(value = "洗码金额")
    private BigDecimal amount;
    @ApiModelProperty(value = "下注金额")
    private BigDecimal betAmount;
    @ApiModelProperty(value = "输赢金额")
    private BigDecimal winLossAmount;
    @ApiModelProperty(value = "有效下注")
    private BigDecimal validAmount;
}
