package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.Entity;
import java.math.BigDecimal;

@Data
@Entity
@ApiModel("充值订单")
public class ChargeOrder extends BaseEntity{

    private Long userId;

    private String orderNo;

    private Integer status;

    private String remitter;

    private Integer remitType;

    private BigDecimal chargeAmount;

    private String remark;
}
