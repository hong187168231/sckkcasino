package com.qianyi.casinoweb.vo;

import com.qianyi.casinocore.model.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Data
@ApiModel("洗码")
public class WashCodeVo{

    @ApiModelProperty(value = "游戏ID")
    private String gameId;

    @ApiModelProperty(value = "游戏名称")
    private String gameName;

    @ApiModelProperty(value = "有效投注额")
    private BigDecimal validbet;

    @ApiModelProperty(value = "返水比例")
    private BigDecimal rate;

    @ApiModelProperty(value = "洗码金额")
    private BigDecimal amount;

}
