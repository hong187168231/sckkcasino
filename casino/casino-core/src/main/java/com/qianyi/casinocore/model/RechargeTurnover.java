package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;

@Entity
@Data
@ApiModel("充值流水记录表")
public class RechargeTurnover extends BaseEntity{

    private Long orderId;
    @ApiModelProperty("客户id")
    private Long userId;
    @ApiModelProperty("订单金额")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal orderMoney;
    @ApiModelProperty("打码量")
    private BigDecimal codeNum;
    @ApiModelProperty("实时打码总量")
    private BigDecimal codeNums;
    @ApiModelProperty("打码倍率")
    private Float codeTimes;
    @ApiModelProperty(value = "汇款方式 1银行卡，4总控操作，5代理上分，6活动赠送")
    private Integer remitType;
}
