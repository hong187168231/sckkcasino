package com.qianyi.casinoadmin.vo;

import com.qianyi.casinoadmin.model.HomePageReport;
import com.qianyi.casinocore.util.CommonConst;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

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

    @ApiModelProperty(value = "有效下注金额")
    private BigDecimal validbetAmount;

    @ApiModelProperty(value = "输赢金额")
    private BigDecimal winLossAmount;

    @ApiModelProperty(value = "洗码金额")
    private BigDecimal washCodeAmount;

    @ApiModelProperty(value = "结算人人代佣金")
    private BigDecimal  shareAmount;

    @ApiModelProperty(value = "预估代理佣金")
    private BigDecimal proxyProfit = BigDecimal.ZERO;

    @ApiModelProperty(value = "发放红利")
    private BigDecimal bonusAmount;

    @ApiModelProperty(value = "充提手续费")
    private BigDecimal serviceCharge = BigDecimal.ZERO;

    @ApiModelProperty(value = "活跃玩家数")
    private Integer activeUsers;

    @ApiModelProperty(value = "新增玩家数")
    private Integer newUsers;

    @ApiModelProperty(value = "统计时间段(日)")
    private String staticsTimes;

    @ApiModelProperty(value = "统计时间段(年)")
    private String staticsYear;

    @ApiModelProperty(value = "统计时间段(月)")
    private String staticsMonth ;

    @ApiModelProperty(value = "毛利1")
    private BigDecimal grossMargin1;

    @ApiModelProperty(value = "毛利2")
    private BigDecimal grossMargin2;

    @ApiModelProperty(value = "预估净利")
    private BigDecimal grossMargin3;

    @ApiModelProperty(value = "充投比")
    private BigDecimal oddsRatio;

    @ApiModelProperty(value = "时间段")
    private String time ;
    public HomePageReportVo(){

    }
    public HomePageReportVo(HomePageReport homePageReport){
        this.chargeAmount = homePageReport.getChargeAmount();
        this.chargeNums = homePageReport.getChargeNums();
        this.withdrawMoney = homePageReport.getWithdrawMoney();
        this.withdrawNums = homePageReport.getWithdrawNums();
        this.validbetAmount = homePageReport.getValidbetAmount();
        this.winLossAmount = homePageReport.getWinLossAmount();
        this.washCodeAmount = homePageReport.getWashCodeAmount();
        this.shareAmount = homePageReport.getShareAmount();
        this.bonusAmount = homePageReport.getBonusAmount();
        this.serviceCharge = homePageReport.getServiceCharge();
        this.staticsTimes = homePageReport.getStaticsTimes();
        this.time = homePageReport.getStaticsTimes();
        this.staticsYear = homePageReport.getStaticsYear();
        this.staticsMonth = homePageReport.getStaticsMonth();
        this.activeUsers = homePageReport.getActiveUsers();
        this.newUsers = homePageReport.getNewUsers();
        if (homePageReport.getValidbetAmount().compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 || homePageReport.getChargeAmount().compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 ){
            this.oddsRatio = homePageReport.getChargeAmount();
        }else {
            this.oddsRatio = homePageReport.getChargeAmount().divide(homePageReport.getValidbetAmount(), 2, RoundingMode.HALF_UP);
        }
    }
}
