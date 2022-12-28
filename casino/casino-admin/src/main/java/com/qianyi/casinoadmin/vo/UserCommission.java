package com.qianyi.casinoadmin.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 玩家代理返佣设置
 */
@Data
@ApiModel
public class UserCommission implements Serializable {
    @ApiModelProperty("一级玩家返佣")
    private BigDecimal firstCommission;

    @ApiModelProperty("二级玩家返佣")
    private BigDecimal secondCommission;

    @ApiModelProperty("三级玩家返佣")
    private BigDecimal thirdCommission;
}
