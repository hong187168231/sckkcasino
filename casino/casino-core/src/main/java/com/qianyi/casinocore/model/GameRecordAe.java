package com.qianyi.casinocore.model;

import com.qianyi.modulecommon.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * https://allwecan.info/api-doc/multi-wallet/PRD#operation/GetTransactionByUpdateDate
 */
@Data
@Entity
@Table(name ="game_record_ae")
@ApiModel("AE游戏记录")
public class GameRecordAe extends BaseEntity{

    @ApiModelProperty(value = "我方账号")
    private Long userId;

    @ApiModelProperty(value = "平台游戏类型")
    private String gameType;

    @ApiModelProperty(value = "返还金额 (包含下注金额)")
    private BigDecimal winAmount;

    @ApiModelProperty(value = "用于区分注单结果是否有更改")
    private Integer settleStatus;

    @ApiModelProperty(value = "真实下注金额")
    private BigDecimal realBetAmount;

    @ApiModelProperty(value = "真实返还金额")
    private BigDecimal realWinAmount;

    @ApiModelProperty(value = "交易时间 yyyy-MM-dd HH:mm:ss")
    private String txTime;

    /**
     * 请使用拉取最后一张注单的更新时间当做取下一次拉帐的 timeFrom 参数
     * 注意：若某次取值无资料 或 无更新资料，则将下次取值 timeFrom 设为现在时间的前一分钟
     */
    @ApiModelProperty(value = "更新时间 yyyy-MM-dd HH:mm:ss")
    private String updateTimeStr;

    @ApiModelProperty(value = "玩家 ID")
    private String account;

    @ApiModelProperty(value = "游戏平台的下注项目")
    private String betType;

    @ApiModelProperty(value = "游戏平台名称")
    private String platform;

    @ApiModelProperty(value = "交易类型,-1.取消投注，超时或系统错误时会发生,0.已下注,1.已结账,2.注单无效，当有问题发生在该局或是该注单时,3.赛马游戏割马后退回的金额,5.因赛马规则限制退还下注\"位置Place\"的交易(仅支持 HORSEBOOK),9.斗鸡(SV388)无效的交易，不会在后台报表呈现")
    private Integer txStatus;

    @ApiModelProperty(value = "下注金额")
    private BigDecimal betAmount;

    @ApiModelProperty(value = "游戏名称")
    private String gameName;

    /**
     * 如果您需要唯一值，请使用 platform + platformTxId
     */
    @ApiModelProperty(value = "游戏商注单号")
    private String platformTxId;

    @ApiModelProperty(value = "玩家下注时间yyyy-MM-dd HH:mm:ss")
    private String betTime;

    @ApiModelProperty(value = "平台游戏代码")
    private String gameCode;

    @ApiModelProperty(value = " 玩家货币代码")
    private String currency;

    @ApiModelProperty(value = " 累积奖金的下注金额")
    private BigDecimal jackpotBetAmount;

    @ApiModelProperty(value = " 累积奖金的获胜金额")
    private BigDecimal jackpotWinAmount;

    @ApiModelProperty(value = " 游戏平台有效投注")
    private BigDecimal turnover;

    @ApiModelProperty(value = " 游戏商的回合识别码")
    private String roundId;

    @ApiModelProperty(value = " 游戏讯息会由游戏商以 JSON 格式提供")
    @Column(columnDefinition = "TEXT")
    private String gameInfo;

    @ApiModelProperty(value = "洗码状态：1：成功")
    private Integer washCodeStatus;

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

    @Transient
    @ApiModelProperty(value = "是否新增，0.修改,1.新增")
    private Integer isAdd = 0;

    @Transient
    @ApiModelProperty(value = "新真实返还金额")
    private BigDecimal newRealWinAmount;

    @Transient
    @ApiModelProperty(value = "新有效下注")
    private BigDecimal newTurnover;

    public GameRecordAe(){

    }

    public GameRecordAe(BigDecimal betAmount,BigDecimal realBetAmount,BigDecimal winAmount){
        this.betAmount = betAmount==null?BigDecimal.ZERO:betAmount;
        this.realBetAmount = realBetAmount==null?BigDecimal.ZERO:realBetAmount;
        this.winAmount = winAmount==null?BigDecimal.ZERO:winAmount;
    }
}
