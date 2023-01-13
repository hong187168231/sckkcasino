package com.qianyi.casinocore.vo;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel
public class VipReportVo {

    @ApiModelProperty(value = "代理账号")
    private String userName;

    @ApiModelProperty(value = "代理账号Id")
    private Long proxyUserId;

    @ApiModelProperty(value = "等级")
    private String level;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "有效投注")
    private BigDecimal validBet;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "总输赢")
    private BigDecimal winLoss;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "每日奖励")
    private BigDecimal todayAward;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "晋级奖励")
    private BigDecimal riseAward;
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "下级人数")
    private Integer proxyUsersNum;

}