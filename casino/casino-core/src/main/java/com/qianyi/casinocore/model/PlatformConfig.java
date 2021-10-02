package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@ApiModel("平台配置表")
public class PlatformConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ApiModelProperty("最低金额清除打码量")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal clearCodeNum;


    @ApiModelProperty("打码倍率")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal betRate;

    @ApiModelProperty("每笔最低充值")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal chargeMinMoney;

    @ApiModelProperty("每笔最高充值")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal ChargeMaxMoney;

    @ApiModelProperty("充值服务费")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal chargeServiceMoney ;


    @ApiModelProperty("ip最大注册量")
    private Integer ipMaxNum;

    @ApiModelProperty("WM余额")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal wmMoney ;
}
