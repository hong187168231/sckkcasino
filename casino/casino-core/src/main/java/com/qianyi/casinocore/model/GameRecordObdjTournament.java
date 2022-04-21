package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Entity
@Table(name ="game_record_obdj_tournament")
@ApiModel("OB游戏记录锦标赛")
public class GameRecordObdjTournament extends BaseEntity{

    @ApiModelProperty(value = "赛事ID")
    private String matchId;

    @ApiModelProperty(value = "赛事简称")
    private String matchName;
}
