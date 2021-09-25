package com.qianyi.casinoadmin.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class BankcardsVo  implements Serializable {
    private static final long serialVersionUID = -3009873269250305179L;

    @ApiModelProperty("会员id")
    private Long userId;
    @ApiModelProperty("会员账号")
    private String account;
    @ApiModelProperty(value = "银行卡id")
    private String bankId;
    @ApiModelProperty(value = "用户的银行账号")
    private String bankAccount;
    @ApiModelProperty(value = "开户地址")
    private String address;
    @ApiModelProperty(value = "开户名")
    private String realName;
    @ApiModelProperty(value = "0:未禁用 1：禁用")
    private Integer disable;
}
