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

    @ApiModelProperty(value = "汇款方式 1银行卡 2支付宝 3微信")
    private Integer remitType;

    @ApiModelProperty(value = "汇款金额")
    private BigDecimal chargeAmount;

    @ApiModelProperty(value = "实际到账金额")
    private BigDecimal practicalAmount;

    @ApiModelProperty(value = "手续费")
    private BigDecimal serviceCharge;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "充值订单类型 1 会员提交 2 管理员提交")
    private Integer type = 1;

    @ApiModelProperty(value = "收款银行卡ID")
    private Long bankcardId;
}
