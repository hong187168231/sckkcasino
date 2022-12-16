package com.qianyi.casinocore.model;

import com.qianyi.modulecommon.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@ApiModel("注单详情表")
public class RptBetInfoDetail extends BaseEntity{

    @ApiModelProperty(value = "玩家 账号")
    private String account;

    @ApiModelProperty(value = "我方账号Id")
    private Long userKkId;
    @ApiModelProperty("玩法类型:(0:越南玩法,1:柬埔寨玩法)")
    private Integer gamePlay;
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
    @ApiModelProperty(value = "旧真实返还金额")
    private BigDecimal oldRealWinAmount;

    @Transient
    @ApiModelProperty(value = "旧有效下注")
    private BigDecimal oldTurnover;


    public RptBetInfoDetail(){

    }

    public RptBetInfoDetail(BigDecimal betMoney,BigDecimal backWaterMoney,BigDecimal realMoney,BigDecimal winMoney){
        this.betMoney = betMoney==null?BigDecimal.ZERO:betMoney;
        this.backWaterMoney = backWaterMoney==null?BigDecimal.ZERO:backWaterMoney;
        this.realMoney = realMoney==null?BigDecimal.ZERO:realMoney;
        this.winMoney = winMoney==null?BigDecimal.ZERO:winMoney;
    }
}
