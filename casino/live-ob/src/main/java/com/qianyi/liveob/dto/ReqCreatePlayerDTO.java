package com.qianyi.liveob.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 */
@ApiModel(value = "ReqCreatePlayerDTO", description = "创建玩家参数模型")
@EqualsAndHashCode(callSuper = true)
@Data
public class ReqCreatePlayerDTO extends BaseReqModel {

    @ApiModelProperty(value = "游戏账号", required = true)
    private String loginName;
    @ApiModelProperty(value = "登录密码", required = true)
    private String loginPassword;
//    @ApiModelProperty(value = "玩家类型：0、试玩 1、正式 2、内部测试 3、机器人", required = true)
//    private Integer testing;
    @ApiModelProperty(value = "盘口ID", required = true)
    private Integer oddType;
    @ApiModelProperty(value = "语言", required = true)
    private Integer lang;

}
