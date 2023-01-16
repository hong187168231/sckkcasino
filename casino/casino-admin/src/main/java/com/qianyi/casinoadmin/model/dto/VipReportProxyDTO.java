package com.qianyi.casinoadmin.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 系统权限dto
 *
 * @author lance
 * @since 2022 -03-02 15:30:49
 */
@Data
@ApiModel
public class VipReportProxyDTO {

    @ApiModelProperty(value = "当前页(默认第一页)")
    private Integer pageCode;
    @ApiModelProperty(value = "每页大小(默认10条)")
    private Integer pageSize;

    @ApiModelProperty(value = "代理账号")
    private String proxyUserName;
    @ApiModelProperty(value = "起始时间查询")
    private String startTime;
    @ApiModelProperty(value = "结束时间查询")
    private String endTime;

    @ApiModelProperty(value = "起始时间查询")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String startDate;
    @ApiModelProperty(value = "结束时间查询")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String endDate;

    @ApiModelProperty(hidden = true,value = "代理id")
    private Long proxyUserId;

    @ApiModelProperty(hidden = true)
    private Long firstProxy;

    @ApiModelProperty(hidden = true)
    private Long secondProxy;


}
