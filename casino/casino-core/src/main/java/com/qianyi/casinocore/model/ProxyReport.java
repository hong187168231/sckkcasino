package com.qianyi.casinocore.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProxyReport extends BaseEntity{

    private Long userId;
    //全部
    private BigDecimal allProfitAmount=BigDecimal.ZERO;
    private BigDecimal allBetAmount=BigDecimal.ZERO;
    private Integer allGroupNum;
    private Integer allBetNum;
    private Integer allChargeNum;

    //直属
    private BigDecimal directProfitAmount=BigDecimal.ZERO;
    private BigDecimal directBetAmount=BigDecimal.ZERO;
    private Integer directGroupNum;
    private Integer directBetNum;
    private Integer directChargeNum;

    //非直属
    private BigDecimal otherProfitAmount=BigDecimal.ZERO;
    private BigDecimal otherBetAmount=BigDecimal.ZERO;
    private Integer otherGroupNum;
    private Integer otherBetNum;
    private Integer otherChargeNum;
}
