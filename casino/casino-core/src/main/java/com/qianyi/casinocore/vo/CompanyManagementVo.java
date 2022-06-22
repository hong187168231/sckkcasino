package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class CompanyManagementVo implements Serializable {

    private static final long serialVersionUID = -488946465305179L;

    @ApiModelProperty(value = "ID")
    private Long id;

    @ApiModelProperty(value = "公司名称")
    private String companyName;
}
