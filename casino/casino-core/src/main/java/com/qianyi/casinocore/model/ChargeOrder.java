package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import java.math.BigDecimal;

@Data
@Entity
@ApiModel("充值订单")
public class ChargeOrder extends BaseEntity{

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    //0.未确认。 1.成功   2.失败, 3.失效
    @ApiModelProperty(value = "订单状态0.未确认,1.成功,2.失败,3.失效")
    private Integer status;

    @ApiModelProperty(value = "汇款人")
    private String remitter;

    @ApiModelProperty(value = "汇款方式")
    private Integer remitType;

    @ApiModelProperty(value = "汇款金额")
    private BigDecimal chargeAmount;

    @ApiModelProperty(value = "备注")
    private String remark;
}
