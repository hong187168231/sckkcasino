package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class GameRecordVNCVo implements Serializable {

    private static final long serialVersionUID = -697896578455179L;

    @ApiModelProperty(value = "我方账号")
    private Long userId;

    @ApiModelProperty(value = "玩家 ID")
    private String account;

    @ApiModelProperty("会员账号")
    private String userName;
    @ApiModelProperty("商户号")
    private String merchantCode;
    @ApiModelProperty("货币")
    private String currency;
    @ApiModelProperty("期号")
    private String issue;
    @ApiModelProperty("注单号")
    private String betOrder;
    @ApiModelProperty("下注时间")
    private Date betTime;
    @ApiModelProperty("下注类型")
    private String betCategory;
    @ApiModelProperty("下注号码")
    private String betCode;
    @ApiModelProperty("下注城市")
    private String betCities;
    @ApiModelProperty("下注金额")
    private BigDecimal betMoney;
    @ApiModelProperty("退水金额")
    private BigDecimal backWaterMoney;
    @ApiModelProperty("实付金额")
    private BigDecimal realMoney;
    @ApiModelProperty("是否存在取消0未取消,1取消,2部分取消")
    private Integer hasCanceled;
    @ApiModelProperty("取消时间")
    private Date cancelTime;
    @ApiModelProperty("结算状态,0:未结算,1已结算(下注多个城市情况下,所有城市都结算才为结算)")
    private Boolean settleState;
    @ApiModelProperty("结算时间(下注多城市情况下,记入最后结算城市)")
    private Date settleTime;
    @ApiModelProperty("中奖金额")
    private BigDecimal winMoney;
    @ApiModelProperty("原始下注数据")
    private String rawData;

    @ApiModelProperty("下注时间String")
    private String betTimeStr;

    @ApiModelProperty("结算时间字符串(下注多城市情况下,记入最后结算城市)")
    private String settleTimeStr;
}
