package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qianyi.casinocore.model.CompanyProxyMonth;
import com.qianyi.casinocore.util.CommonConst;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class CompanyProxyMonthVo implements Serializable {
    private static final long serialVersionUID = -6989742983298432179L;
    @ApiModelProperty(value = "ID")
    private Long id;
    @ApiModelProperty(value = "代理id")
    private Long proxyUserId;
    @ApiModelProperty(value = "账号")
    private String userName;
    @ApiModelProperty(value = "昵称")
    private String nickName;
    @ApiModelProperty(value = "代理角色 1：总代理 2：区域代理 3：基层代理")
    private Integer proxyRole;
    @ApiModelProperty(value = "统计时段")
    private String staticsTimes;
    @ApiModelProperty(value = "创造业绩的玩家数")
    private Integer playerNum = CommonConst.NUMBER_0;
    @ApiModelProperty(value = "团队业绩流水")
    private BigDecimal groupBetAmount = BigDecimal.ZERO;
    @ApiModelProperty(value = "返佣级别")
    private String profitLevel;
    @ApiModelProperty(value = "返佣比例")
    private String profitRate;
    @ApiModelProperty(value = "团队总返佣")
    private BigDecimal groupTotalprofit = BigDecimal.ZERO;
    @ApiModelProperty(value = "佣金分成比")
    private BigDecimal benefitRate;
    @ApiModelProperty(value = "个人结算佣金")
    private BigDecimal profitAmount = BigDecimal.ZERO;
    @ApiModelProperty(value = "结清状态")
    private Integer settleStatus = CommonConst.NUMBER_0;
    @ApiModelProperty("创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @ApiModelProperty("创建人")
    private String createBy;
    @ApiModelProperty("最后修改时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    @ApiModelProperty("最后修改人")
    private String updateBy;
    public CompanyProxyMonthVo(){

    }
    public CompanyProxyMonthVo(CompanyProxyMonth companyProxyMonth){
        this.id = companyProxyMonth.getId();
        this.proxyUserId = companyProxyMonth.getUserId();
        this.proxyRole = companyProxyMonth.getProxyRole();
        this.staticsTimes = companyProxyMonth.getStaticsTimes();
        this.playerNum = companyProxyMonth.getPlayerNum();
        this.groupBetAmount = companyProxyMonth.getGroupBetAmount();
        this.profitLevel = companyProxyMonth.getProfitLevel();
        this.profitRate = companyProxyMonth.getProfitRate();
        this.groupTotalprofit = companyProxyMonth.getGroupTotalprofit();
        this.benefitRate = companyProxyMonth.getBenefitRate();
        this.profitAmount = companyProxyMonth.getProfitAmount();
        this.settleStatus = companyProxyMonth.getSettleStatus();
        this.createTime = companyProxyMonth.getCreateTime();
        this.updateTime = companyProxyMonth.getUpdateTime();
        this.createBy = companyProxyMonth.getCreateBy();
        this.updateBy = companyProxyMonth.getUpdateBy();
    }
}
