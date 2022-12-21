package com.qianyi.liveob.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 */
@ApiModel(value = "ReqGetTransferDTO", description = "获取交易状态")
@EqualsAndHashCode(callSuper = true)
@Data
public class ReqGetTransferDTO extends BaseReqModel {

    @ApiModelProperty(value = "交易单号", required = true)
    private String transferNo;

    @ApiModelProperty(value = "游戏账号", required = true)
    private String loginName;

}
