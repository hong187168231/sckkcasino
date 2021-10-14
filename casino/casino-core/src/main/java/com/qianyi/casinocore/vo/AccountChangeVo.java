package com.qianyi.casinocore.vo;

import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.modulecommon.executor.JobSuperVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("账变中心")
public class AccountChangeVo extends JobSuperVo {

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "账变类型")
    private AccountChangeEnum changeEnum;

    @ApiModelProperty(value = "额度变化")
    private BigDecimal amount;

    @ApiModelProperty(value = "额度变化前")
    private BigDecimal amountBefore;

    @ApiModelProperty(value = "额度变化后")
    private BigDecimal amountAfter;

    @ApiModelProperty("总代ID")
    private Long firstProxy;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    @ApiModelProperty("基层代理ID")
    private Long thirdProxy;
}
