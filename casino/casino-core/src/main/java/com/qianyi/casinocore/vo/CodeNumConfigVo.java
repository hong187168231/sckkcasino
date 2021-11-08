package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CodeNumConfigVo implements Serializable {

    private static final long serialVersionUID = -6875617946853305179L;

    @ApiModelProperty("打码倍率")
    private BigDecimal betRate = BigDecimal.ZERO;

    @ApiModelProperty("每笔最低充值")
    private BigDecimal chargeMinMoney = BigDecimal.ZERO;

    @ApiModelProperty("每笔最高充值")
    private BigDecimal chargeMaxMoney = BigDecimal.ZERO;

    @ApiModelProperty("每笔最低提现额")
    private BigDecimal withdrawMinMoney = BigDecimal.ZERO;

    @ApiModelProperty("每笔最高提现额")
    private BigDecimal withdrawMaxMoney = BigDecimal.ZERO;
}
