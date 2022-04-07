package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "error_order")
public class ErrorOrder extends BaseEntity {

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("用户名")
    private Long userName;

    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal money;

    @ApiModelProperty("0.失败、1.补单成功、2.取消补单")
    private Integer status;

    @ApiModelProperty(value = "账变类型:0.洗码领取,7.转入wm,8.一键回收(转出WM),9.代理佣金领取,10.转入PC/CQ9,11.转出PC/CQ9")
    private Integer type;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("平台名称: WM, PG/CQ9")
    private String platform;
}
