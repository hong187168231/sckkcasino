package com.qianyi.liveob.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 */
@ApiModel(value = "ReqLoginGameDTO", description = "登陆游戏参数模型")
@EqualsAndHashCode(callSuper = true)
@Data
public class ReqLoginGameDTO extends BaseReqModel {

    @ApiModelProperty(value = "游戏账号", required = true)
    private String loginName;
    @ApiModelProperty(value = "登录密码", required = true)
    private String loginPassword;
    @ApiModelProperty(value = "设备类型", required = true)
    private Integer deviceType;
    @ApiModelProperty(value = "限红", required = true)
    private Integer oddType;
    @ApiModelProperty(value = "语言", required = true)
    private Integer lang;
    @ApiModelProperty(value = "异常情况时返回商户地址.H5端有用", required = true)
    private String backurl;


}
