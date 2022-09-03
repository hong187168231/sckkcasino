package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GameRecordAeVo implements Serializable {

    private static final long serialVersionUID = -648165416541654659L;

    @ApiModelProperty(value = "ID")
    private Long id;
    @ApiModelProperty(value = "平台游戏类型")
    private String gameType;
    @ApiModelProperty(value = "我方会员账号")
    private String account;
    @ApiModelProperty(value = "我方账号")
    private String userId;
    @ApiModelProperty(value = "游戏商注单号")
    private String platformTxId;
    @ApiModelProperty(value = "交易类型,-1.取消投注，超时或系统错误时会发生,0.已下注,1.已结账,2.注单无效，当有问题发生在该局或是该注单时,3.赛马游戏割马后退回的金额,5.因赛马规则限制退还下注\"位置Place\"的交易(仅支持 HORSEBOOK),9.斗鸡(SV388)无效的交易，不会在后台报表呈现")
    private Integer txStatus;
    @ApiModelProperty(value = "下注金额")
    private BigDecimal betAmount;
    @ApiModelProperty(value = "真实下注金额")
    private BigDecimal realBetAmount;
    @ApiModelProperty(value = "返还金额 (包含下注金额)")
    private BigDecimal winAmount;
    @ApiModelProperty(value = " 有效投注")
    private BigDecimal turnover;
    @ApiModelProperty(value = "结算时间 yyyy-MM-dd HH:mm:ss")
    private String updateTimeStr;
    @ApiModelProperty(value = "玩家下注时间yyyy-MM-dd HH:mm:ss")
    private String betTime;
}
