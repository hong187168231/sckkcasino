package com.qianyi.liveob.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 */
@ApiModel(value = "ReqEnableUserBettingDTO", description = "禁用和启用会员投注状态")
@EqualsAndHashCode(callSuper = true)
@Data
public class ReqEnableUserBettingDTO extends BaseReqModel {

    @ApiModelProperty(value = "游戏账号", required = true)
    private String loginName;
    @ApiModelProperty(value = "开启/禁用 0=开启，1=禁用", required = true)
    private Integer enabled;


}
