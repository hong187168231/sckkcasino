package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class UserThirdVo  implements Serializable {

    private static final long serialVersionUID = -3005414569250305179L;
    @ApiModelProperty("我方会员账号")
    private String account;
    @ApiModelProperty("三方会员账号")
    private String thirdAccount;
    @ApiModelProperty("余额")
    private BigDecimal money;
    @ApiModelProperty("剩余打码量")
    private BigDecimal codeNum;
    @ApiModelProperty("WM余额")
    private BigDecimal wmMoney;
    @ApiModelProperty("平台名称 WM、PG/CQ9")
    private String platform;

}
