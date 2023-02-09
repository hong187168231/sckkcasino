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
@Table(indexes = {@Index(columnList = "firstProxy"),
    @Index(columnList = "secondProxy"),@Index(name="identity_index",columnList = "gameRecordReportId",unique=true)})
public class GameRecordReportNew extends BaseEntity{

    @ApiModelProperty("唯一标识")
    private Long gameRecordReportId;

    @ApiModelProperty("投注笔数")
    private Integer bettingNumber;

    @ApiModelProperty(value = "用户返利金额(弃用)")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal userAmount;

    @ApiModelProperty(value = "代理返利金额(弃用)")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal surplusAmount;

    @ApiModelProperty(value = "平台:wm,PG,CQ9")
    private String platform;

    @ApiModelProperty(value = "洗码金额(弃用)")
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

    @ApiModelProperty(value = "洗码金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal newAmount;

    @ApiModelProperty(value = "用户返利金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal newUserAmount;

    @ApiModelProperty(value = "代理返利金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal newSurplusAmount;

    @ApiModelProperty(value = "每日奖励")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal todayAward;

    @ApiModelProperty(value = "晋级奖励")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal riseAward;

    public GameRecordReportNew(){

    }
    public GameRecordReportNew(Long firstProxy,Long bettingNumber,BigDecimal amount,BigDecimal betAmount,BigDecimal validAmount,
        BigDecimal winLossAmount,BigDecimal userAmount,BigDecimal surplusAmount){
        this.firstProxy = firstProxy;
        this.bettingNumber = bettingNumber==null?0:bettingNumber .intValue();
        this.amount = amount;
        this.betAmount = betAmount;
        this.validAmount = validAmount;
        this.winLossAmount = winLossAmount;
        this.userAmount = userAmount;
        this.surplusAmount = surplusAmount;
    }
    public GameRecordReportNew(Long firstProxy,Long bettingNumber,BigDecimal amount,BigDecimal betAmount,BigDecimal validAmount,
        BigDecimal winLossAmount,BigDecimal userAmount,BigDecimal surplusAmount,BigDecimal newAmount,BigDecimal newUserAmount,BigDecimal newSurplusAmount
        ,BigDecimal todayAward,BigDecimal riseAward){
        this.firstProxy = firstProxy;
        this.bettingNumber = bettingNumber==null?0:bettingNumber .intValue();
        this.amount = amount;
        this.betAmount = betAmount;
        this.validAmount = validAmount;
        this.winLossAmount = winLossAmount;
        this.userAmount = userAmount;
        this.surplusAmount = surplusAmount;
        this.newAmount = newAmount;
        this.newUserAmount = newUserAmount;
        this.newSurplusAmount = newSurplusAmount;
        this.todayAward = todayAward;
        this.riseAward = riseAward;
    }
    public GameRecordReportNew(Long bettingNumber,BigDecimal amount,BigDecimal betAmount,BigDecimal validAmount,BigDecimal winLossAmount,BigDecimal userAmount,BigDecimal surplusAmount){
        this.bettingNumber = bettingNumber==null?0:bettingNumber .intValue();
        this.amount = amount;
        this.betAmount = betAmount;
        this.validAmount = validAmount;
        this.winLossAmount = winLossAmount;
        this.userAmount = userAmount;
        this.surplusAmount = surplusAmount;
    }
    public GameRecordReportNew(Long bettingNumber,BigDecimal amount,BigDecimal betAmount,BigDecimal validAmount,BigDecimal winLossAmount,
        BigDecimal userAmount,BigDecimal surplusAmount,BigDecimal newAmount,BigDecimal newUserAmount,BigDecimal newSurplusAmount,
        BigDecimal todayAward,BigDecimal riseAward){
        this.bettingNumber = bettingNumber==null?0:bettingNumber .intValue();
        this.amount = amount;
        this.betAmount = betAmount;
        this.validAmount = validAmount;
        this.winLossAmount = winLossAmount;
        this.userAmount = userAmount;
        this.surplusAmount = surplusAmount;
        this.newAmount = newAmount;
        this.newUserAmount = newUserAmount;
        this.newSurplusAmount = newSurplusAmount;
        this.todayAward = todayAward;
        this.riseAward = riseAward;
    }
    public GameRecordReportNew(BigDecimal newAmount){
        this.newAmount = newAmount==null?BigDecimal.ZERO:newAmount;
    }
}
