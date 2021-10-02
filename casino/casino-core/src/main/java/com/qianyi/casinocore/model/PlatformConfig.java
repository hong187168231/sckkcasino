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
    private BigDecimal chargeMaxMoney;

    @ApiModelProperty("充值服务费")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal chargeServiceMoney;

    @ApiModelProperty("充值手续费百分比")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal chargeRate;

    @ApiModelProperty("每笔最低提现额")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal withdrawMinMoney;

    @ApiModelProperty("每笔最高提现额")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal withdrawMaxMoney;

    @ApiModelProperty("提现服务费")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal withdrawServiceMoney;

    @ApiModelProperty("提现手续费百分比")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal withdrawRate;

    @ApiModelProperty("ip最大注册量")
    private Integer ipMaxNum;

    @ApiModelProperty("WM余额")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal wmMoney;

    @ApiModelProperty("WM余额警戒线")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal wmMoneyWarning;

    //得到充值手续费用
    public BigDecimal getChargeServiceCharge(BigDecimal money){
        if (this.chargeRate == null){
            this.chargeRate = BigDecimal.ZERO;
        }
        if (this.chargeServiceMoney == null){
            this.chargeServiceMoney = BigDecimal.ZERO;
        }
        return money.multiply(this.chargeRate).add(this.chargeServiceMoney);
    }
    //得到提现手续费用
    public BigDecimal getWithdrawServiceCharge(BigDecimal money){
        if (this.withdrawRate == null){
            this.withdrawRate = BigDecimal.ZERO;
        }
        if (this.withdrawServiceMoney == null){
            this.withdrawServiceMoney = BigDecimal.ZERO;
        }
        return money.multiply(this.withdrawRate).add(this.withdrawServiceMoney);
    }
}
