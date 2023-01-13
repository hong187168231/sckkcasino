package com.qianyi.casinoadmin.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Transient;


@Data
@ApiModel
public class VipReportTotalDTO {
    @ApiModelProperty(value = "账号")
    private String account;
    @ApiModelProperty(value = "起始时间查询")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String startTime;
    @ApiModelProperty(value = "结束时间查询")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String endTime;
    @ApiModelProperty(value = "等级数组,逗号分隔")
    private String levelArray;

    @Transient
    @ApiModelProperty(hidden = true)
    private Long userId;

    @Transient
    @ApiModelProperty(hidden = true)
    private String pf;
}
