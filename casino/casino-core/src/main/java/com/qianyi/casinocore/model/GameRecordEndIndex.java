package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
@ApiModel("报表最后获取下标")
public class GameRecordEndIndex extends BaseEntity {
    @ApiModelProperty("wm注单id")
    private Long GameRecordId;

    @ApiModelProperty("PG电子注单id")
    private Long PGMaxId;

    @ApiModelProperty("CQ9电子注单id")
    private Long CQ9MaxId;
}
