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
import java.util.Date;

@Data
@Entity
@Table(name ="game_record_vnc",uniqueConstraints={@UniqueConstraint(columnNames={"merchantCode","betOrder"})})
@ApiModel("越南彩游戏记录")
public class GameRecordVNC extends BaseEntity{

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

    public GameRecordVNC(){

    }

    public GameRecordVNC(BigDecimal betMoney,BigDecimal backWaterMoney,BigDecimal realMoney,BigDecimal winMoney){
        this.betMoney = betMoney==null?BigDecimal.ZERO:betMoney;
        this.backWaterMoney = backWaterMoney==null?BigDecimal.ZERO:backWaterMoney;
        this.realMoney = realMoney==null?BigDecimal.ZERO:realMoney;
        this.winMoney = winMoney==null?BigDecimal.ZERO:winMoney;
    }
}
