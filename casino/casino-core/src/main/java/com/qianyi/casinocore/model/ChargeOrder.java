package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Transient;
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
    @ApiModelProperty(value = "订单状态0.未确认,1.成功,2.失败,3.失效 4.总控提交 5.代理提交")
    private Integer status;

    @ApiModelProperty(value = "汇款人")
    private String remitter;

    @ApiModelProperty(value = "汇款方式 1银行卡 2支付宝 3微信")
    private Integer remitType;

    @ApiModelProperty(value = "汇款金额")
    private BigDecimal chargeAmount;

//    @ApiModelProperty(value = "实际充值金额")
//    private BigDecimal realityAmount;

    @ApiModelProperty(value = "实际到账金额")
    private BigDecimal practicalAmount;

    @ApiModelProperty(value = "手续费")
    private BigDecimal serviceCharge;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "收款银行卡ID")
    private Long bankcardId;

    @ApiModelProperty("总代ID")
    private Long firstProxy;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    @ApiModelProperty("基层代理ID")
    private Long thirdProxy;

    @ApiModelProperty("审核人")
    private String lastModifier;
}
