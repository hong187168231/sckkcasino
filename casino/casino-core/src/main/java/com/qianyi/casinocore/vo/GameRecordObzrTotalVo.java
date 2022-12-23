package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GameRecordObzrTotalVo implements Serializable {

    private static final long serialVersionUID = -5876936402231305179L;

    @ApiModelProperty(value = "实际投注总计")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal validBetAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "下注总计")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal betAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "输赢总计")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal netAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "盈利总计")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal payoutAmount = BigDecimal.ZERO;
}