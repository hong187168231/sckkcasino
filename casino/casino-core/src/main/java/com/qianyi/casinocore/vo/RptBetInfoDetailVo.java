package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class RptBetInfoDetailVo implements Serializable {

    @ApiModelProperty("主键")
    private Long id;
    @ApiModelProperty("期号")
    private String issue;
    @ApiModelProperty("会员id")
    private Long userId;
    @ApiModelProperty("会员账号")
    private String userName;
    @ApiModelProperty("商户号")
    private String merchantCode;
    @ApiModelProperty("货币")
    private String currency;
    @ApiModelProperty("主单号")
    private String betOrder;
    @ApiModelProperty("下注时间")
    private Date betTime;
    @ApiModelProperty("子单号")
    private String betDetailOrder;
    @ApiModelProperty("下注号码")
    private String betCode;
    @ApiModelProperty("下注种类")
    private String betCategory;
    @ApiModelProperty("下注玩法")
    private String betPlayType;
    @ApiModelProperty("'打'字")
    private Integer betPlayTypeCombine;
    @ApiModelProperty("下注城市")
    private String betCity;
    @ApiModelProperty("赔率")
    private BigDecimal odds;
    @ApiModelProperty("下注城市 0北部,1中部,2南部")
    private Integer betCitySection;
    @ApiModelProperty("单笔下注金额")
    private BigDecimal money;
    @ApiModelProperty("下注总金额")
    private BigDecimal betMoney;
    @ApiModelProperty("退水总金额")
    private BigDecimal backWaterMoney;
    @ApiModelProperty("实付金额")
    private BigDecimal realMoney;
    @ApiModelProperty("中奖金额")
    private BigDecimal winMoney;
    @ApiModelProperty("是否取消(0未取消,1已取消)")
    private Boolean isCanceled;
    @ApiModelProperty("取消时间")
    private Date canceledTime;
    @ApiModelProperty("结算状态(0/false:未开奖,1/true:已开奖)")
    private Boolean settleState;
    @ApiModelProperty("结算时间")
    private Date settleTime;


    @ApiModelProperty("下注时间String")
    private String betTimeStr;

    @ApiModelProperty("结算时间字符串(下注多城市情况下,记入最后结算城市)")
    private String settleTimeStr;
}
