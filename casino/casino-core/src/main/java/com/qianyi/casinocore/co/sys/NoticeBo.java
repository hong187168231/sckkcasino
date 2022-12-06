package com.qianyi.casinocore.co.sys;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel
@Data
public class NoticeBo implements Serializable {

    @ApiModelProperty(value = "id主键")
    private Long id;
    @ApiModelProperty(value = "标题")
    @NotBlank(message = "标题 不能为空")
    private String title;
    @ApiModelProperty(value = "英文标题")
    @NotBlank(message = "英文标题 不能为空")
    private String enTitle;
    @ApiModelProperty(value = "柬文标题")
    @NotBlank(message = "柬文标题 不能为空")
    private String khTitle;
    @ApiModelProperty(value = "是否上架 true false")
    private Boolean isShelves = false;
    @ApiModelProperty(value = "详情访问页")
    private String url;
    @ApiModelProperty(value = "中文简介")
    @NotBlank(message = "中文简介 不能为空")
    private String introduction;
    @ApiModelProperty(value = "英文简介")
    @NotBlank(message = "英文简介 不能为空")
    private String enIntroduction;
    @ApiModelProperty(value = "柬语简介")
    @NotBlank(message = "柬语简介 不能为空")
    private String khIntroduction;
    @ApiModelProperty(value = "显示类型 0-所有 1-跑马灯 2-弹窗")
    @NotNull(message = "显示类型 不能为空")
    private Integer showType;


}
