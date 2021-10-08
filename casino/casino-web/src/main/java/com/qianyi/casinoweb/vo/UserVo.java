package com.qianyi.casinoweb.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.math.BigDecimal;

@Data
@ApiModel("用户信息")
public class UserVo {
    private Long userId;
    @ApiModelProperty("名称")
    private String name;
    private String account;
    private String headImg;
    @ApiModelProperty("账户余额")
    private BigDecimal money;
    @ApiModelProperty("可提款金额")
    private BigDecimal drawMoney;
    @ApiModelProperty("未完成流水")
    private BigDecimal unfinshTurnover;
    @ApiModelProperty("真实姓名")
    private String realName;
    @ApiModelProperty("洗码余额")
    private BigDecimal washCode;
    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("QQ")
    private String qq;
    @ApiModelProperty("微信")
    private String webChat;
    @ApiModelProperty("手机号")
    private String phone;
    @ApiModelProperty("邀请码")
    private String inviteCode;
    @ApiModelProperty("分润余额")
    private BigDecimal shareProfit;
    @ApiModelProperty("域名配置")
    private String domain;
}
