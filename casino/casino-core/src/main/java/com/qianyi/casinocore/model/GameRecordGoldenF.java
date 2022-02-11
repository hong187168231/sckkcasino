package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity
@Data
@ApiModel("资金详情")
public class GameRecordGoldenF extends BaseEntity {

    @ApiModelProperty(value = "用户姓名")
    private String playerName;

    @ApiModelProperty(value = "父主单号")
    private String parentBetId;

    @ApiModelProperty(value = "下注编号")
    private String betId;

    @ApiModelProperty(value = "交易类型")
    private String transType;

    @ApiModelProperty(value = "游戏代码")
    private String gameCode;

    @ApiModelProperty(value = "币别")
    private String currency;

    @ApiModelProperty(value = "下注金额")
    private BigDecimal betAmount;

    @ApiModelProperty(value = "派彩或退回金额")
    private BigDecimal winAmount;

    @ApiModelProperty(value = "产品代码")
    private String vendorCode;

    @ApiModelProperty(value = "钱包代码")
    private String walletCode;

    @ApiModelProperty(value = "创建时间")
    private Long createAt;

    @ApiModelProperty(value = "创建时间字符串")
    private String createAtStr;

    @ApiModelProperty(value = "交易编号")
    private String traceId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "一级代理")
    private Long firstProxy;

    @ApiModelProperty(value = "二级代理")
    private Long secondProxy;

    @ApiModelProperty(value = "三级代理")
    private Long thirdProxy;
}
