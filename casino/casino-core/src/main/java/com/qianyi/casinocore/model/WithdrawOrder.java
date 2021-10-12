package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.math.BigDecimal;

@Data
@Entity
@ApiModel("提币订单")
public class WithdrawOrder extends BaseEntity {

    @ApiModelProperty(value = "用户id")
    private Long userId;
    @ApiModelProperty(value = "状态 0: 未确认 1：通过，2：拒绝，3：冻结  4.总控上分 5.代理上分")
    private Integer status;
    @ApiModelProperty(value = "订单号")
    private String no;

    @ApiModelProperty(value = "金额")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal withdrawMoney;

    @ApiModelProperty(value = "实际提现金额")
    private BigDecimal practicalAmount;

    @ApiModelProperty(value = "手续费")
    private BigDecimal serviceCharge;

    @ApiModelProperty(value = "银行卡Id")
    private String bankId;

    @ApiModelProperty(value = "收款方式 1银行卡 2支付宝 3微信")
    private Integer remitType;

    @ApiModelProperty("总代ID")
    private Long firstProxy;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    @ApiModelProperty("基层代理ID")
    private Long thirdProxy;

}
