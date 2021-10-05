package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "`order`")
public class Order extends BaseEntity {
    @ApiModelProperty("用户id")
    private Long userId;
    @ApiModelProperty("订单号")
    private String no;
    @ApiModelProperty("金额")
    private BigDecimal money;
    //1.未确认。 2.成功   3.失败
    @ApiModelProperty("1.未确认。 2.成功   3.失败")
    private Integer state;
    @ApiModelProperty("备注")
    private String remark;

    /**
     * 0.转入，1.转出
     */
    @ApiModelProperty("0.转入，1.转出")
    private Integer type;
}
