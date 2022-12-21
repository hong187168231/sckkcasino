package com.qianyi.liveob.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "ReqResetUserPwdDTO", description = "重置密码")
@Data
public class ReqResetUserPwdDTO extends BaseReqModel {

    @ApiModelProperty(value = "游戏账号", required = true)
    private String loginName;
    @ApiModelProperty(value = "新的密码", required = true)
    private String newPassword;


}
