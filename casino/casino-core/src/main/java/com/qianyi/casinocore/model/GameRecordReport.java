package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Entity
@ApiModel("游戏记录报表")
@Table(indexes = {@Index(name="identity_index",columnList = "staticsTimes",unique=true),@Index(columnList = "firstProxy"),
        @Index(columnList = "secondProxy"),@Index(name="identity_index",columnList = "thirdProxy",unique=true)})
public class GameRecordReport extends BaseEntity{

    @ApiModelProperty("投注笔数")
    private Integer bettingNumber;

    @ApiModelProperty(value = "洗码金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal amount;
    /**
     * 下注金额
     */
    @ApiModelProperty(value = "下注金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal betAmount;
    /**
     * 有效下注
     */
    @ApiModelProperty(value = "有效下注")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal validAmount;
    /**
     * 输赢金额
     */
    @ApiModelProperty(value = "输赢金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal winLossAmount;
    /**
     * 统计时间段
     */
    @ApiModelProperty(value = "统计时间段yyyy-MM-dd HH")
    private String staticsTimes;

    @ApiModelProperty("总代ID")
    private Long firstProxy;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    @ApiModelProperty("基层代理ID")
    private Long thirdProxy;
}
