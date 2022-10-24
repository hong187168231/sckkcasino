package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Data
@ApiModel("公告/活动")
public class Notice extends BaseEntity {
    @ApiModelProperty("内容")
    private String title;
    @ApiModelProperty("简介")
    private String introduction;
    @ApiModelProperty("英文标题")
    @Column(length = 1500)
    private String enTitle;
    @ApiModelProperty("英文内容")
    @Column(length = 1500)
    private String enIntroduction;
    @ApiModelProperty("高棉语标题")
    @Column(length = 1500)
    private String khTitle;
    @ApiModelProperty("高棉语内容")
    @Column(length = 1500)
    private String khIntroduction;
    @ApiModelProperty("详情访问页")
    private String url;
    @ApiModelProperty("是否上架")
    private Boolean isShelves = true;
    @ApiModelProperty("显示类型 0 全部 1 跑马灯 2 弹窗")
    private Integer showType;
}
