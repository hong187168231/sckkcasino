package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.persistence.Entity;

@Entity
@Data
@ApiModel("客户风险配置表")
public class RiskConfig extends BaseEntity {

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "次数限制")
    private Integer timeLimit;
}
