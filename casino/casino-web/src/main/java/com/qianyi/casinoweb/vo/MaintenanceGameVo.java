package com.qianyi.casinoweb.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("维护状态游戏列表")
@Data
public class MaintenanceGameVo {

    @ApiModelProperty(value = "平台:WM,PG,CQ9")
    private String gamePlatformName;

    @ApiModelProperty(value = "平台状态：0：维护，1：正常")
    private Integer gameStatus;

    @ApiModelProperty(value = "维护状态游戏列表")
    private List<AdGame> gameList;

    @Data
    @ApiModel("维护状态游戏")
    public static class AdGame {

        @ApiModelProperty(value = "游戏编码")
        private String gameCode;

        @ApiModelProperty(value = "游戏名称")
        private String gameName;

    }
}
