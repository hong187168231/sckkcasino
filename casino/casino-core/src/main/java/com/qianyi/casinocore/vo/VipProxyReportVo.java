package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel
public class VipProxyReportVo {

    @ApiModelProperty(value = "代理账号")
    private String userName;

    @ApiModelProperty(value = "代理账号Id")
    private Long proxyUserId;

    @ApiModelProperty(hidden = true,value = "代理账号Id")
    private Long id;


    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "有效投注")
    private BigDecimal validBet;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "总输赢")
    private BigDecimal winLoss;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "每日奖励")
    private BigDecimal todayAward = BigDecimal.ZERO;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "晋级奖励")
    private BigDecimal riseAward = BigDecimal.ZERO;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "红利")
    private BigDecimal awardAmount;


    @ApiModelProperty(value = "下级人数")
    private Integer proxyUsersNum;

}