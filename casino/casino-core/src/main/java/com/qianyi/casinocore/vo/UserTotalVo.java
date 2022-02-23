package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class UserTotalVo implements Serializable {
    private static final long serialVersionUID = -6875617998456387632L;
    @ApiModelProperty("待领取洗码金额")
    private BigDecimal washCode = BigDecimal.ZERO;
    @ApiModelProperty("中心余额")
    private BigDecimal money = BigDecimal.ZERO;

}
