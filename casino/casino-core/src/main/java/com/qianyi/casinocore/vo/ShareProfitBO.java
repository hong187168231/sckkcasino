package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.id.IntegralDataTypeHolder;

import java.math.BigDecimal;

@Data
public class ShareProfitBO {

    @ApiModelProperty(value = "源用户ID")
    private Long fromUserId;
    @ApiModelProperty(value = "用户ID")
    private Long userId;
    @ApiModelProperty(value = "投注金额")
    private BigDecimal betAmount;
    @ApiModelProperty(value = "分润金额")
    private BigDecimal profitAmount;
    @ApiModelProperty(value = "是否第一次下注")
    private boolean isFirst;
    @ApiModelProperty(value = "下注时间")
    private String betTime;
    @ApiModelProperty(value = "是否直属")
    private boolean direct;
    @ApiModelProperty(value = "返佣比例")
    private BigDecimal commission;
    @ApiModelProperty(value = "上级层级")
    private Integer parentLevel;
    @ApiModelProperty(value = "用户账户")
    private String account;


    @ApiModelProperty(value = "游戏账户ID")
    private Long recordId;

    @ApiModelProperty(value = "游戏账户用户ID")
    private Long recordUserId;

    @ApiModelProperty(value = "游戏账户下注ID")
    private String recordBetId;
}
