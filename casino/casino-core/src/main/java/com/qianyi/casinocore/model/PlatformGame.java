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

    @ApiModelProperty(value = "平台状态：0：维护，1：正常")
    private Integer gameStatus;

    public PlatformGame() {
    }

    public PlatformGame(Integer gamePlatformId, String gamePlatformName, Integer gameStatus) {
        this.gamePlatformId = gamePlatformId;
        this.gamePlatformName = gamePlatformName;
        this.gameStatus = gameStatus;
    }
}
