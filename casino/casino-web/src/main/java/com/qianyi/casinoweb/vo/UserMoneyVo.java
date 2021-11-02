package com.qianyi.casinoweb.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.math.BigDecimal;

@Data
@ApiModel("用户信息")
public class UserMoneyVo {
    @ApiModelProperty("账户余额")
    private BigDecimal money;
    @ApiModelProperty("可提款金额")
    private BigDecimal drawMoney;
    @ApiModelProperty("未完成流水")
    private BigDecimal unfinshTurnover;
    @ApiModelProperty("洗码余额")
    private BigDecimal washCode;
    @ApiModelProperty("分润余额")
    private BigDecimal shareProfit;
}
