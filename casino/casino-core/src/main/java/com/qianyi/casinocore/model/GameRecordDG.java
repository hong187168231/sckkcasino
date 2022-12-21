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
@Table(name ="game_record_dg",uniqueConstraints={@UniqueConstraint(columnNames={"betOrderNo"})})
@ApiModel("DG游戏记录")
public class GameRecordDG extends BaseEntity{
    @ApiModelProperty(value = "注单唯一Id")
    private String betOrderNo;
    @ApiModelProperty(value = "游戏大厅号 1:旗舰厅；2:亚洲厅；3，4:现场厅；5:欧美厅,7:国际厅,8:区块链厅")
    private Integer lobbyId;//	Integer   可以为空
    @ApiModelProperty(value = "游戏桌号")
    private Integer tableId;//	Integer		可以为空
    @ApiModelProperty(value = "游戏靴号")
    private Long shoeId;//	Long		可以为空
    @ApiModelProperty(value = "游戏局号")
    private Long playId;//	Long		可以为空
    @ApiModelProperty(value = "游戏类型")
    private Integer GameType;//	Integer
    @ApiModelProperty(value = "游戏Id")
    private Integer GameId;//	Integer
    @ApiModelProperty(value = "会员Id")
    private Long memberId;//	Long
    @ApiModelProperty(value = "游戏下注时间")
    private String betTime;//	Date
    @ApiModelProperty(value = "游戏结算时间")
    private String calTime;//	Date		可以为空

    @ApiModelProperty(value = "派彩金额")
    private BigDecimal winOrLoss;//	Double	 (输赢应扣除下注金额)	可以为空
    @ApiModelProperty(value = "好路追注派彩金额")
    private BigDecimal winOrLossz;//	Double		winOrLoss为总派彩金额
    @ApiModelProperty(value = "下注金额")
    private BigDecimal betPoints;//	Double
    @ApiModelProperty(value = "好路追注金额")
    private BigDecimal betPointsz;//	Double		betPoints为总金额

    @ApiModelProperty("实付金额")
    private BigDecimal realMoney;//派彩金额减去有效下注金额
    @ApiModelProperty("中奖金额")
    private BigDecimal winMoney;//派彩金额减去下注金额
    @ApiModelProperty(value = "有效下注金额")
    private BigDecimal availableBet;//	Double		可以为空
    @ApiModelProperty(value = "会员登入账号")
    private String userName;//	String
    @ApiModelProperty(value = "游戏结果")
    private String result;//	String		可以为空
    @ApiModelProperty(value = "下注注单")
    private String betDetail;//	String		可以为空
    @ApiModelProperty(value = "好路追注注单")
    private String betDetailz;//	String		可以为空,betDetail为总单
    @ApiModelProperty(value = "下注时客户端IP")
    private String ip;//	String
    @ApiModelProperty(value = "游戏唯一ID")
    private String ext;//	String
    @ApiModelProperty(value = "是否结算：1：已结算 2:撤销")
    private Integer isRevocation;//	Integer
    @ApiModelProperty(value = "余额")
    private BigDecimal balanceBefore;//	Double
    @ApiModelProperty(value = "撤销的那比注单的ID 对冲注单才有,可以为空")
    private Long parentBetId;//	Long
    @ApiModelProperty(value = "货币ID")
    private Integer currencyId;//	Integer
    @ApiModelProperty(value = "下注时客户端类型")
    private Integer deviceType;//	Integer
    @ApiModelProperty(value = "追注转账流水号 共享钱包API可用于对账,普通转账API可忽略")
    private Long pluginid;//	Long

    @ApiModelProperty(value = "我方用户ID")
    private Long userId;

    @ApiModelProperty(value = "我方用户账号")
    private String userAcct;

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

    @ApiModelProperty("总代ID")
    private Long firstProxy;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    @ApiModelProperty("基层代理ID")
    private Long thirdProxy;

    @ApiModelProperty(value = "是否新增，0.修改,1.新增")
    private Integer isAdd = 0;

    @Transient
    @ApiModelProperty(value = "旧真实返还金额")
    private BigDecimal oldRealWinAmount;

    @Transient
    @ApiModelProperty(value = "旧有效下注")
    private BigDecimal oldTurnover;

    public GameRecordDG(){
    }
    public GameRecordDG(BigDecimal betPoints,BigDecimal winOrLoss,BigDecimal realMoney,BigDecimal winMoney,BigDecimal availableBet){
        this.betPoints = betPoints==null?BigDecimal.ZERO:betPoints;
        this.winOrLoss = winOrLoss==null?BigDecimal.ZERO:winOrLoss;
        this.realMoney = realMoney==null?BigDecimal.ZERO:realMoney;
        this.winMoney = winMoney==null?BigDecimal.ZERO:winMoney;
        this.availableBet = availableBet==null?BigDecimal.ZERO:availableBet;
    }
}
