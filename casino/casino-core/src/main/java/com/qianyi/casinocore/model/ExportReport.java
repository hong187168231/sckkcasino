package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@ApiModel("会员总报表导出数据")
@Table(indexes = {@Index(columnList = "userId"),
    @Index(columnList = "orderTimes")},uniqueConstraints={@UniqueConstraint(columnNames={"userId","orderTimes"})})
public class ExportReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(value = "投注笔数")
    private Integer num;

    @ApiModelProperty(value = "会员id")
    private Long userId;

    @ApiModelProperty(value = "账号")
    private String account;

    @ApiModelProperty(value = "基础id")
    private String thirdProxy;

    @ApiModelProperty(value = "基代账号")
    private String thirdProxyName;

    @ApiModelProperty(value = "统计时间段yyyy-MM-dd(以美东时间为维度统计一天)")
    @Temporal(value = TemporalType.DATE)
    private Date orderTimes;

    @ApiModelProperty(value = "投注金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal betAmount;

    @ApiModelProperty(value = "有效投注")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal validbet;

    @ApiModelProperty(value = "玩家输赢")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal winLoss;

    @ApiModelProperty(value = "洗码金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal washAmount;

    @ApiModelProperty(value = "人人代返佣")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal allProfitAmount;

    @ApiModelProperty(value = "手续费")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal serviceCharge;

    @ApiModelProperty(value = "贡献代理抽点, 表示该用户，对上级代理贡献的抽点金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal allWater;

    @ApiModelProperty(value = "每日奖励")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal todayAward = BigDecimal.ZERO;

    @ApiModelProperty(value = "晋级奖励")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal riseAward = BigDecimal.ZERO;

    @ApiModelProperty(value = "毛利1")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal avgBenefit;

    @ApiModelProperty(value = "总结算(毛利2)")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal totalAmount;

    public BigDecimal getSum(){
        return this.betAmount.add(this.validbet).add(this.winLoss).add(this.washAmount).add(this.allProfitAmount).
            add(this.serviceCharge).add(this.allWater).add(this.todayAward).add(this.riseAward).add(this.avgBenefit).add(this.totalAmount);
    }
}
