package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
@ApiModel("游戏表")
public class AdGame extends BaseEntity{

    @ApiModelProperty(value = "平台:WM,PG,CQ9")
    private String gamePlatformName;

    @ApiModelProperty(value = "游戏平台名称")
    private String gamePlatformName;

    @ApiModelProperty(value = "游戏编码")
    private String gameCode;

    @ApiModelProperty(value = "游戏中文名称")
    private String gameName;

    @ApiModelProperty(value = "游戏英文名称")
    private String gameEnName;

    @ApiModelProperty(value = "是否维护 0：维护 1正常 2下架")
    private Integer gamesStatus;
}
