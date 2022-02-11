package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
@ApiModel("游戏表")
public class AdGame extends BaseEntity{

    @ApiModelProperty(value = "游戏平台ID")
    private Integer gamePlatformId;

    @ApiModelProperty(value = "游戏编码")
    private String gameCode;

    @ApiModelProperty(value = "游戏名称")
    private String gameName;

    @ApiModelProperty(value = "是否维护 0：维护 1正常")
    private Integer gamesStatus;
}
