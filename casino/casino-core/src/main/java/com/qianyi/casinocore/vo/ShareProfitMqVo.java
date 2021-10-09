package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("用户分润MQ实体")
public class ShareProfitMqVo implements Serializable {

    @ApiModelProperty(value = "用户ID")
    private Long userId;
    @ApiModelProperty(value = "有效下注")
    private BigDecimal validbet;
    @ApiModelProperty(value = "游戏记录表ID")
    private Long gameRecordId;
}
