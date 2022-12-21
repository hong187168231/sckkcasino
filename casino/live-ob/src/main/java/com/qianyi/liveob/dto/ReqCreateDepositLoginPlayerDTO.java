package com.qianyi.liveob.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 *
 */
@ApiModel(value = "ReqCreateDepositLoginPlayerDTO", description = "创建玩家、上分、登录三合一接口，参数模型")
@EqualsAndHashCode(callSuper = true)
@Data
public class ReqCreateDepositLoginPlayerDTO extends BaseReqModel {

    /**
     * 创建账号和登录游戏所需
     *
     * 如果创建账号失败，则返回不走上分、进入游戏逻辑
     */
    @ApiModelProperty(value = "游戏账号", required = true)
    private String loginName;
    @ApiModelProperty(value = "登录密码", required = true)
    private String loginPassword;
    @ApiModelProperty(value = "盘口ID", required = true)
    private Integer oddType;
    @ApiModelProperty(value = "语言", required = true)
    private Integer lang;
    @ApiModelProperty(value = "设备类型", required = true)
    private Integer deviceType;
    @ApiModelProperty(value = "异常情况时返回商户地址.H5端有用", required = true)
    private String backurl;

    /**
     *可选择进入游戏时带入金额。如果不带分，则下面两个参数可不填写
     *
     * 如果有上分，失败时，接口返回提示。
     * 如果有上分，成功时，返回交易单号，继续进入游戏
     */
    @ApiModelProperty(value = "交易单号", required = true)
    private String transferNo;
    @ApiModelProperty(value = "上分金额。小于等于0表示不上分。大于0上分时必须要包含单号", required = true)
    private BigDecimal amount;
}
