package com.qianyi.casinoadmin.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class VisitsVo {

    @ApiModelProperty("域名")
    private String domainName;

    @ApiModelProperty("域名去重Ip访问量")
    private Integer domainIpCount;

    @ApiModelProperty("访问总数量")
    private Integer domainCount;
}
