package com.qianyi.casinocore.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProxyDayReport extends BaseEntity{

    private Long userId;

    private BigDecimal betAmount = BigDecimal.ZERO;
    private BigDecimal profitAmount= BigDecimal.ZERO;
    private Integer newNum;
    private Integer groupNum;
    private Integer directNum;
    private String dayTime;

}
