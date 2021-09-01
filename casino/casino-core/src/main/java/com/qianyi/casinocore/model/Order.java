package com.qianyi.casinocore.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "`order`")
public class Order extends BaseEntity {
    private Long userId;
    private String no;
    private BigDecimal money;
    //1.未确认。 2.成功   3.失败
    private Integer state;

    private String remark;
}
