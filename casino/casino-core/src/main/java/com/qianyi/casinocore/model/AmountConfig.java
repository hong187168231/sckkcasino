package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity
@Data
@ApiModel("充提明细配置表")
public class AmountConfig extends BaseEntity {
    /**
     * 状态 0免手续费 1 有手续费
     */
    @ApiModelProperty(value = "状态 0免手续费 1 有手续费")
    private Integer status;

    /**
     * 固定金额
     */
    @ApiModelProperty(value = "固定金额")
    private BigDecimal fixedAmount;

    /**
     * 百分比金额
     */
    @ApiModelProperty(value = "百分比金额")
    private Float percentage;

    /**
     * 最大金额
     */
    @ApiModelProperty(value = "最大金额")
    private BigDecimal maxMoney;

    /**
     * 最小金额
     */
    @ApiModelProperty(value = "最小金额")
    private BigDecimal minMoney;

    public AmountConfig(){

    }
    public AmountConfig(Integer status, BigDecimal fixedAmount, Float percentage, BigDecimal maxMoney, BigDecimal minMoney){
        this.status = status==null?0:status;
        this.fixedAmount = fixedAmount==null?BigDecimal.ZERO:fixedAmount;
        this.percentage = percentage==null?0F:percentage;
        this.maxMoney = maxMoney;
        this.minMoney = minMoney;
    }
    //得到手续费用
    private BigDecimal getServiceCharge(BigDecimal money){
        if (this.status == 0){
            return BigDecimal.ZERO;
        }
        return money.multiply(new BigDecimal(this.percentage)).add(this.fixedAmount);
    }
}
