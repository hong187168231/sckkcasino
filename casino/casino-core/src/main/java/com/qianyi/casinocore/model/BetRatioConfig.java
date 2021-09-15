package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
@ApiModel("打码赔率配置表")
public class BetRatioConfig extends BaseEntity {

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("打码倍率")
    private Float codeTimes;

}
