package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
@ApiModel("三方游戏平台表")
public class PlatformGame extends BaseEntity  {

    @ApiModelProperty(value = "游戏平台ID")
    private Integer gamePlatformId;

    @ApiModelProperty(value = "游戏平台ID")
    private String gamePlatformName;

    @ApiModelProperty(value = "平台状态：0下架，1：上架")
    private String gameStatus;
}
