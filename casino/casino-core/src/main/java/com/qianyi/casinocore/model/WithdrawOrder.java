package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Data
@Entity
@ApiModel("提币订单")
public class WithdrawOrder extends BaseEntity {

    @ApiModelProperty(value = "用户id")
    private Long userId;
    @ApiModelProperty(value = "状态")
    private Integer status;
    @ApiModelProperty(value = "订单号")
    private String no;

    @ApiModelProperty(value = "状态")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal withdrawMoney;

    @ApiModelProperty(value = "银行卡Id")
    private String bankId;

}
