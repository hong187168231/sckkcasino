package com.qianyi.casinoadmin.vo;

import com.qianyi.casinocore.model.HomePageReport;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class HomePageReportVo implements Serializable {

    private static final long serialVersionUID = -6875667929814895179L;

    @ApiModelProperty(value = "汇款金额")
    private BigDecimal chargeAmount;

    @ApiModelProperty(value = "汇款笔数")
    private Integer chargeNums;

    @ApiModelProperty(value = "提款金额")
    private BigDecimal withdrawMoney;

    @ApiModelProperty(value = "提款笔数")
    private Integer withdrawNums;

    @ApiModelProperty(value = "下注金额")
    private BigDecimal betAmount;

    @ApiModelProperty(value = "输赢金额")
    private BigDecimal winLossAmount;

    @ApiModelProperty(value = "洗码金额")
    private BigDecimal washCodeAmount;

    @ApiModelProperty(value = "结算人人代佣金")
    private BigDecimal  shareAmount;

    @ApiModelProperty(value = "结算代理佣金")
    private BigDecimal  proxyAmount;

    @ApiModelProperty(value = "发放红利")
    private BigDecimal bonusAmount;

    @ApiModelProperty(value = "充提手续费")
    private BigDecimal serviceCharge = BigDecimal.ZERO;

    @ApiModelProperty(value = "活跃玩家数")
    private Integer activeUsers;

    @ApiModelProperty(value = "新增玩家数")
    private Integer newUsers;

    @ApiModelProperty(value = "统计时间段")
    private String staticsTimes;

    @ApiModelProperty(value = "毛利1")
    private BigDecimal grossMargin1;

    @ApiModelProperty(value = "毛利2")
    private BigDecimal grossMargin2;

    public HomePageReportVo(){

    }
    public HomePageReportVo(HomePageReport homePageReport){
        this.chargeAmount = homePageReport.getChargeAmount();
        this.chargeNums = homePageReport.getChargeNums();
        this.withdrawMoney = homePageReport.getWithdrawMoney();
        this.withdrawNums = homePageReport.getWithdrawNums();
        this.betAmount = homePageReport.getBetAmount();
        this.winLossAmount = homePageReport.getWinLossAmount();
        this.washCodeAmount = homePageReport.getWashCodeAmount();
        this.shareAmount = homePageReport.getShareAmount();
        this.proxyAmount = homePageReport.getProxyAmount();
        this.bonusAmount = homePageReport.getBonusAmount();
        this.serviceCharge = homePageReport.getServiceCharge();
        this.activeUsers = homePageReport.getActiveUsers();
        this.newUsers = homePageReport.getNewUsers();
    }
}
