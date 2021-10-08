package com.qianyi.casinoadmin.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 域名配置
 */
@Data
public class DomainNameVo {
    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("域名配置")
    private String domainNameConfiguration;
}
