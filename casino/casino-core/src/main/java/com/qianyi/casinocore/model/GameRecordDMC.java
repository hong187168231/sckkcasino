package com.qianyi.casinocore.model;

import com.qianyi.modulecommon.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Data
@Entity
@Table(name ="game_record_dmc",uniqueConstraints={@UniqueConstraint(columnNames={"betOrderNo"})})
@ApiModel("大马彩彩游戏子单记录")
public class GameRecordDMC extends BaseEntity{

    @ApiModelProperty(value = "我方账号ID")
    private Long userId;
    @ApiModelProperty(value = "我方账号")
    private String account;
    @ApiModelProperty(value = "玩家Id")
    private String customerId;
    @ApiModelProperty(value = "玩家账号")
    private String userName;
    @ApiModelProperty("商户Id")
    private String enterpriseId;
    @ApiModelProperty("商名称")
    private String companyName;
    @ApiModelProperty("货币")
    private String currency;
    @ApiModelProperty("注单号")
    private String betOrderNo;
    @ApiModelProperty("父注单号")
    private String parentBetOrderNo;
    @ApiModelProperty("下注时间")
    private String betTime;
    @ApiModelProperty("结算时间")
    private String settleTime;
    @ApiModelProperty("下注类型")
    private String betType;
    @ApiModelProperty("下注大小")
    private String betSize;
    private String drawDate;  //开彩日期

    @ApiModelProperty("实付金额")
    private BigDecimal realMoney;
    @ApiModelProperty("投注金额")
    private BigDecimal betMoney;

    @ApiModelProperty("中奖金额")
    private BigDecimal winMoney;

    @ApiModelProperty("返水金额")
    private BigDecimal backWaterMoney;
    @ApiModelProperty("下注号码")
    private String betCode;
    @ApiModelProperty("大 - 下注金额")
    private BigDecimal bigBetAmount;
    @ApiModelProperty("小 - 下注金额")
    private BigDecimal smallBetAmount;
    @ApiModelProperty("期号")
    private String issue;
    @ApiModelProperty("游戏名称 (Magnum/Damachai/Toto)")
    private String gameName;
    @ApiModelProperty("开彩日期")
    private String gameDate;

    @ApiModelProperty("根据下注号码和投注玩法所衍生的下注号码")
    private String slaveLotteryNumber;
    @ApiModelProperty("子单号的下注金额")
    private BigDecimal slaveAmount;
    @ApiModelProperty("子单状态")
    private String slaveStatus;
    @ApiModelProperty("子单净金额")
    private String slaveNetAmount;

    @ApiModelProperty(" 中奖类别 (P1:首奖, P2: 次奖, P3: 三奖, Special: 特别奖, Consolation: 安慰奖)")
    private String prizeType;


    @ApiModelProperty(value = "洗码状态：1：成功")
    private Integer washCodeStatus;

    @ApiModelProperty(value = "等级流水状态：1：成功")
    private Integer levelWaterStatus;

    @ApiModelProperty(value = "返利状态：1：成功")
    private Integer rebateStatus;

    @ApiModelProperty(value = "抽点状态: 0: 否 1: 是")
    private Integer extractStatus;

    @ApiModelProperty(value = "打码状态：1：成功")
    private Integer codeNumStatus;

    @ApiModelProperty(value = "分润状态：0:失败，>0：成功")
    private Integer shareProfitStatus = Constants.no;

    @ApiModelProperty(value = "游戏报表状态：0:失败，1：成功")
    private Integer gameRecordStatus = Constants.no;

    @ApiModelProperty(value = "子单下注号码")
    private String lotteryNumber ; //期号

    @ApiModelProperty(value = "期号")
    private String drawNumber ; //期号

    @ApiModelProperty(value = "货币Code")
    private String currencyCode ; //货币Code


    @ApiModelProperty("总代ID")
    private Long firstProxy;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    @ApiModelProperty("基层代理ID")
    private Long thirdProxy;

    @Transient
    @ApiModelProperty(value = "是否新增，0.修改,1.新增")
    private Integer isAdd = 0;

    @Transient
    @ApiModelProperty(value = "旧真实返还金额")
    private BigDecimal oldRealWinAmount;

    @Transient
    @ApiModelProperty(value = "旧有效下注")
    private BigDecimal oldTurnover;


    public GameRecordDMC(){

    }

    public GameRecordDMC(BigDecimal betMoney,BigDecimal backWaterMoney,BigDecimal realMoney,BigDecimal winMoney){
        this.betMoney = betMoney==null?BigDecimal.ZERO:betMoney;
        this.backWaterMoney = backWaterMoney==null?BigDecimal.ZERO:backWaterMoney;
        this.realMoney = realMoney==null?BigDecimal.ZERO:realMoney;
        this.winMoney = winMoney==null?BigDecimal.ZERO:winMoney;
    }

}
