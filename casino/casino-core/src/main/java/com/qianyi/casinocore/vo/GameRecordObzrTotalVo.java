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

    @ApiModelProperty(value = "订单实际投注金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal validBetAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "结算金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal netAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "盈利金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal payoutAmount = BigDecimal.ZERO;
}