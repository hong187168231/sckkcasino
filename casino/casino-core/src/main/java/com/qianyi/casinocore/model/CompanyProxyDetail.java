package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("公司代理明细")
public class CompanyProxyDetail extends BaseEntity implements Cloneable{

    @ApiModelProperty(value = "用户id")
    private Long userId;
    @ApiModelProperty(value = "总代理")
    private Long firstId;
    @ApiModelProperty(value = "区域代理")
    private Long secondeId;
    @ApiModelProperty(value = "基层代理")
    private Long thirdId;
    @ApiModelProperty(value = "统计时段")
    private String staticsTimes;
    @ApiModelProperty(value = "创造业绩的玩家数")
    private Integer playerNum;
    @ApiModelProperty(value = "团队业绩流水")
    private BigDecimal groupBetAmount;
    @ApiModelProperty(value = "返佣级别")
    private String profitLevel;
    @ApiModelProperty(value = "返佣比例")
    private String profitRate;
    @ApiModelProperty(value = "团队总返佣")
    private BigDecimal groupTotalprofit;
    @ApiModelProperty(value = "佣金分成比")
    private BigDecimal benefitRate;
    @ApiModelProperty(value = "个人结算佣金")
    private BigDecimal profitAmount;
    @ApiModelProperty(value = "结清状态")
    private String settleStatus;

    @Override
    public Object clone() {
        CompanyProxyDetail companyProxyDetail = null;
        try{
            companyProxyDetail = (CompanyProxyDetail)super.clone();
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return companyProxyDetail;
    }

}
