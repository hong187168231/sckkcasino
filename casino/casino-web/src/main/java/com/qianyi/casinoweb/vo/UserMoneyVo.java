package com.qianyi.casinoweb.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.math.BigDecimal;

@Data
@ApiModel("用户钱包信息")
public class UserMoneyVo {
    @ApiModelProperty("账户余额")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal money;
    @ApiModelProperty("可提款金额")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal drawMoney;
    @ApiModelProperty("未完成流水")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal unfinshTurnover;
    @ApiModelProperty("洗码余额")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal washCode;
    @ApiModelProperty("分润余额")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal shareProfit;
}
