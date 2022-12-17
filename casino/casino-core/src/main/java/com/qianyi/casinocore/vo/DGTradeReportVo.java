package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("DG交易记录")
public class DGTradeReportVo {
    private Long id;//	Long	注单唯一Id
    private Integer lobbyId;//	Integer 游戏大厅号 1:旗舰厅；2:亚洲厅；3，4:现场厅；5:欧美厅,7:国际厅,8:区块链厅  可以为空
    private Integer tableId;//	Integer	游戏桌号	可以为空
    private Long shoeId;//	Long	游戏靴号	可以为空
    private Long playId;//	Long	游戏局号	可以为空
    private Integer GameType;//	Integer	游戏类型
    private Integer GameId;//	Integer	游戏Id
    private Long memberId;//	Long	会员Id
    private String betTime;//	Date	游戏下注时间
    private String calTime;//	Date	游戏结算时间	可以为空
    private BigDecimal winOrLoss;//	Double	派彩金额 (输赢应扣除下注金额)	可以为空
    private BigDecimal winOrLossz;//	Double	好路追注派彩金额	winOrLoss为总派彩金额
    private BigDecimal betPoints;//	Double	下注金额
    private BigDecimal betPointsz;//	Double	好路追注金额	betPoints为总金额
    private BigDecimal availableBet;//	Double	有效下注金额	可以为空
    private String userName;//	String	会员登入账号
    private String result;//	String	游戏结果	可以为空
    private String betDetail;//	String	下注注单	可以为空
    private String betDetailz;//	String	好路追注注单	可以为空,betDetail为总单
    private String ip;//	String	下注时客户端IP
    private String ext;//	String	游戏唯一ID
    private Integer isRevocation;//	Integer	是否结算：1：已结算 2:撤销
    private BigDecimal balanceBefore;//	Double	余额
    private Long parentBetId;//	Long	撤销的那比注单的ID	对冲注单才有,可以为空
    private Integer currencyId;//	Integer	货币ID
    private Integer deviceType;//	Integer	下注时客户端类型
    private Long pluginid;//	Long	追注转账流水号	共享钱包API可用于对账,普通转账API可忽略

}
