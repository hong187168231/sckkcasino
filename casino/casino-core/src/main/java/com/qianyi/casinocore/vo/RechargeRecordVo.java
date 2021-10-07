package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("充值消息对象")
public class RechargeRecordVo  implements Serializable {
    private static final long serialVersionUID = -6898736529250305179L;
    @ApiModelProperty(value = "用户ID")
    private Long userId;
    @ApiModelProperty("是否首充 0 是 1 不是")
    private Integer isFirst;
    @ApiModelProperty(value = "充值金额")
    private BigDecimal chargeAmount;
    @ApiModelProperty(value = "第一级用户")
    private Long firstUserId;
    @ApiModelProperty(value = "第二级用户ID")
    private Long secondUserId;
    @ApiModelProperty(value = "第三级用户ID")
    private Long thirdUserId;
}
