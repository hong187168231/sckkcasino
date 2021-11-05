package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Data
@Entity
public class CompanyProxyMonth extends BaseEntity{

    @ApiModelProperty(value = "用户id")
    private Long userId;
    @ApiModelProperty("代理角色 1：总代理 2：区域代理 3：基层代理")
    private Integer proxyRole;
    @ApiModelProperty(value = "总代理")
    private Long firstProxy;
    @ApiModelProperty(value = "区域代理")
    private Long secondProxy;
    @ApiModelProperty(value = "基层代理")
    private Long thirdProxy;
    @ApiModelProperty(value = "统计时段")
    private String staticsTimes;
    @ApiModelProperty(value = "创造业绩的玩家数")
    private Integer playerNum;
    @ApiModelProperty(value = "团队业绩流水")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal groupBetAmount;
    @ApiModelProperty(value = "返佣级别")
    private String profitLevel;
    @ApiModelProperty(value = "返佣比例")
    private String profitRate;
    @ApiModelProperty(value = "团队总返佣")
    private BigDecimal groupTotalprofit;
    @ApiModelProperty(value = "佣金分成比")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal benefitRate;
    @ApiModelProperty(value = "个人结算佣金")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal profitAmount;
    @ApiModelProperty(value = "结清状态 0 未结清 1已结清")
    private Integer settleStatus;
}
