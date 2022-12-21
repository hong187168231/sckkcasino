package com.qianyi.liveob.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 *
 */
@ApiModel(value = "ReqTransferDTO", description = "转账交易参数模型")
@EqualsAndHashCode(callSuper = true)
@Data
public class ReqTransferDTO extends BaseReqModel {

    @ApiModelProperty(value = "游戏账号", required = true)
    private String loginName;
    @ApiModelProperty(value = "交易单号", required = true)
    private String transferNo;
    @ApiModelProperty(value = "交易金额", required = true)
    private BigDecimal amount;


}
