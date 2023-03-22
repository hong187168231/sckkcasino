package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GameRecordReportVo implements Serializable {
    private static final long serialVersionUID = -6875674563240305179L;
    @ApiModelProperty(value = "代理账号")
    private String account;
    private Long accountId;
    @ApiModelProperty("是否有下级代理 ")
    private Boolean hasChildren;

    @ApiModelProperty("父级Id")
    private Long topLevelFirstProxy;

    @ApiModelProperty("投注笔数")
    private Integer bettingNumber;
    @ApiModelProperty(value = "洗码金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal amount;
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "下注金额")
    private BigDecimal betAmount;
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "输赢金额")
    private BigDecimal winLossAmount;
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "有效下注")
    private BigDecimal validAmount;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "输赢金额")
    private BigDecimal totalWinLossAmount;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "用户返利金额")
    private BigDecimal userAmount;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "代理返利金额")
    private BigDecimal surplusAmount;

    @ApiModelProperty(value = "每日奖励")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal todayAward;

    @ApiModelProperty(value = "晋级奖励")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal riseAward;

    @ApiModelProperty(value = "唯一标识")
    private String tag;
}
