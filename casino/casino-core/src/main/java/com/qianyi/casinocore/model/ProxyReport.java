package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import java.math.BigDecimal;

@Data
@Entity
public class ProxyReport extends BaseEntity{

    private Long userId;
    private String account;
    //全部
    @ApiModelProperty("分润")
    private BigDecimal allProfitAmount=BigDecimal.ZERO;
    @ApiModelProperty("有效投注")
    private BigDecimal allBetAmount=BigDecimal.ZERO;
    @ApiModelProperty("团队人数")
    private Integer allGroupNum;
    @ApiModelProperty("投注人数")
    private Integer allBetNum;
    @ApiModelProperty("充值人数")
    private Integer allChargeNum;

    //直属
    @ApiModelProperty("分润")
    private BigDecimal directProfitAmount=BigDecimal.ZERO;
    @ApiModelProperty("有效投注")
    private BigDecimal directBetAmount=BigDecimal.ZERO;
    @ApiModelProperty("团队人数")
    private Integer directGroupNum;
    @ApiModelProperty("投注人数")
    private Integer directBetNum;
    @ApiModelProperty("充值人数")
    private Integer directChargeNum;

    //非直属
    @ApiModelProperty("分润")
    private BigDecimal otherProfitAmount=BigDecimal.ZERO;
    @ApiModelProperty("有效投注")
    private BigDecimal otherBetAmount=BigDecimal.ZERO;
    @ApiModelProperty("团队人数")
    private Integer otherGroupNum;
    @ApiModelProperty("投注人数")
    private Integer otherBetNum;
    @ApiModelProperty("充值人数")
    private Integer otherChargeNum;
}
