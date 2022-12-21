package com.qianyi.liveob.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 */
@ApiModel(value = "RespCreateDepositLoginPlayerDTO", description = "创建玩家、上分、登录三合一接口，参数模型")
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class RespCreateDepositLoginPlayerDTO extends BaseReqModel {

    @ApiModelProperty(value = "创建账号结果", required = true)
    private String create;
    @ApiModelProperty(value = "上分结果", required = true)
    private String deposit;
    @ApiModelProperty(value = "进入游戏结果", required = true)
    private String url;
}
