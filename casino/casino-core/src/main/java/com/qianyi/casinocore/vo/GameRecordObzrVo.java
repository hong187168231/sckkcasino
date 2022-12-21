package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GameRecordObzrVo implements Serializable {

    private static final long serialVersionUID = -648529622312379L;
    @ApiModelProperty(value = "会员Id")
    private Long userId;
    @ApiModelProperty(value = "用户名")
    private String account;
    @ApiModelProperty(value = "第三方账户")
    private String userName;
    @ApiModelProperty(value = "注单号")
    private String orderNo;
    @ApiModelProperty(value = "投注金属额")
    private BigDecimal betAmount;
    @ApiModelProperty(value = "游戏下注")
    private BigDecimal validBetAmount;
    @ApiModelProperty(value = "输赢金额")
    private BigDecimal netAmount;
    @ApiModelProperty(value = "派彩金额")
    private BigDecimal payoutAmount;
    @ApiModelProperty(value = "下注时间")
    private String betStrTime;
    @ApiModelProperty(value = "派彩时间")
    private String netAt;



}